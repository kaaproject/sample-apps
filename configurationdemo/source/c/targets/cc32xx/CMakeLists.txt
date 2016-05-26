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

# TODO APP-63: this file must be reviewed and moved to common targets dir

# Before this module is added, SDK path must be provided
if(NOT EXISTS "${CC32XX_SDK}")
    message(FATAL_ERROR "CC32XX SDK path: ${CC32XX_SDK} not exist!")
endif()

# Target support library that provides abstractions for cc32xx target.
add_library(target_support
        STATIC
        # Example files contains startup routine and other useful stuff.
        ${CC32XX_SDK}/example/common/uart_if.c
        ${CC32XX_SDK}/example/common/udma_if.c
        ${CC32XX_SDK}/example/common/gpio_if.c
        ${CC32XX_SDK}/example/common/utils_if.c
        ${CC32XX_SDK}/example/common/startup_gcc.c
        cc32xx_support.c
        )

# Additional library required for nonos library
add_library(cc32xx_common
        STATIC
        ${CC32XX_SDK}/example/common/network_common.c)

target_include_directories(target_support PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})

# CC32XX SDK libraries

set(LIB_DRIVER ${CC32XX_SDK}/driverlib/gcc/exe/libdriver.a)
set(LIB_SIMPLELINK_NONOS ${CC32XX_SDK}/simplelink/gcc/exe/libsimplelink_nonos.a)
set(CC32XX_SDK_LIBS ${LIB_SIMPLELINK_NONOS} ${LIB_DRIVER})

target_include_directories(target_support
        PUBLIC
        ${CC32XX_SDK}/inc
        ${CC32XX_SDK}/driverlib
        ${CC32XX_SDK}/simplelink
        ${CC32XX_SDK}/simplelink/include
        ${CC32XX_SDK}/simplelink/source
        ${CC32XX_SDK}/example/common)

# Find and use ARM GCC libraries, provided that CMAKE_FIND_ROOT_PATH is properly set
# TODO KAA-994: Use gcc as a linker, rather than listing versions dirs in PATH_SUFFIXES variables
# and trying to link/find libraries. See related issue for further details.

find_library(LIB_C_PATH
        NAMES c libc
        PATHS /lib/armv7e-m/
        NO_DEFAULT_PATH)

find_library(LIB_M_PATH
        NAMES m libm
        PATHS /lib/armv7e-m/
        NO_DEFAULT_PATH)

# Required for few stubs when in debug mode
find_library(LIB_NOSYS_PATH
        NAMES nosys
        PATHS /lib/armv7e-m/
        NO_DEFAULT_PATH)

find_library(LIB_GCC_PATH
        NAMES gcc
        PATHS "${TOOLCHAIN_PATH}/lib/gcc/arm-none-eabi/"
        PATH_SUFFIXES 5.3.0/armv7e-m/ 4.9.3/armv7e-m/ 4.7.4/armv7e-m/
        NO_DEFAULT_PATH
        NO_CMAKE_FIND_ROOT_PATH)

# GCC dependencies
target_link_libraries(target_support PUBLIC ${LIB_C_PATH} ${LIB_M_PATH} ${LIB_GCC_PATH} ${LIB_NOSYS_PATH})
# CC32XX dependencies
target_link_libraries(target_support PUBLIC ${CC32XX_SDK_LIBS} cc32xx_common)
# Propagate linker script
target_link_libraries(target_support PUBLIC -T${CMAKE_CURRENT_SOURCE_DIR}/app.ld)

message(STATUS "WiFi AP: ${WIFI_SSID}")
message(STATUS "WiFi Pass: ${WIFI_PASSWORD}")

# Expose WiFi credentials
target_compile_definitions(target_support
        PUBLIC
        -DWIFI_SSID="${WIFI_SSID}"
        -DWIFI_PASSWORD="${WIFI_PASSWORD}")


