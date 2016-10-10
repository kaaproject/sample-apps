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

/**
 * @file
 *
 * This header provides several bindings for ESP8266 target that abstracts
 * an implementation of its features. Right now it contains only
 * console and target initialisation routines, but it can be extended
 * if required.
 *
 */

#ifndef ESP8266_SUPPORT_H_
#define ESP8266_SUPPORT_H_

#include <stdio.h>
#include <stdlib.h>

/* Demo print routine. printf implementation is available on this platform. */
#define demo_printf(msg, ...) printf((msg), ##__VA_ARGS__)

#define NUM_GPIO_LEDS 2

#define TARGET_DEVICE_NAME "ESP8266"
#define TARGET_MODEL_NAME "01"

/**
 * Initializes a target. 0 means success, negative values - errors.
 *
 * For this particular target this will eventually try to connect to
 * the WiFi spot using SSID and password supplied during build.
 */
int target_initialize(void);

/**
 * The ESP8266 SDK does not define getchar() anywhere
 * (except for useless chain of defines which does not work, however),
 * but it is required to read user input in some demos,
 * so it is implemented here.
 */
#undef getchar
int getchar(void);

#endif //ESP8266_SUPPORT_H_
