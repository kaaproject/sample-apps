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
#include <kaa_user.h>
#include <kaa_profile.h>
#include <gen/kaa_verifiers_demo_event_class_family.h>
#include <platform/ext_key_utils.h>

static kaa_client_t *kaa_client = NULL;


void kaa_message_receive(void *context,
                            kaa_verifiers_demo_event_class_family_message_event_t *event,
                            kaa_endpoint_id_p source)
{
    (void)context;
    (void)source;

    printf("Message was received!\n");

    if (event->message->type == KAA_VERIFIERS_DEMO_EVENT_CLASS_FAMILY_UNION_STRING_OR_NULL_BRANCH_0) {
        char *message = event->message->data;
        printf("Message: %s\n", message);
    }

    event->destroy(event); 
}

int main(void)
{
    printf("User verifier demo started\n");

    /**
     * Initialize Kaa client.
     */
    kaa_error_t error_code = kaa_client_create(&kaa_client, NULL);
    if (error_code) {
        printf("Failed to create Kaa client, error code %d\n", error_code);
        return EXIT_FAILURE;
    }
    
    error_code = kaa_profile_manager_set_endpoint_access_token(
                        kaa_client_get_context(kaa_client)->profile_manager,
                        DEMO_ACCESS_TOKEN);
    if (error_code) {
        printf("Failed to set access token, error code %d\n", error_code);
        kaa_client_destroy(kaa_client);
        return EXIT_FAILURE;
    }
    printf("Endpoint access token: %s\n", DEMO_ACCESS_TOKEN);
    
    error_code = kaa_event_manager_set_kaa_verifiers_demo_event_class_family_message_event_listener(
                    kaa_client_get_context(kaa_client)->event_manager,
                    kaa_message_receive,
                    NULL);
    if (error_code) {
        printf("Failed to set message event listener\n");
        kaa_client_destroy(kaa_client);
        return EXIT_FAILURE;
    }

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
