/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


#ifndef DHT11_H_
#define DHT11_H_

#ifdef __cplusplus
extern "C" {
#endif

int dht11_read_val(int pin, float *humidity, float *temperature);

#ifdef __cplusplus
}      /* extern "C" */
#endif

#endif /* DHT11_H_ */
