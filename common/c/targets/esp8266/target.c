/*
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

#include <string.h>

#include <freertos/FreeRTOS.h>
#include <freertos/task.h>

#include <lwip/mem.h>

#include <espressif/esp_wifi.h>
#include <espressif/esp_sta.h>

#include "uart.h"

#define MAIN_STACK_SIZE 512

extern int main(void);

static void main_task(void *pvParameters);

static int wifi_init(void);
static int wifi_connect(const char *ssid, const char *pwd);
static void wifi_print_opmode(void);
static void wifi_print_station_config(void);

static void uart_init(void);

int target_initialize(void)
{
    if (wifi_init()) {
        printf("Error initialising wifi!\r\n");
        return 1;
    }

    if (wifi_connect(WIFI_SSID,WIFI_PASSWORD)) {
        printf("Couldn't connect to \"%s\" with password \"%s\"\r\n",WIFI_SSID, WIFI_PASSWORD);
        return 1;
    }

    return 0;
}

void ICACHE_FLASH_ATTR user_init()
{
    uart_init();

    portBASE_TYPE error = xTaskCreate(main_task, (const signed char *)"main_task",
            MAIN_STACK_SIZE, NULL, 2, NULL );
    if (error < 0) {
        printf("Error creating main_task! Error code: %ld\r\n", error);
    }
}

static void main_task(void *pvParameters)
{
    (void)pvParameters;
    printf("main_task() started\r\n");
    int ret = main();
    printf("main() exited with %d\r\n", ret);
    for (;;);
}


static void uart_init(void)
{
    uart_init_new();
    UART_SetBaudrate(UART0, 115200);
    UART_SetPrintPort(UART0);
}

static int wifi_init(void)
{
    printf("\r\nInitialising wifi station\r\n");
    wifi_print_opmode();

    if (!wifi_set_opmode_current(0x01)) {
        printf("Error setting wifi opmode to station mode!\r\n");
        return 1;
    }

    printf("Changed wifi mode to station\r\n");
    return 0;
}

static int wifi_connect(const char *ssid, const char *pwd)
{
    struct station_config sta_cfg;
    memset(&sta_cfg, 0, sizeof(sta_cfg));
    strcpy((char *)sta_cfg.ssid, ssid);
    strcpy((char *)sta_cfg.password, pwd);
    if (!wifi_station_set_config_current(&sta_cfg)) {
        printf("Error setting wifi station config!\r\n");
        return 1;
    }

    wifi_print_station_config();
    printf("Connecting to %s...\r\n ", ssid);
    if (!wifi_station_connect()) {
        printf("FAIL!\r\n");
        return 0;
    }

    uint8 status;
    do {
        status = wifi_station_get_connect_status();
    } while (status == STATION_CONNECTING);

    switch (status) {
        case STATION_WRONG_PASSWORD:
            printf("Error connecting to \"%s\": wrong password!\r\n", ssid);
            goto conn_error;
        case STATION_NO_AP_FOUND:
            printf("Error connecting to \"%s\": no AP found with this ssid\r\n", ssid);
            goto conn_error;
        case STATION_CONNECT_FAIL:
            printf("Failed to connect\r\n");
            goto conn_error;
        case STATION_GOT_IP:
        case STATION_IDLE:
            printf("OK\r\n");
            return 0;
        default:
            printf("Connection status: %d\r\n", status);
            return 0;
    }
conn_error:
    wifi_station_disconnect();
    return 1;
}

static void wifi_print_opmode(void)
{
    uint8 opmode = wifi_get_opmode();
    switch (opmode) {
        case STATION_MODE: /* Station mode */
            printf("Current wifi opmode is station\r\n");
            break;
        case SOFTAP_MODE: /* soft-AP */
            printf("Current wifi opmode is soft-AP\r\n");
            break;
        case STATIONAP_MODE: /* station+soft-AP */
            printf("Current wifi opmode is station+soft-AP\r\n");
            break;
        case MAX_MODE: /* station+soft-AP */
            printf("Current wifi opmode is max mode\r\n");
            break;
        default:
            printf("Error getting wifi opmode!\r\n");
    }
}

static void wifi_print_station_config(void)
{
    struct station_config sta_cfg;
    if (!wifi_station_get_config(&sta_cfg)) {
        printf("Error getting current wifi station config!\r\n");
        return;
    }

    printf("Current wifi station conifguration:\r\n\tssid: %s\r\n\tpassword: %s\r\n\tbssid_set: %s\r\n",
           sta_cfg.ssid,sta_cfg.password, sta_cfg.bssid_set?"yes":"no");
}

/* Required, don't touch */
void ets_putc(char c)
{
    os_putc(c);
}

int getchar(void)
{
    return uart_getchar();
}

