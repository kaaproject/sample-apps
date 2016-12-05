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

#include <stdint.h>
#include <stdio.h>

#include "target.h"
#include "target_gpio_led.h"

static bool gpio_led_status[NUM_GPIO_LEDS];

void target_gpio_led_init(void)
{
    for (int n = 0; n < NUM_GPIO_LEDS; n++) {
        gpio_led_status[n] = false;
    }
}

void target_gpio_led_toggle(unsigned int id, bool status)
{
    if (id >= NUM_GPIO_LEDS) {
        return;
    }

    gpio_led_status[id] = status;
    
    demo_printf("GPIO LED status: ");
    
    for (int n = 0; n < NUM_GPIO_LEDS; n++) {
        demo_printf("%d", (int)gpio_led_status[n]);
    }
    
    demo_printf("\n");
}

bool target_gpio_led_get_state(unsigned int led)
{
    /* It is necessary to realize the reading of the list of leds */
    return false;
}

unsigned int target_gpio_led_get_count(void )
{
    return NUM_GPIO_LEDS;
}

gpio_port_t *target_get_gpio_port( unsigned int led )
{
    /* It is necessary to realize the reading of the list of leds */
    return NULL;
}
