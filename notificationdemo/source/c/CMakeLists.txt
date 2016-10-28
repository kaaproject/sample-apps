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

cmake_minimum_required(VERSION 2.8.12)
project(NotificationDemo C)

# Disable unused features
set(WITH_EXTENSION_CONFIGURATION OFF)
set(WITH_EXTENSION_LOGGING OFF)
set(WITH_EXTENSION_EVENTS OFF)

# Set configuration variables
if (NOT DEFINED KAA_PLATFORM)
    set(KAA_PLATFORM "posix")
endif (NOT DEFINED KAA_PLATFORM)
if (NOT DEFINED WIFI_SSID)
    set(WIFI_SSID "WiFi SSID")
endif (NOT DEFINED WIFI_SSID)
if (NOT DEFINED KAA_MAX_LOG_LEVEL)
    set(KAA_MAX_LOG_LEVEL 3)
endif (NOT DEFINED KAA_MAX_LOG_LEVEL)
if (NOT DEFINED WIFI_PASSWORD)
    set(WIFI_PASSWORD "Password")
endif (NOT DEFINED WIFI_PASSWORD)

if (NOT DEFINED KAA_SDK_PATH)
    add_subdirectory(libs/kaa)
else (NOT DEFINED KAA_SDK_PATH)
    add_subdirectory(${KAA_SDK_PATH})
endif (NOT DEFINED KAA_SDK_PATH)

# This is required for ESP8266 platform
# due to it's specific requirements regarding linked executable.
# The blank.c file is a placeholder for CMake's add_executable()
# All the code (Kaa SDK, ESP8266 SDK and demo) is compiled as static libraries
# and linked into that executable.
add_subdirectory(targets/${KAA_PLATFORM})
if("${KAA_PLATFORM}" STREQUAL "esp8266")
    add_library(demo_client_s STATIC src/kaa_demo.c)
    file(WRITE ${CMAKE_BINARY_DIR}/blank.c "")
    add_executable(demo_client ${CMAKE_BINARY_DIR}/blank.c)
    target_link_libraries(demo_client demo_client_s)
    target_link_libraries(demo_client_s kaac target_support)
else()
    add_executable(demo_client src/kaa_demo.c)
    target_link_libraries(demo_client kaac target_support)
endif()

install(TARGETS demo_client DESTINATION bin)
