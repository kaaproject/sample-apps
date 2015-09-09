/*
 * Copyright 2014-2015 CyberVision, Inc.
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

#include <kaa/kaa_error.h>
#include <kaa/kaa_context.h>
#include <kaa/platform/kaa_client.h>
#include <kaa/utilities/kaa_log.h>
#include <kaa/utilities/kaa_mem.h>
#include <kaa/kaa_user.h>
#include <kaa/gen/kaa_remote_control_ecf.h>

#ifdef CC32XX
#include "../cc32xx/cc32xx_support.h"

#define KAA_DEMO_RETURN_IF_ERROR(error, message) \
    if ((error)) { \
        UART_PRINT(message ", error code %d\r\n", (error)); \
        return (error); \
    }
#define DEMO_LOG(msg, ...) UART_PRINT(msg "\r", ##__VA_ARGS__);
#else
#define KAA_DEMO_RETURN_IF_ERROR(error, message) \
    if ((error)) { \
        printf(message ", error code %d\n", (error)); \
        return (error); \
    }
#define DEMO_LOG(msg, ...) printf(msg, ##__VA_ARGS__);
#endif

static kaa_client_t *kaa_client = NULL;
static bool is_shutdown = false;

static int gpio_led[] = { 0, 0, 0 };
static int led_number = sizeof (gpio_led) / sizeof (int);

#define KAA_DEMO_UNUSED(x) (void)(x);

/*
 * Event callback-s.
 */

void kaa_device_info_request(void *context
                           , kaa_remote_control_ecf_device_info_request_t *event
                           , kaa_endpoint_id_p source)
{
    KAA_DEMO_UNUSED(context);
    KAA_DEMO_UNUSED(source);

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
    KAA_DEMO_UNUSED(context);
    KAA_DEMO_UNUSED(source);

    if (event->gpio->status) {
        GPIO_IF_LedOn(MCU_RED_LED_GPIO + event->gpio->id);
        gpio_led[event->gpio->id] = 1;
    } else {
        GPIO_IF_LedOff(MCU_RED_LED_GPIO + event->gpio->id);
        gpio_led[event->gpio->id] = 0;
    }

    event->destroy(event);
}

int main(/*int argc, char *argv[]*/)
{
#ifdef CC32XX
    BoardInit();

    MAP_PRCMPeripheralClkEnable(PRCM_GPIOA1, PRCM_RUN_MODE_CLK);
    MAP_PinTypeGPIO(PIN_64, PIN_MODE_0, false);
    MAP_GPIODirModeSet(GPIOA1_BASE, 0x2, GPIO_DIR_MODE_OUT);
    MAP_PinTypeGPIO(PIN_01, PIN_MODE_0, false);
    MAP_GPIODirModeSet(GPIOA1_BASE, 0x4, GPIO_DIR_MODE_OUT);
    MAP_PinTypeGPIO(PIN_02, PIN_MODE_0, false);
    MAP_GPIODirModeSet(GPIOA1_BASE, 0x8, GPIO_DIR_MODE_OUT);
    GPIO_IF_LedConfigure(LED1|LED2|LED3);
    GPIO_IF_LedOff(MCU_ALL_LED_IND);

    wlan_configure();
    sl_Start(0, 0, 0);
    wlan_connect("cyber9", "Cha5hk123", SL_SEC_TYPE_WPA_WPA2);
#endif
    DEMO_LOG("Event demo started\n");

    /**
     * Initialize Kaa client.
     */
    kaa_error_t error_code = kaa_client_create(&kaa_client, NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed create Kaa client");

    error_code = kaa_user_manager_default_attach_to_user(kaa_client_get_context(kaa_client)->user_manager
                                                       , "2"
                                                       , "12345");
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

    DEMO_LOG("Event demo stopped\n");

    return error_code;
}

