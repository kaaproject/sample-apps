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

cmake_minimum_required(VERSION 2.8)

if(DEFINED $ENV{ESPRESSIF_HOME})
    set(ESPRESSIF_HOME $ENV{ESPRESSIF_HOME})
else()
    set(ESPRESSIF_HOME /opt/Espressif)
endif()

set(ESP_SDK_HOME ${ESPRESSIF_HOME}/esp-rtos-sdk)

if(DEFINED ENV{ESP8266_TOOLCHAIN_PATH})
  set(TOOLCHAIN_PATH "$ENV{ESP8266_TOOLCHAIN_PATH}")
  message(STATUS "Toolchain path is provided: ${TOOLCHAIN_PATH}")
else ()

  if (NOT TOOLCHAIN_PATH)
    # Set default path.
    set(TOOLCHAIN_PATH /opt/Espressif/crosstool-NG/builds/xtensa-lx106-elf)
    message(STATUS "Default toolchain path will be used")
  endif ()
endif ()

set(ESP_TOOLCHAIN_LIBS_HOME ${TOOLCHAIN_PATH}/lib/gcc/xtensa-lx106-elf/4.8.2)

add_library(target_support
            STATIC
            target.c
            driver/uart.c
            driver/gpio.c
            )


target_include_directories(target_support PUBLIC driver)
target_include_directories(target_support PUBLIC .)
target_include_directories(target_support PUBLIC
                           ${ESP_SDK_HOME}/extra_include
                           ${ESP_SDK_HOME}/include
                           ${ESP_SDK_HOME}/include/lwip
                           ${ESP_SDK_HOME}/include/lwip/ipv4
                           ${ESP_SDK_HOME}/include/lwip/ipv6
                           ${ESP_SDK_HOME}/include/espressif/)
# specify linker script
configure_file(${CMAKE_CURRENT_SOURCE_DIR}/ld/eagle.rom.addr.v6.ld ${CMAKE_BINARY_DIR})
configure_file(${CMAKE_CURRENT_SOURCE_DIR}/ld/eagle.app.v6.ld ${CMAKE_BINARY_DIR})

# ESP8266 RTOS SDK dependencies
target_link_libraries(target_support PUBLIC
                      ${ESP_SDK_HOME}/lib/libfreertos.a
                      ${ESP_SDK_HOME}/lib/libhal.a
                      ${ESP_SDK_HOME}/lib/libpp.a
                      ${ESP_SDK_HOME}/lib/libphy.a
                      ${ESP_SDK_HOME}/lib/libnet80211.a
                      ${ESP_SDK_HOME}/lib/libwpa.a
                      ${ESP_SDK_HOME}/lib/liblwip.a
                      ${ESP_SDK_HOME}/lib/libmain.a
                      ${ESP_SDK_HOME}/lib/libssl.a
                      ${ESP_SDK_HOME}/lib/libhal.a
                      ${ESP_TOOLCHAIN_LIBS_HOME}/libgcc.a
                      -T${CMAKE_CURRENT_SOURCE_DIR}/ld/eagle.app.v6.ld
                      )

message(STATUS "WiFi AP: ${WIFI_SSID}")
message(STATUS "WiFi Pass: ${WIFI_PASSWORD}")

# Expose WiFi credentials
target_compile_definitions(target_support
        PUBLIC
        -DWIFI_SSID="${WIFI_SSID}"
        -DWIFI_PASSWORD="${WIFI_PASSWORD}")

