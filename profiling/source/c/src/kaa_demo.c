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

#include <stdio.h>
#include <time.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <unistd.h>

#include <kaa_error.h>
#include <platform/kaa_client.h>
#include <kaa_configuration_manager.h>
#include <kaa_profile.h>
#include <platform/ext_key_utils.h>

static kaa_client_t *kaa_client = NULL;

void kaa_demo_print_configuration_message(const kaa_root_configuration_t *configuration)
{
    printf("%s - %s - %s\n", configuration->audio_subscription_active ? "true" : "false",
        configuration->video_subscription_active ? "true" : "false",
        configuration->vibro_subscription_active ? "true" : "false");
}

kaa_error_t kaa_demo_configuration_receiver(void *context,
                                            const kaa_root_configuration_t *configuration)
{
    (void)context;
    
    const uint8_t *endpoint_key_hash = NULL;
    size_t endpoint_key_hash_length = 0;

    ext_get_sha1_base64_public(&endpoint_key_hash, &endpoint_key_hash_length);

    printf("- - -\n");
    printf("Endpoint Key Hash %.*s\n", (int)endpoint_key_hash_length,
                endpoint_key_hash);

    printf("Configuration was succesfully edited:\n");
    kaa_demo_print_configuration_message(configuration);
    kaa_client_stop(kaa_client);
    return KAA_ERR_NONE;
}

bool set_client_parameter(char *parameter)
{
    if (strcmp(parameter, "+") == 0) {
        return true;
    }
    return false;
}

int main(int argc, char *argv[])
{   
    if (argc != 5) {
        printf("Please, input + or - for audio, video and vibro supporting and directory for key storage. (Need 4 arguments)\n");
        exit(1);
    }
    
    mkdir(argv[4], 0777);
    chdir(argv[4]);

    /**
     * Initialize Kaa client.
     */
    kaa_error_t error_code = kaa_client_create(&kaa_client, NULL);
    if (error_code) {
        printf("Failed create Kaa client, error code %d\n", error_code);
        return EXIT_FAILURE;
    }
    
    kaa_profile_t *profile = kaa_profile_pager_client_profile_create();
    if (!profile) {
        printf("Failed to create profile\n");
        kaa_client_destroy(kaa_client);
        return EXIT_FAILURE;
    }
    
    const uint8_t *endpoint_key_hash = NULL;
    size_t endpoint_key_hash_length = 0;

    ext_get_sha1_base64_public(&endpoint_key_hash, &endpoint_key_hash_length);

    printf("- - -\n");
    printf("Endpoint Key Hash %.*s\n", (int)endpoint_key_hash_length,
                endpoint_key_hash);


    profile->audio_support = set_client_parameter(argv[1]);
    profile->video_support = set_client_parameter(argv[2]);
    profile->vibro_support = set_client_parameter(argv[3]);

    printf("Profiling body (have audio-, video-, vibro-support):\n");
    printf("%s - %s - %s\n", profile->audio_support ? "true" : "false",
        profile->video_support ? "true" : "false",
        profile->vibro_support ? "true" : "false");

    error_code = kaa_profile_manager_update_profile(kaa_client_get_context(kaa_client)->profile_manager, profile);
    if (error_code) {
        printf("Failed to update profile, error code %d", error_code);
        kaa_client_destroy(kaa_client);
        return EXIT_FAILURE;
    }

    kaa_configuration_root_receiver_t receiver = {
        NULL,
        &kaa_demo_configuration_receiver,
    };

    error_code = kaa_configuration_manager_set_root_receiver(
        kaa_client_get_context(kaa_client)->configuration_manager,
        &receiver);
    if (error_code) {
        printf("Failed to add configuration receiver\n");
        kaa_client_destroy(kaa_client);
        return EXIT_FAILURE;
    }
    printf("Default configuration:\n");
    kaa_demo_print_configuration_message(
        kaa_configuration_manager_get_configuration(
            kaa_client_get_context(kaa_client)->configuration_manager));

    /**
     * Start Kaa client main loop.
     */
    error_code = kaa_client_start(kaa_client, NULL, NULL, 0);
    if (error_code) {
        printf("Failed to start Kaa main loop, error code %d\n", error_code);
        kaa_client_destroy(kaa_client);
        return EXIT_FAILURE;
    }
    
    /**
     * Destroy Kaa client.
     */
    kaa_client_destroy(kaa_client);

    return EXIT_SUCCESS;
}
