/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <wiringPi.h>

#include <stdint.h>

#define MAX_TIME    85

static int dht11_val[5] = { 0, 0, 0, 0, 0 };

int dht11_read_val(int pin, float *humidity, float *temperature)
{
    uint8_t lststate = HIGH;
    uint8_t counter = 0;
    uint8_t i = 0;
    uint8_t j = 0;

    for (i = 0; i < 5; i++) {
        dht11_val[i]=0;
    }

    pinMode(pin, OUTPUT);
    digitalWrite(pin, LOW);
    delay(18);
    digitalWrite(pin, HIGH);
    delayMicroseconds(40);
    pinMode(pin, INPUT);

    for (i = 0; i < MAX_TIME; i++) {
        counter = 0;
        while (digitalRead(pin) == lststate && (counter != 255)) {
            counter++;
            delayMicroseconds(1);
        }

        lststate=digitalRead(pin);
        if (counter == 255) {
             break;
        }

        // top 3 transistions are ignored
        if ((i >= 4) && ( i % 2 == 0)){
            dht11_val[j/8] <<= 1;
            if (counter > 16) {
                dht11_val[j / 8]|=1;
            }
            j++;
        }
    }

    // verify checksum and print the verified data
    if ((j >= 40) && (dht11_val[4] == ((dht11_val[0] + dht11_val[1] + dht11_val[2] + dht11_val[3]) & 0xFF))) {
        *humidity = (float)dht11_val[0] + (float)dht11_val[1] / 100.0;
        *temperature = (float)dht11_val[2] + (float)dht11_val[3] / 100.0;
        return 0;
    }

    return -1;
}

