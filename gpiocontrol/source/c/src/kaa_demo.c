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

#include <kaa_error.h>
#include <kaa_context.h>
#include <platform/kaa_client.h>
#include <utilities/kaa_log.h>
#include <utilities/kaa_mem.h>
#include <gen/kaa_remote_control_ecf.h>
#include <kaa_profile.h>
#include <target_gpio_led.h>

// TODO APP-63: abstract gpio functions into separate target driver

static kaa_client_t *kaa_client = NULL;


/*
 * Event callback-s.
 */

static void kaa_device_info_request(void *context
                           , kaa_remote_control_ecf_device_info_request_t *event
                           , kaa_endpoint_id_p source)
{
    (void)context;
    (void)source;

    demo_printf("Device info request received\r\n");
    kaa_remote_control_ecf_device_info_response_t *response = kaa_remote_control_ecf_device_info_response_create();

    response->device_name = kaa_string_copy_create(TARGET_DEVICE_NAME);
    response->model       = kaa_string_copy_create(TARGET_MODEL_NAME);
    response->gpio_status = kaa_list_create();
    for (int i = 0; i < target_gpio_led_get_count(); ++i) {
        gpio_port_t *gpio_led = target_get_gpio_port( i );
        if (gpio_led) {
            kaa_remote_control_ecf_gpio_status_t *gio_status = kaa_remote_control_ecf_gpio_status_create();
            gio_status->id = i;
            gio_status->status = gpio_led->state;
            gio_status->type = kaa_string_copy_create( gpio_led->id );
            kaa_list_push_back(response->gpio_status, (void*)gio_status);
        }
    }
    kaa_error_t err = kaa_event_manager_send_kaa_remote_control_ecf_device_info_response(kaa_client_get_context(kaa_client)->event_manager, response, NULL);

    response->destroy(response); // Destroying event that was successfully sent
    event->destroy(event);
}

static void kaa_GPIOToggle_info_request(void *context
                              , kaa_remote_control_ecf_gpio_toggle_request_t *event
                              , kaa_endpoint_id_p source)
{
    (void)context;
    (void)source;

    demo_printf("Toggling GPIO...\r\n");

    target_gpio_led_toggle(event->gpio->id, event->gpio->status);

    event->destroy(event);
}


void kaa_external_process_fn(void *context)
{
    target_wifi_reconnect_if_disconected();
}

int main(void)
{
    int rc = target_initialize();
    if (rc < 0) {
        return 1;
    }

    target_gpio_led_init();

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
            kaa_device_info_request,
            NULL);
    if (error_code) {
        demo_printf("Unable to set remote control listener: %i\r\n", error_code);
        return 4;
    }

    error_code = kaa_event_manager_set_kaa_remote_control_ecf_gpio_toggle_request_listener(kaa_client_get_context(kaa_client)->event_manager,
            kaa_GPIOToggle_info_request,
            NULL);
    if (error_code) {
        demo_printf("Unable to set GPIO listener: %i\r\n", error_code);
        return 5;
    }

    demo_printf("ACCESS_TOKEN :%s\r\n", DEMO_ACCESS_TOKEN);

    /**
     * Start Kaa client main loop.
     */
    error_code = kaa_client_start(kaa_client, kaa_external_process_fn, NULL, 0);
    if (error_code) {
        demo_printf("Unable to start Kaa client: %i\r\n", error_code);
        return 6;
    }

    /**
     * Destroy Kaa client.
     */
    kaa_client_destroy(kaa_client);

    demo_printf("GPIO demo stopped\r\n");

    return error_code;
}

