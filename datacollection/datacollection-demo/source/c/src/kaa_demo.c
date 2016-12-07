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

#include <target.h>

#include <kaa/kaa_error.h>
#include <kaa/kaa_context.h>
#include <kaa/platform/kaa_client.h>
#include <kaa_configuration_manager.h>
#include <kaa_logging.h>
#include <platform-impl/common/ext_log_upload_strategies.h>


#define DEMO_LOG_UPLOAD_THRESHOLD 5

typedef struct {
    kaa_client_t *kaa_client;
    int32_t sample_period;
    time_t last_upload_time;
} temperature_sensor_context;

static void error_cleanup(kaa_client_t *client)
{
    if (client != NULL) {
        kaa_client_stop(client);
        kaa_client_destroy(client);
    }

    exit(EXIT_FAILURE);
}

static kaa_error_t configuration_update(void *context,
        const kaa_configuration_configuration_t *configuration)
{
    if (!context || !configuration) {
        return KAA_ERR_BADPARAM;
    }

    printf("Received new sample period: %d\n", configuration->sample_period);
    temperature_sensor_context *sensor_context = context;

    if (configuration->sample_period > 0) {
        sensor_context->sample_period = configuration->sample_period;
    } else {
        demo_printf("Sample period value in updated configuration is wrong, so ignore it.\r\n");
    }
    return KAA_ERR_NONE;
}

static void send_temperature(kaa_client_t *kaa_client)
{
    int32_t temp = rand() % 10 + 25;
    int64_t timestamp = time(NULL);
    kaa_logging_data_collection_t *log_record = kaa_logging_data_collection_create();
    if (!log_record) {
        demo_printf("Failed to create log record\r\n");
        error_cleanup(kaa_client);
    }

    log_record->temperature = temp;
    log_record->time_stamp = timestamp;

    demo_printf("Sampled temperature %d %lu\n", temp, timestamp);
    kaa_error_t error = kaa_logging_add_record(
            kaa_client_get_context(kaa_client)->log_collector,
            log_record, NULL);

    if (error) {
        demo_printf("Failed to add log record, error code %d\r\n");
        error_cleanup(kaa_client);
    }

    log_record->destroy(log_record);
}

static void temperature_update(void *context)
{
    if (context == NULL) {
        return;
    }

    temperature_sensor_context *sensor_context = context;
    time_t current = time(NULL);

    if (current - sensor_context->last_upload_time >=
            sensor_context->sample_period) {
        send_temperature(sensor_context->kaa_client);
        sensor_context->last_upload_time = time(NULL);
    }
}

int main(void)
{
    /**
     * Initialise a board.
     */
    int ret = target_initialize();
    if (ret < 0) {
        /* If console is failed to initialise, you will not see this message */
        demo_printf("Failed to initialise a target\r\n");
        return 1;
    }
    
    demo_printf("Data collection demo started\r\n");

    /**
     * Initialize Kaa client.
     */
    kaa_client_t *kaa_client = NULL;
    kaa_error_t error = kaa_client_create(&kaa_client, NULL);

    if (error) {
        demo_printf("Failed to create Kaa client\r\n", error);
        return EXIT_FAILURE;
    }

    temperature_sensor_context sensor_context;
    sensor_context.kaa_client = kaa_client;
    kaa_configuration_root_receiver_t receiver = {
        &sensor_context,
        configuration_update,
    };

    error = kaa_configuration_manager_set_root_receiver(
            kaa_client_get_context(kaa_client)->configuration_manager,
            &receiver);

    if (error) {
        demo_printf("Failed to set configuiration receiver\r\n", error);
        return EXIT_FAILURE;
    }

    const kaa_configuration_configuration_t *default_configuration =
            kaa_configuration_manager_get_configuration(kaa_client_get_context(kaa_client)->configuration_manager);

    sensor_context.sample_period = default_configuration->sample_period;
    sensor_context.last_upload_time = time(NULL);

    void *log_upload_strategy_context = NULL;
    error = ext_log_upload_strategy_create(kaa_client_get_context(kaa_client),
            &log_upload_strategy_context, KAA_LOG_UPLOAD_VOLUME_STRATEGY);

    if (error) {
        demo_printf("Failed to create log upload strategy, error code %d\r\n", error);
        return EXIT_FAILURE;
    }

    error = ext_log_upload_strategy_set_threshold_count(log_upload_strategy_context,
            DEMO_LOG_UPLOAD_THRESHOLD);

    if (error) {
        demo_printf("Failed to set threshold log record count, error code %d\r\n", error);
        return EXIT_FAILURE;
    }

    error = kaa_logging_set_strategy(kaa_client_get_context(kaa_client)->log_collector,
            log_upload_strategy_context);

    if (error) {
        demo_printf("Failed to set log upload strategy, error code %d\r\n", error);
        return EXIT_FAILURE;
    }

    /**
     * Start Kaa client main loop.
     */
    error = kaa_client_start(kaa_client, temperature_update,
            &sensor_context, sensor_context.sample_period);

    if (error) {
        demo_printf("Failed to start Kaa client, error code %d\r\n", error);
        return EXIT_FAILURE;
    }

    /**
     * Destroy Kaa client.
     */
    kaa_client_destroy(kaa_client);

    return EXIT_SUCCESS;
}
