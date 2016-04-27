/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <stdint.h>
//#include <string.h>
//#include <time.h>

#include <kaa/kaa_error.h>
#include <kaa/kaa_context.h>
#include <kaa/platform/kaa_client.h>
#include <kaa/kaa_logging.h>
#include <kaa/utilities/kaa_log.h>
#include <kaa/platform-impl/common/ext_log_upload_strategies.h>



/*
 * Strategy-specific configuration parameters used by Kaa log collection feature.
 */
#define KAA_DEMO_UPLOAD_COUNT_THRESHOLD      1 /* Count of collected logs needed to initiate log upload */
#define KAA_DEMO_LOG_GENERATION_FREQUENCY    1 /* In seconds */

#define LOGS_TO_SEND_COUNT    1000
#define ZONE_COUNT            10
#define PANEL_COUNT           10
#define MAX_PANEL_POWER       100

#define MAX_LOG_BUCKET_SIZE     SIZE_MAX
#define MAX_LOG_COUNT                 5

/*
 * Forward declarations.
 */
extern kaa_error_t ext_unlimited_log_storage_create(void **log_storage_context_p
                                                  , kaa_logger_t *logger);

static kaa_client_t *kaa_client = NULL;

static void *log_storage_context         = NULL;
static void *log_upload_strategy_context = NULL;



#define KAA_DEMO_RETURN_IF_ERROR(error, message) \
    if ((error)) { \
        printf(message ", error code %d\n", (error)); \
        return (error); \
    }


static double get_random_double(int max) {
    return ((double) rand() / RAND_MAX) * max;
}

static void kaa_demo_add_log_record(void *context)
{
    static size_t log_number = LOGS_TO_SEND_COUNT;

    kaa_logging_power_report_t *log_record = kaa_logging_power_report_create();
    if (!log_record) {
        printf("Failed to create log record, error code %d\n", KAA_ERR_NOMEM);
        return;
    }

    log_record->timestamp = time(NULL) * 1000; // expected in millis
    log_record->samples = kaa_list_create();

    size_t zone_id = 0;
    size_t panel_id = 0;
    for (zone_id = 0; zone_id < ZONE_COUNT; ++zone_id) {
        for (panel_id = 0; panel_id < PANEL_COUNT; ++panel_id) {
            kaa_logging_power_sample_t *sample = kaa_logging_power_sample_create();
            sample->zone_id = zone_id;
            sample->panel_id = panel_id;
            sample->power = get_random_double(MAX_PANEL_POWER);

            kaa_list_push_back(log_record->samples, sample);
        }
    }

    kaa_error_t error_code = kaa_logging_add_record(kaa_client_get_context((kaa_client_t *)context)->log_collector, log_record, NULL);
    if (error_code) {
        printf("Failed to add log record, error code %d\n", error_code);
    }

    log_record->destroy(log_record);

    if (!--log_number) {
        kaa_client_stop(kaa_client);
    }
}

int main(/*int argc, char *argv[]*/)
{
    printf("Storm data analytics demo started\n");

    srand(time(NULL));

    /**
     * Initialize Kaa client.
     */
    kaa_error_t error_code = kaa_client_create(&kaa_client, NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed create Kaa client");

    error_code = ext_unlimited_log_storage_create(&log_storage_context, kaa_client_get_context(kaa_client)->logger);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to create unlimited log storage");

    error_code = ext_log_upload_strategy_create(kaa_client_get_context(kaa_client), &log_upload_strategy_context, KAA_LOG_UPLOAD_VOLUME_STRATEGY);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to create log upload strategy");

    error_code = ext_log_upload_strategy_set_threshold_count(log_upload_strategy_context, KAA_DEMO_UPLOAD_COUNT_THRESHOLD);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to set threshold log record count");

    kaa_log_bucket_constraints_t bucket_sizes = {
        .max_bucket_size = MAX_LOG_BUCKET_SIZE,
        .max_bucket_log_count = MAX_LOG_COUNT,
    };

    error_code = kaa_logging_init(kaa_client_get_context(kaa_client)->log_collector
                                , log_storage_context
                                , log_upload_strategy_context
                                , &bucket_sizes);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to init Kaa log collector");

    /**
     * Start Kaa client main loop.
     */
    error_code = kaa_client_start(kaa_client, &kaa_demo_add_log_record, (void *)kaa_client, KAA_DEMO_LOG_GENERATION_FREQUENCY);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to start Kaa main loop");

    /**
     * Destroy Kaa client.
     */
    kaa_client_destroy(kaa_client);

    printf("Storm data analytics demo stopped\n");

    return error_code;
}
