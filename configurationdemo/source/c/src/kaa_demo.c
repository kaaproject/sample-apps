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
#include <kaa/utilities/kaa_log.h>
#include <kaa_configuration_manager.h>
#include <platform/ext_key_utils.h>

static kaa_client_t *kaa_client = NULL;

void kaa_demo_print_configuration_message(
        const kaa_root_configuration_t *configuration) {
    demo_printf("Sample period is now %d second(s)\r\n", configuration->sample_period);
}

kaa_error_t kaa_demo_configuration_receiver(void *context,
                                            const kaa_root_configuration_t *configuration) {
    (void) context;
    demo_printf("Received configuration data\r\n");
    kaa_demo_print_configuration_message(configuration);
    kaa_client_stop(kaa_client);
    return KAA_ERR_NONE;
}

int main(/*int argc, char *argv[]*/) {
    /**
     * Initialise a board
     */
    int ret = target_initialize();
    if (ret < 0) {
        /* If console is failed to initialise, you will not see this message */
        demo_printf("Failed to initialise a target\r\n");
        return EXIT_FAILURE;
    }

    demo_printf("Configuration demo started\r\n");

    /**
     * Initialize Kaa client.
     */
    kaa_error_t error_code = kaa_client_create(&kaa_client, NULL);
    if (error_code) {
        demo_printf("Failed create Kaa client\r\n");
        return EXIT_FAILURE;
    }

    /*
     * Set the handler for configuration updates.
     */
    kaa_configuration_root_receiver_t receiver = {
            NULL,
            &kaa_demo_configuration_receiver
    };

    error_code = kaa_configuration_manager_set_root_receiver(
            kaa_client_get_context(kaa_client)->configuration_manager,
            &receiver);

    if (error_code) {
        demo_printf("Failed to add configuration receiver\r\n");
        return EXIT_FAILURE;
    }

    /*
     * Display default configuration.
     */
    kaa_demo_print_configuration_message(
            kaa_configuration_manager_get_configuration(
                    kaa_client_get_context(kaa_client)->configuration_manager));

    /*
     * Obtain and display Endpoint Key Hash.
     */
    const uint8_t *endpoint_key_hash = NULL;
    size_t endpoint_key_hash_length = 0;

    ext_get_sha1_base64_public(&endpoint_key_hash, &endpoint_key_hash_length);

    printf("Endpoint Key Hash: %.*s\r\n", (int)endpoint_key_hash_length,
            endpoint_key_hash);

    /**
     * Start Kaa client main loop.
     */
    error_code = kaa_client_start(kaa_client, NULL, NULL, 0);
    if(error_code) {
        demo_printf("Failed to start Kaa main loop\r\n");
        return EXIT_FAILURE;
    }

    /**
     * Destroy Kaa client.
     */
    kaa_client_destroy(kaa_client);

    return EXIT_SUCCESS;
}

