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
 * This header provides several bindings for TI's cc32xx target that abstracts
 * an implementation of its features. Right now it contains only
 * console and target initialisation routines, but it must be extended
 * if required.
 *
 */

#include <stdint.h>

#include "target.h"
#include "target_gpio_led.h"

#include "gpio.h"

#define HIGH 1
#define LOW 0

void target_gpio_led_init(void) {
    GPIO_OUTPUT_SET(0, LOW);
    GPIO_OUTPUT_SET(2, LOW);
}

void target_gpio_led_toggle(int id, int status) {
    if(id >= NUM_GPIO_LEDS || id < 0) {
        return;
    }
    if(status) {
        GPIO_OUTPUT_SET(id, HIGH);
    } else {
        GPIO_OUTPUT_SET(id, LOW);
    }
}
