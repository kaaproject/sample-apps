#
#  Copyright 2014-2016 CyberVision, Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

cmake_minimum_required(VERSION 3.0.2)

if(NOT DEFINED ESP_RTOS_SDK)
    set(ESP_RTOS_SDK /opt/Espressif/esp-rtos-sdk)
endif()

exec_program(xtensa-lx106-elf-gcc .
            ARGS -print-libgcc-file-name
            OUTPUT_VARIABLE ESP8266_LIBGCC)

add_library(target_support
            STATIC
            target.c
            target_gpio_led.c
            driver/uart.c
            driver/gpio.c)

target_include_directories(target_support PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/../common)
target_include_directories(target_support PUBLIC driver)
target_include_directories(target_support PUBLIC .)
target_include_directories(target_support PUBLIC
                           ${ESP_RTOS_SDK}/extra_include
                           ${ESP_RTOS_SDK}/include
                           ${ESP_RTOS_SDK}/include/lwip
                           ${ESP_RTOS_SDK}/include/lwip/ipv4
                           ${ESP_RTOS_SDK}/include/lwip/ipv6
                           ${ESP_RTOS_SDK}/include/espressif/)
# specify linker script
configure_file(${CMAKE_CURRENT_SOURCE_DIR}/ld/eagle.rom.addr.v6.ld ${CMAKE_BINARY_DIR})
configure_file(${CMAKE_CURRENT_SOURCE_DIR}/ld/eagle.app.v6.ld ${CMAKE_BINARY_DIR})

# ESP8266 RTOS SDK dependencies
target_link_libraries(target_support PUBLIC
                      ${ESP_RTOS_SDK}/lib/libfreertos.a
                      ${ESP_RTOS_SDK}/lib/libhal.a
                      ${ESP_RTOS_SDK}/lib/libpp.a
                      ${ESP_RTOS_SDK}/lib/libphy.a
                      ${ESP_RTOS_SDK}/lib/libnet80211.a
                      ${ESP_RTOS_SDK}/lib/libwpa.a
                      ${ESP_RTOS_SDK}/lib/liblwip.a
                      ${ESP_RTOS_SDK}/lib/libmain.a
                      ${ESP_RTOS_SDK}/lib/libssl.a
                      ${ESP8266_LIBGCC}
                      -T${CMAKE_CURRENT_SOURCE_DIR}/ld/eagle.app.v6.ld
                      )

message(STATUS "WiFi AP: ${WIFI_SSID}")
message(STATUS "WiFi Pass: ${WIFI_PASSWORD}")

# Expose WiFi credentials
target_compile_definitions(target_support
        PUBLIC
        -DWIFI_SSID="${WIFI_SSID}"
        -DWIFI_PASSWORD="${WIFI_PASSWORD}"
        -DDEMO_ACCESS_TOKEN="${DEMO_ACCESS_TOKEN}")

