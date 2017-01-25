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

/*
 * This header provides several bindings for posix target that abstracts
 * an implementation of its features. Right now it contains only
 * console and target initialisation routines, but it must be extended
 * if required.
 *
 * Note that posix target implies a system with support of
 * libc library and most likely running full-featured OS.
 *
 */
#ifndef POSIX_SUPPORT_H
#define POSIX_SUPPORT_H

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <stdint.h>
#include <time.h>
#include <math.h>

/* Demo print routine. Default printf exists for posix target. */
#define demo_printf(msg, ...) printf((msg), ##__VA_ARGS__)

#define NUM_GPIO_LEDS 4

#define TARGET_DEVICE_NAME "POSIX"
#define TARGET_MODEL_NAME "01"

/* Initialises a target. Zero value means success, negative - errors. */
static inline int target_initialize(void)
{
    /* This target do not require special initialisation. */
    return 0;
}

static inline int target_wifi_reconnect_if_disconected(void)
{
    return 0;
}

static inline int32_t target_get_temperature(void)
{
    static const float PI = 3.141592653;
    float random_real = rand()/(float)RAND_MAX;
    return (0.6f * (random_real - 0.5) + sin((2 * PI / 90) * time(NULL))) * 25 + 15;
}

#endif // POSIX_SUPPORT_H
