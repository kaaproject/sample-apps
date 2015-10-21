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
#include <kaa/platform/stdio.h>

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

void timerHandler()
{
    Timer_IF_InterruptClear(TIMERA0_BASE);
#ifdef DEMO_LED
    if(DEMO_LED & 0x01) GPIO_IF_LedToggle(MCU_RED_LED_GPIO);
    if(DEMO_LED & 0x02) GPIO_IF_LedToggle(MCU_ORANGE_LED_GPIO);
    if(DEMO_LED & 0x04) GPIO_IF_LedToggle(MCU_GREEN_LED_GPIO);
#endif
}

bool is_new_version(kaa_configuration_firmware_update_configuration_t *conf)
{
    kaa_string_t *classifier;
    firmware_version_t version = get_firmware_version();
    if (conf->classifier->type == KAA_PROFILE_UNION_STRING_OR_NULL_BRANCH_0)
        classifier = (kaa_string_t*)conf->classifier->data;

    if (conf->major_version != version.major ||
        conf->minor_version != version.minor ||
        strcmp(version.classifier, classifier->data))
        return true;
    return false;
}

kaa_error_t kaa_configuration_receiver(void *context, const kaa_configuration_device_configuration_t *configuration)
{
    KAA_DEMO_UNUSED(context)

    kaa_string_t *addr_str;
    char *ip_str, *port_str, *firmware_file;
    firmware_version_t version = get_firmware_version();

    DEMO_LOG("Received configuration data\r\n");

    DEMO_LOG("version [%d.%d]\r\n firmware_url %s\r\n hash %d\r\n size %d\r\n",
             configuration->firmware_update_configuration->major_version,
             configuration->firmware_update_configuration->minor_version,
             configuration->firmware_update_configuration->url->data,
             configuration->firmware_update_configuration->check_sum,
             configuration->firmware_update_configuration->size);

    if (is_new_version(configuration->firmware_update_configuration)) {

        DEMO_LOG("Upgade firmware start\r\n");

        addr_str = kaa_string_copy_create(configuration->firmware_update_configuration->url->data);
        strtok(addr_str->data, "/:/");

        ip_str        = strtok(NULL, "/:/");
        port_str      = strtok(NULL, "/:/");
        firmware_file = strtok(NULL, "/:/");

        update_firmware(ip_str, (uint16_t)atoi(port_str), firmware_file,
                        configuration->firmware_update_configuration->check_sum,
                        configuration->firmware_update_configuration->size);

        kaa_string_destroy(addr_str);
        cc32xx_reboot();
    }

    return KAA_ERR_NONE;
}

int main(/*int argc, char *argv[]*/)
{
#ifdef CC32XX
    BoardInit();

    DEMO_LOG("FIRMWARE VERSION=%d.%d[%s]\n", get_firmware_version().major, get_firmware_version().minor, get_firmware_version().classifier);

    MAP_PRCMPeripheralClkEnable(PRCM_GPIOA1, PRCM_RUN_MODE_CLK);
    MAP_PinTypeGPIO(PIN_64, PIN_MODE_0, false);
    MAP_GPIODirModeSet(GPIOA1_BASE, 0x2, GPIO_DIR_MODE_OUT);
    MAP_PinTypeGPIO(PIN_01, PIN_MODE_0, false);
    MAP_GPIODirModeSet(GPIOA1_BASE, 0x4, GPIO_DIR_MODE_OUT);
    MAP_PinTypeGPIO(PIN_02, PIN_MODE_0, false);
    MAP_GPIODirModeSet(GPIOA1_BASE, 0x8, GPIO_DIR_MODE_OUT);
    GPIO_IF_LedConfigure(LED1|LED2|LED3);
    GPIO_IF_LedOff(MCU_ALL_LED_IND);    

    MAP_PRCMPeripheralClkEnable(PRCM_GPIOA2, PRCM_RUN_MODE_CLK);
    PinTypeGPIO(PIN_04, PIN_MODE_0, false);
    GPIODirModeSet(GPIOA2_BASE, 0x20, GPIO_DIR_MODE_IN);

    DEMO_LOG("Connecting to wlan...\n");

    wlan_configure();
    sl_Start(0, 0, 0);
    wlan_connect(SSID, PWD, SL_SEC_TYPE_WPA_WPA2);

    DEMO_LOG("Connection established\n");

    Timer_IF_Init(PRCM_TIMERA0, TIMERA0_BASE, TIMER_CFG_PERIODIC, TIMER_A, 0);
    Timer_IF_IntSetup(TIMERA0_BASE, TIMER_A, timerHandler);
    Timer_IF_Start(TIMERA0_BASE, TIMER_A, 200);

#endif
    DEMO_LOG("Firmware update demo started\n");

    /**
     * Initialize Kaa client.
     */
    kaa_error_t error_code;
    firmware_version_t version;

    error_code = kaa_client_create(&kaa_client, NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed create Kaa client");

    kaa_profile_device_profile_t *profile = kaa_profile_device_profile_create();
    version = get_firmware_version();
    profile->serial_number    = 777;
    profile->model            = kaa_string_move_create("CC3200", NULL);
    profile->location         = kaa_string_move_create("US", NULL);
    profile->sensors          = kaa_list_create();
    profile->firmware_version = kaa_profile_firmware_version_create();
    profile->firmware_version->major_version = version.major;
    profile->firmware_version->major_version = version.minor;
    profile->firmware_version->classifier    = kaa_profile_union_string_or_null_branch_1_create();

    kaa_profile_sensor_t *sensor_1 = KAA_CALLOC(1, sizeof(kaa_profile_sensor_t));
    kaa_profile_sensor_t *sensor_2 = KAA_CALLOC(1, sizeof(kaa_profile_sensor_t));
    sensor_1->type = ENUM_SENSOR_TYPE_TEMPERATURE;
    sensor_1->model = kaa_string_move_create("TI LM73", NULL);
    kaa_list_push_back(profile->sensors, sensor_1);
    sensor_2->type = ENUM_SENSOR_TYPE_LIGHT;
    sensor_2->model = kaa_string_move_create(" OPT3001", NULL);
    kaa_list_push_back(profile->sensors, sensor_2);

    error_code = kaa_profile_manager_update_profile(kaa_client_get_context(kaa_client)->profile_manager, profile);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to set profile");

    profile->destroy(profile);

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
    kaa_client_destroy(kaa_client);

    return error_code;
}

