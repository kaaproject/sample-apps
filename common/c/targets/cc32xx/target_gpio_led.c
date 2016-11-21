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

#include "target_gpio_led.h"
#include "target.h"

#include "gpio.h"
#include "gpio_if.h"
#include "pin.h"
#include "prcm.h"
#include "hw_memmap.h"
#include "rom_map.h"


enum {
	push_pull_OpenDrain = true,
	push_pull_STD       = false
};

 //  Look in http://www.ti.com/product/CC3200/datasheet/terminal_configuration_and_functions
static gpio_port_t gpios[] = {
		{ "RED",   9,  PIN_64, push_pull_STD },
		{ "YELOW", 10, PIN_01, push_pull_STD },
		{ "GREEN", 11, PIN_02, push_pull_STD },
		{ "RED2",  12, PIN_03, push_pull_STD },
		{ "RED3",  13, PIN_04, push_pull_STD },
};

#define countof(array) sizeof(array)/sizeof(array[0])

size_t gpios_size = countof(gpios);


void target_gpio_led_init(void)
{
    MAP_PRCMPeripheralClkEnable(PRCM_GPIOA1, PRCM_RUN_MODE_CLK);

    for(int i =0; i<countof(gpios); i++ ){
        MAP_PinTypeGPIO(gpios[i].pin_spec, PIN_MODE_0, gpios[i].OpenDrain);
        MAP_GPIODirModeSet(GPIOA1_BASE, (0x2<<i), GPIO_DIR_MODE_OUT);

        GPIO_IF_GetPortNPin(gpios[i].number, &gpios[i].port, &gpios[i].bit );
        gpios[i].state = 0;
        GPIO_IF_Set(gpios[i].number, gpios[i].port, gpios[i].bit, gpios[i].state);
    }

}

void target_gpio_led_toggle(unsigned int led, bool status)
{
    if (led >= countof(gpios)) {
        return;
    }

    gpios[led].state = (status == true) ? 1 : 0;
	GPIO_IF_Set(gpios[led].number, gpios[led].port, gpios[led].bit, gpios[led].state);
}

bool target_gpio_led_get_state(unsigned int led)
{
    if (led >= countof(gpios)) {
        return 0;
    }
	return (gpios[led].state != 0) ? true : false;
}


unsigned int target_gpio_led_get_count(void )
{
	return countof(gpios);
}

gpio_port_t *target_get_gpio_port( unsigned int led )
{
    if (led >= countof(gpios)) {
        return NULL;
    }
	return &gpios[led];
}


