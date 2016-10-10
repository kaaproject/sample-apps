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
 * This header defines generic API to access
 * GPIO LEDs on embedded targets.
 */

#ifndef TARGET_GPIO_LED_H
#define TARGET_GPIO_LED_H

#include <stdbool.h>
#include <stdint.h>

/**
 * Platform-specific GPIO LEDs initalization.
 *
 */
void target_gpio_led_init(void);

/**
 * Toggles output for GPIO LED id.
 *
 * @param [in] id Id of the LED to toggle. Should be less that NUM_GPIO_LEDS.
 * @param [in] status Sets the status of the LED.
 *
 */
void target_gpio_led_toggle(uint32_t id, bool status);

#endif // TARGET_GPIO_LED_H

