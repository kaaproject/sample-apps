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

#include <stdlib.h>

#include <kaa_error.h>
#include <kaa_context.h>
#include <platform/kaa_client.h>
#include <utilities/kaa_log.h>
#include <kaa_channel_manager.h>
#include <platform/stdio.h>

static void auth_failure_handler(kaa_auth_failure_reason reason, void *context)
{
    printf("Authorization failed! Reason: ");
    switch (reason) {
        case KAA_AUTH_STATUS_VERIFICATION_FAILED:
            printf("Verification failed\n");
            break;
        case KAA_AUTH_STATUS_BAD_CREDENTIALS:
            printf("Bad credentials\n");
            break;
        default:
            printf("Unknown reason\n");
        break;
    }

    kaa_client_stop((kaa_client_t*)context);
}

int main(/*int argc, char *argv[]*/)
{
    /**
     * Initialize Kaa client.
     */
    kaa_client_t *kaa_client = NULL;

    kaa_error_t error_code = kaa_client_create(&kaa_client, NULL);
    if (error_code) {
        printf("Failed create Kaa client\r\n");
        return EXIT_FAILURE;
    }

    /**
     * Specify authorization failure handler.
     */
    kaa_channel_manager_set_auth_failure_handler(
            kaa_client_get_context(kaa_client)->channel_manager,
            auth_failure_handler, kaa_client);

    /**
     * Start Kaa client main loop.
     */
    error_code = kaa_client_start(kaa_client, NULL, NULL, 0);
    if (error_code) {
        printf("Failed to start Kaa main loop\r\n");
        kaa_client_destroy(kaa_client);
        return EXIT_FAILURE;
    }

    /**
     * Destroy Kaa client.
     */
    kaa_client_destroy(kaa_client);

    return EXIT_SUCCESS;
}

