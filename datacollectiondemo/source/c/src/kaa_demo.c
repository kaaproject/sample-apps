/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <time.h>

#include <kaa/kaa_error.h>
#include <kaa/kaa_context.h>
#include <kaa/platform/kaa_client.h>
#include <kaa/utilities/kaa_log.h>
#include <kaa/kaa_logging.h>
#include <kaa/platform-impl/common/ext_log_upload_strategies.h>



/*
 * Strategy-specific configuration parameters used by Kaa log collection feature.
 */
#define KAA_DEMO_UPLOAD_COUNT_THRESHOLD      1 /* Count of collected logs needed to initiate log upload */
#define KAA_DEMO_LOG_GENERATION_FREQUENCY    1 /* In seconds */
#define KAA_DEMO_LOGS_TO_SEND                5
#define KAA_DEMO_LOG_STORAGE_SIZE            10000 /* The amount of space allocated for a log storage, in bytes */
#define KAA_DEMO_BUCKET_SIZE                 500   /* Size of single bucket, in bytes */
#define KAA_DEMO_LOGS_IN_BUCKET              5     /* Amount of logs in single bucket */
#define KAA_DEMO_LOGS_TO_KEEP                50    /* The minimum amount of logs to be present in a log storage, in percents */
#define KAA_DEMO_LOG_BUF_SZ                  32    /* Log buffer size in bytes */

/*
 * Hard-coded Kaa log entry body.
 */
#define KAA_DEMO_LOG_TAG     "TAG"
#define KAA_DEMO_LOG_MESSAGE "MESSAGE_"



/*
 * Forward declarations.
 */
extern kaa_error_t ext_limited_log_storage_create(void **log_storage_context_p
                          , kaa_logger_t *logger
                          , size_t size
                          , size_t percent);


static kaa_client_t *kaa_client = NULL;

static void *log_storage_context         = NULL;
static void *log_upload_strategy_context = NULL;
static size_t log_record_counter = 0;
static size_t log_successfully_sent_counter = 0;



#define KAA_DEMO_RETURN_IF_ERROR(error, message) \
    if ((error)) { \
        printf(message ", error code %d\n", (error)); \
        return (error); \
    }

static void success_log_delivery(void *context, const kaa_log_bucket_info_t *bucket)
{
    (void) context;
    printf("Bucket: %u is successfully delivered. Logs uploaded: %zu\n",
           bucket->bucket_id,
           bucket->log_count);

    log_successfully_sent_counter += bucket->log_count;
}

/* Under normal conditions this callback shouldn't be called */
static void failed_log_delivery(void *context, const kaa_log_bucket_info_t *bucket)
{
    (void) context;
    printf("Log delivery of the bucket: %u is failed!\n", bucket->bucket_id);
}

/* Under normal conditions this callback shouldn't be called */
static void timeout_log_delivery(void *context, const kaa_log_bucket_info_t *bucket)
{
    (void) context;
    printf("Timeout reached for log delivery of the bucket: %u!\n", bucket->bucket_id);
}

static kaa_log_delivery_listener_t log_listener = {
    .on_success = success_log_delivery,
    .on_failed  = failed_log_delivery,
    .on_timeout = timeout_log_delivery,
    .ctx        = NULL,
};

static void kaa_demo_add_log_record(void *context)
{
    (void) context;

    if (log_record_counter >= KAA_DEMO_LOGS_TO_SEND) {
        printf("All logs are sent, waiting for responce\n");
        if (log_successfully_sent_counter == KAA_DEMO_LOGS_TO_SEND) {
            printf("All logs successfully sent, stopping demo...\n");
            kaa_client_stop(context);
        }
        return;
    }

    printf("Going to add %zuth log record\n", log_record_counter);

    kaa_user_log_record_t *log_record = kaa_logging_log_data_create();
    if (!log_record) {
        printf("Failed to create log record, error code %d\n", KAA_ERR_NOMEM);
        return;
    }

    log_record->level = ENUM_LEVEL_KAA_INFO;
    log_record->tag = kaa_string_move_create(KAA_DEMO_LOG_TAG, NULL);

    log_record->time_stamp = KAA_TIME() * 1000;

    char log_message_buffer[KAA_DEMO_LOG_BUF_SZ];
    snprintf(log_message_buffer, KAA_DEMO_LOG_BUF_SZ, KAA_DEMO_LOG_MESSAGE"%zu", log_record_counter);

    log_record->message = kaa_string_copy_create(log_message_buffer);

    kaa_log_record_info_t log_info;
    kaa_error_t error_code = kaa_logging_add_record(kaa_client_get_context(kaa_client)->log_collector, log_record, &log_info);
    if (error_code) {
        printf("Failed to add log record, error code %d\n", error_code);
    } else {
        printf("Log record: %u added to bucket %u\n", log_info.log_id, log_info.bucket_id);
    }

    log_record->destroy(log_record);
    log_record_counter++;
}

int main(/*int argc, char *argv[]*/)
{
    printf("Data collection demo started\n");
    kaa_log_bucket_constraints_t bucket_sizes = {
        .max_bucket_size       = KAA_DEMO_BUCKET_SIZE,
        .max_bucket_log_count  = KAA_DEMO_LOGS_IN_BUCKET,
    };

    /**
     * Initialize Kaa client.
     */
    kaa_error_t error_code = kaa_client_create(&kaa_client, NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed create Kaa client");

    error_code = ext_limited_log_storage_create(&log_storage_context, kaa_client_get_context(kaa_client)->logger, KAA_DEMO_LOG_STORAGE_SIZE, KAA_DEMO_LOGS_TO_KEEP);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to create limited log storage");

    error_code = ext_log_upload_strategy_create(kaa_client_get_context(kaa_client), &log_upload_strategy_context, KAA_LOG_UPLOAD_VOLUME_STRATEGY);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to create log upload strategy");

    error_code = ext_log_upload_strategy_set_threshold_count(log_upload_strategy_context, KAA_DEMO_UPLOAD_COUNT_THRESHOLD);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to set threshold log record count");

    error_code = kaa_logging_init(kaa_client_get_context(kaa_client)->log_collector
                                , log_storage_context
                                , log_upload_strategy_context
                                , &bucket_sizes);

    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to init Kaa log collector");

    error_code = kaa_logging_set_listeners(kaa_client_get_context(kaa_client)->log_collector,
                                           &log_listener);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to add log listeners");

    /**
     * Start Kaa client main loop.
     */
    error_code = kaa_client_start(kaa_client, &kaa_demo_add_log_record, (void *)kaa_client, KAA_DEMO_LOG_GENERATION_FREQUENCY);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to start Kaa main loop");

    /**
     * Destroy Kaa client.
     */
    kaa_client_destroy(kaa_client);

    printf("Data collection demo stopped\n");

    return error_code;
}

