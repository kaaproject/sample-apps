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

#ifndef CC32XX_SUPPORT_H_
#define CC32XX_SUPPORT_H_

/* Avoid redifined warning */
#undef FD_SETSIZE
#undef FD_SET
#undef FD_CLR
#undef FD_ISSET
#undef FD_ZERO
#undef fd_set

#include "hw_types.h"
#include "hw_ints.h"
#include "hw_memmap.h"
#include "hw_common_reg.h"
#include "rom.h"
#include "rom_map.h"
#include "interrupt.h"
#include "hw_apps_rcm.h"
#include "prcm.h"
#include "common.h"
#include "uart.h"
#include "gpio.h"
#include "timer.h"
#include "gpio_if.h"
#include "uart_if.h"
#include "udma_if.h"
#include "timer_if.h"
#include "button_if.h"
#include "pin.h"

#include "common.h"

#include "simplelink.h"

typedef struct {
    int major;
    int minor;
    int patch;
    char classifier[255];
} firmware_version_t;

void BoardInit();

void wlan_configure();
void wlan_scan();
int  wlan_connect(const char *ssid, const char *pass, unsigned char sec_type);
void net_ping(const char *host);

int update_firmware(const char *server_host, unsigned short server_port, const char *file_path, long long checksum, unsigned firmware_size);
firmware_version_t get_firmware_version();

#endif //CC32XX_SUPPORT_H_
