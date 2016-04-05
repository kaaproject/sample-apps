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
#include <kaa/utilities/kaa_mem.h>
#include <kaa/kaa_user.h>
#include <kaa/gen/kaa_remote_control_ecf.h>
#include <kaa/kaa_profile.h>

#include "target.h"

// TODO APP-63: abstract gpio functions into separate target driver

static kaa_client_t *kaa_client = NULL;

static int gpio_led[] = { 0, 0, 0 };
static int led_number = sizeof (gpio_led) / sizeof (int);

/*
 * Event callback-s.
 */

void kaa_device_info_request(void *context
                           , kaa_remote_control_ecf_device_info_request_t *event
                           , kaa_endpoint_id_p source)
{
    (void)context;
    (void)source;

    kaa_remote_control_ecf_device_info_response_t *response = kaa_remote_control_ecf_device_info_response_create();

    response->device_name = kaa_string_copy_create("CC3200");
    response->model       = kaa_string_copy_create("LaunchPad");
    response->gpio_status = kaa_list_create();

    int i = 0;
    for (i = 0; i < led_number; ++i) {
        kaa_remote_control_ecf_gpio_status_t *gio_status = kaa_remote_control_ecf_gpio_status_create();
        gio_status->id = i;
        gio_status->status = gpio_led[i];
        kaa_list_push_back(response->gpio_status, (void*)gio_status);
    }

    kaa_event_manager_send_kaa_remote_control_ecf_device_info_response(kaa_client_get_context(kaa_client)->event_manager, response, NULL);

    response->destroy(response); // Destroying event that was successfully sent
    event->destroy(event);
}

void kaa_GPIOToggle_info_request(void *context
                              , kaa_remote_control_ecf_gpio_toggle_request_t *event
                              , kaa_endpoint_id_p source)
{
    (void)context;
    (void)source;

    if (event->gpio->status) {
        GPIO_IF_LedOn(MCU_RED_LED_GPIO + event->gpio->id);
        gpio_led[event->gpio->id] = 1;
    } else {
        GPIO_IF_LedOff(MCU_RED_LED_GPIO + event->gpio->id);
        gpio_led[event->gpio->id] = 0;
    }

    event->destroy(event);
}

int main(void)
{

    int rc = target_initialise();
    if (rc < 0) {
        return 1;
    }

    demo_printf("GPIO demo started\r\n");

    /**
     * Initialize Kaa client.
     */
    kaa_error_t error_code = kaa_client_create(&kaa_client, NULL);
    if (error_code) {
        demo_printf("Failed to create client context: %i\r\n", error_code);
        return 2;
    }

    error_code = kaa_profile_manager_set_endpoint_access_token(kaa_client_get_context(kaa_client)->profile_manager,
            DEMO_ACCESS_TOKEN);

    if (error_code) {
        demo_printf("Failed to set access token: %i\r\n", error_code);
        return 3;
    }


    error_code = kaa_event_manager_set_kaa_remote_control_ecf_device_info_request_listener(kaa_client_get_context(kaa_client)->event_manager,
                                                                                          &kaa_device_info_request,
                                                                                          NULL);
    if (error_code) {
        demo_printf("Unable to set remote control listener: %i\r\n", error_code);
        return 4;
    }

    /**
     * Start Kaa client main loop.
     */
    error_code = kaa_client_start(kaa_client, NULL, NULL, 0);
    if (error_code) {
        demo_printf("Unable to start Kaa client: %i\r\n", error_code);
        return 5;
    }

    /**
     * Destroy Kaa client.
     */
    kaa_client_destroy(kaa_client);

    demo_printf("GPIO demo stopped\r\n");

    return error_code;
}

