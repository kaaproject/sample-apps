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
#include <stdlib.h>

#include <kaa/kaa_error.h>
#include <kaa/kaa_context.h>
#include <kaa/platform/kaa_client.h>
#include <kaa/utilities/kaa_log.h>
#include <kaa/kaa_user.h>
#include <kaa/kaa_profile.h>
#include <kaa/gen/kaa_remote_control_ecf.h>

#include "gpio.h"



#define ICACHE_FLASH_ATTR __attribute__((section(".irom0.text")))
#define ICACHE_RODATA_ATTR __attribute__((section(".irom.text")))

#define KAA_DEMO_RETURN_IF_ERROR(error, message) \
    if ((error)) { \
        printf(message ", error code %d\n", (error)); \
        return (error); \
    }

#define KAA_DEMO_UNUSED(x) (void)(x);

#define DEVICE_NAME     "ESP8266"
#define DEVICE_MODEL    "01"

#define HIGH    1
#define LOW     0


static kaa_client_t *kaa_client = NULL;
static int gpio_led[] = { 0, 0 };



ICACHE_FLASH_ATTR void kaa_device_info_request(void *context
                                             , kaa_remote_control_ecf_device_info_request_t *event
                                             , kaa_endpoint_id_p source)
{
    KAA_DEMO_UNUSED(context);
    KAA_DEMO_UNUSED(source);

    kaa_remote_control_ecf_device_info_response_t *response = kaa_remote_control_ecf_device_info_response_create();

    response->device_name = kaa_string_copy_create(DEVICE_NAME);
    response->model       = kaa_string_copy_create(DEVICE_MODEL);
    response->gpio_status = kaa_list_create();

    kaa_remote_control_ecf_gpio_status_t *gio_status = kaa_remote_control_ecf_gpio_status_create();
    gio_status->id = 0;
    gio_status->status = gpio_led[0];
    kaa_list_push_back(response->gpio_status, (void*)gio_status);

    gio_status = kaa_remote_control_ecf_gpio_status_create();
    gio_status->id = 2;
    gio_status->status = gpio_led[1];
    kaa_list_push_back(response->gpio_status, (void*)gio_status);

    kaa_event_manager_send_kaa_remote_control_ecf_device_info_response(kaa_client_get_context(kaa_client)->event_manager, response, NULL);

    response->destroy(response);
    event->destroy(event);
}

ICACHE_FLASH_ATTR void kaa_GPIOToggle_info_request(void *context
                                                 , kaa_remote_control_ecf_gpio_toggle_request_t *event
                                                 , kaa_endpoint_id_p source)
{
    KAA_DEMO_UNUSED(context);
    KAA_DEMO_UNUSED(source);

    int index = event->gpio->id;
    if (event->gpio->id == 2) {
        index = 1;
    }

    if (event->gpio->status) {
        GPIO_OUTPUT_SET(event->gpio->id, HIGH);
        gpio_led[index] = HIGH;
    } else {
        GPIO_OUTPUT_SET(event->gpio->id, LOW);
        gpio_led[index] = LOW;
    }

    event->destroy(event);
}

ICACHE_FLASH_ATTR void init_gpio()
{
    gpio_led[0] = 0;
    GPIO_OUTPUT_SET(0, LOW);

    gpio_led[1] = 0;
    GPIO_OUTPUT_SET(2, LOW);
}

int main(/*int argc, char* argv[] */)
{

    printf("\r\ESP8266 GPIO client started\n");

    init_gpio();

    /**
     * Initialize Kaa client.
     */
    kaa_error_t error_code = kaa_client_create(&kaa_client, NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed create Kaa client");

    error_code = kaa_profile_manager_set_endpoint_access_token(kaa_client_get_context(kaa_client)->profile_manager, DEMO_ACCESS_TOKEN);
    KAA_RETURN_IF_ERR(error_code);

    error_code = kaa_event_manager_set_kaa_remote_control_ecf_device_info_request_listener(kaa_client_get_context(kaa_client)->event_manager
                                                                                         , &kaa_device_info_request
                                                                                         , NULL);
    KAA_RETURN_IF_ERR(error_code);

    error_code = kaa_event_manager_set_kaa_remote_control_ecf_gpio_toggle_request_listener(kaa_client_get_context(kaa_client)->event_manager
                                                                                         , &kaa_GPIOToggle_info_request
                                                                                         , NULL);
    KAA_RETURN_IF_ERR(error_code);

    /**
     * Start Kaa client main loop.
     */
    error_code = kaa_client_start(kaa_client, NULL, NULL, 0);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to start Kaa main loop");

    /**
     * Destroy Kaa client.
     */
    kaa_client_destroy(kaa_client);

    printf("ESP8266 GPIO client stopped\n");
    return error_code;
}

