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

#include "target.h"
#include "target_gpio_led.h"

#include "gpio.h"

#define HIGH 1
#define LOW 0

#define BIT(nr)       (1UL << (nr))

static gpio_port_t gpios[] = {
    { "LED1", 4 },
    { "LED2", 5 },
};

void target_gpio_led_init(void)
{
    GPIO_ConfigTypeDef pGPIOConfig;

    pGPIOConfig.GPIO_IntrType = GPIO_PIN_INTR_DISABLE;
    pGPIOConfig.GPIO_Mode = GPIO_Mode_Output;
    pGPIOConfig.GPIO_Pullup = GPIO_PullUp_EN;

    for (unsigned int i = 0; i < NUM_GPIO_LEDS; i++) {
        pGPIOConfig.GPIO_Pin = (BIT(gpios[i].number));
        gpio_config(&pGPIOConfig);

        GPIO_OUTPUT_SET(gpios[i].number, LOW);
    }
}

void target_gpio_led_toggle(unsigned int led, bool status)
{
    if (led >= NUM_GPIO_LEDS) {
        return;
    }

    gpios[led].state = (status) ? HIGH : LOW;

    GPIO_OUTPUT_SET(gpios[led].number, gpios[led].state);
}

bool target_gpio_led_get_state(unsigned int led)
{
    if (led >= NUM_GPIO_LEDS) {
        return 0;
    }
    return (gpios[led].state != LOW) ? true : false;
}

unsigned int target_gpio_led_get_count(void )
{
    return NUM_GPIO_LEDS;
}

gpio_port_t *target_get_gpio_port(unsigned int led)
{
    if (led >= NUM_GPIO_LEDS) {
        return NULL;
    }
    return &gpios[led];
}
