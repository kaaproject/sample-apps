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
#include <kaa/kaa_configuration_manager.h>
#include <kaa/gen/kaa_profile_gen.h>
#include <kaa/gen/kaa_configuration_gen.h>
#include <kaa/kaa_profile.h>
#include <kaa/platform/time.h>

#include <kaa/platform-impl/cc32xx/cc32xx_file_utils.h>

#ifdef CC32XX
#include "../platform/cc32xx/cc32xx_support.h"

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

int update = 0;
void button_hdl()
{
    DEMO_LOG("push button\r\n");    
    update = 1;
    Button_IF_EnableInterrupt(SW3);
}

kaa_error_t kaa_configuration_receiver(void *context, const kaa_configuration_device_configuration_t *configuration)
{
    //KAA_LOG_TRACE(kaa_client_get_context(kaa_client)->logger, KAA_ERR_NONE, "Received configuration data");

    DEMO_LOG("\r\nNEW_CONFIG\r\n");

    //configuration->firmware_update_configuration

    //update_firmware(configuration->server_address->data, configuration->firmware_file_path->data, configuration->firmware_checksum);

    return KAA_ERR_NONE;
}

int main(/*int argc, char *argv[]*/)
{
#ifdef CC32XX
    BoardInit();

    DEMO_LOG("BUGSBUGSBUGS V=0.3\n");

    MAP_PRCMPeripheralClkEnable(PRCM_GPIOA1, PRCM_RUN_MODE_CLK);
    MAP_PinTypeGPIO(PIN_64, PIN_MODE_0, false);
    MAP_GPIODirModeSet(GPIOA1_BASE, 0x2, GPIO_DIR_MODE_OUT);
    MAP_PinTypeGPIO(PIN_01, PIN_MODE_0, false);
    MAP_GPIODirModeSet(GPIOA1_BASE, 0x4, GPIO_DIR_MODE_OUT);
    MAP_PinTypeGPIO(PIN_02, PIN_MODE_0, false);
    MAP_GPIODirModeSet(GPIOA1_BASE, 0x8, GPIO_DIR_MODE_OUT);
    GPIO_IF_LedConfigure(LED1|LED2|LED3);
    GPIO_IF_LedOff(MCU_ALL_LED_IND);

    DEMO_LOG("Step1\n");

    MAP_PRCMPeripheralClkEnable(PRCM_GPIOA2, PRCM_RUN_MODE_CLK);
    PinTypeGPIO(PIN_04, PIN_MODE_0, false);
    GPIODirModeSet(GPIOA2_BASE, 0x20, GPIO_DIR_MODE_IN);

    DEMO_LOG("Step2\n");

    wlan_configure();
    sl_Start(0, 0, 0);
//    //wlan_connect(SSID, PWD, SL_SEC_TYPE_WPA_WPA2);
    wlan_connect("KaaIoT", "cybervision2015", SL_SEC_TYPE_WPA_WPA2);
    //wlan_connect("cyber9", "Cha5hk123", SL_SEC_TYPE_WPA_WPA2);


    DEMO_LOG("Step3\n");

    unsigned t = 0;
    if (update_sys_time(&t) == 0) {
        t += 3 * 60 * 60;//set timezone to +3
        //set_sys_time(t);
    }

    DEMO_LOG("Step4\n");

    cc32xx_binary_file_delete("kaa_status.bin");
    cc32xx_binary_file_delete("kaa_configuration.bin");

    //==================================================
    Button_IF_Init(button_hdl, button_hdl);
    Button_IF_EnableInterrupt(SW3);
    //==================================================

#endif
    DEMO_LOG("Event demo started\n");

    /**
     * Initialize Kaa client.
     */
    kaa_error_t error_code;
    firmware_version_t version;

    error_code = kaa_client_create(&kaa_client, NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed create Kaa client");


    kaa_profile_device_profile_t *profile = kaa_profile_device_profile_create();
    version = get_firmware_version();
    profile->serial_number = 12345;
    profile->model         = kaa_string_copy_create("CC3200");
    profile->location      = kaa_string_copy_create("UK");
    profile->sensors       = kaa_list_create();
    profile->firmware_version = kaa_profile_firmware_version_create();
    profile->firmware_version->major_version = version.major;
    profile->firmware_version->major_version = version.minor;
    profile->firmware_version->classifier = kaa_profile_union_string_or_null_branch_0_create();
    error_code = kaa_profile_manager_update_profile(kaa_client_get_context(kaa_client)->profile_manager, profile);

    kaa_configuration_root_receiver_t receiver = { NULL, &kaa_configuration_receiver };
    error_code = kaa_configuration_manager_set_root_receiver(kaa_client_get_context(kaa_client)->configuration_manager, &receiver);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to add configuration receiver");

    /**
     * Start Kaa client main loop.
     */    

    error_code = kaa_client_start(kaa_client, NULL, NULL, 0);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to start Kaa main loop");

    /**
     * Destroy Kaa client.
     */
    //profile->destroy(profile);
    kaa_client_destroy(kaa_client);

//    DEMO_LOG("Event demo stopped\n");

    while(1)
    {
        if(update)
        {
            update_firmware("10.2.2.203", 8080, "/demo_client"/*"/firmwares/CC32XX"*/, 2249941899, 113980);
            update = 0;
        }
        _SlNonOsMainLoopTask();
        MAP_UtilsDelay(1000);
    }

    return error_code;
}

