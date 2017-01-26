/*
 *  Copyright 2014-2017 CyberVision, Inc.
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

#include "interger_math.h"

int32_t integer_sin(int32_t degrees, int32_t A)
{
    int sign = (degrees >= 0) ? 1 : -1;
    degrees *= sign;
    degrees %= 180;
    if (sign == -1) {
        degrees = 180 - degrees;
    }
    return A * degrees * (180 - degrees) / (10125 - degrees * (180 - degrees) / 4);
}

