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

#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <time.h>
#include <stdlib.h>

#include <kaa/kaa_error.h>
#include <kaa/kaa_context.h>
#include <kaa/platform/kaa_client.h>
#include <kaa/utilities/kaa_log.h>
#include <kaa/kaa_logging.h>
#include <kaa/platform-impl/common/ext_log_upload_strategies.h>

#include <wiringPi.h>

#include "dht11/dht11.h"

#define MAX_LOG_BUCKET_SIZE     SIZE_MAX
#define MAX_LOG_COUNT                 5

/*
 * Pin on Rasbery Pi 2 Model B
 */
#define DHT11_PIN    7

#define KAA_DEMO_UPLOAD_COUNT_THRESHOLD      1 /* Count of collected logs needed to initiate log upload */
#define KAA_DEMO_LOG_GENERATION_FREQUENCY    1 /* In seconds */

#define KAA_DEMO_RETURN_IF_ERROR(error, message) \
    if ((error)) { \
        printf(message ", error code %d\n", (error)); \
        return (error); \
    }



/*
 * Forward declarations.
 */
extern kaa_error_t ext_unlimited_log_storage_create(void **log_storage_context_p
                                                  , kaa_logger_t *logger);



static void kaa_demo_add_log_record(void *context)
{
    static size_t log_record_counter = 0;

    float humidity = 0.0;
    float temperature = 0.0;

    if (dht11_read_val(DHT11_PIN, &humidity, &temperature) != 0) {
        printf("Failed to read data from sensor\n");
        return;
    }

    ++log_record_counter;

    kaa_user_log_record_t *log_record = kaa_logging_sensor_data_create();
    if (!log_record) {
        printf("Failed to create log record, error code %d\n", KAA_ERR_NOMEM);
        return;
    }

    log_record->sensor_id = kaa_string_copy_create("Sensor 1");
    log_record->region = kaa_string_copy_create("Region 1");
    log_record->model = kaa_string_copy_create("DHT11");
    log_record->value = temperature;

    printf("Going to add %zuth log record: { id: '%s', region: '%s', model: '%s', val: %g }\n"
            , log_record_counter, log_record->sensor_id->data, log_record->region->data, log_record->model->data, log_record->value);

    kaa_error_t error_code = kaa_logging_add_record(kaa_client_get_context((kaa_client_t *)context)->log_collector, log_record, NULL);
    if (error_code) {
        printf("Failed to add log record, error code %d\n", error_code);
    }

    log_record->destroy(log_record);
}

int main(/*int argc, char *argv[]*/)
{
    printf("Cassandra data analytics demo started\n");

    if (wiringPiSetup() == -1) {
        printf("Failed to initialize Pi wiring\n");
        exit(1);
    }

    kaa_client_t *kaa_client = NULL;

    void *log_storage_context         = NULL;
    void *log_upload_strategy_context = NULL;

    /**
     * Initialize Kaa client.
     */
    kaa_error_t error_code = kaa_client_create(&kaa_client, NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed create Kaa client");

    /**
     * Configure Kaa data collection module.
     */
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

    printf("Cassandra data analytics demo stopped\n");

    return error_code;
}

