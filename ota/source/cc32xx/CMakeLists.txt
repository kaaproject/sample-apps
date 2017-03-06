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

cmake_minimum_required(VERSION 2.8.8)

project (C-SDK-sample)
enable_language(C)

set(APP_NAME demo_client)

add_definitions(-DSSID="${SSID}")
add_definitions(-DPWD="${PWD}")

# Disable unused features
set(WITH_EXTENSION_NOTIFICATION OFF CACHE BOOL "")
set(WITH_EXTENSION_LOGGING OFF CACHE BOOL "")
set(WITH_EXTENSION_USER OFF CACHE BOOL "")
set(WITH_ENCRYPTION OFF CACHE BOOL "")

set(CMAKE_BUILD_TYPE MinSizeRel)

add_subdirectory(libs/kaa)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=gnu99 -g -Wall -Wextra -DSSID='\"${SSID}\"' -DPWD='\"${PWD}\"' \
-DMAJOR_VERSION=${MAJOR_VERSION} -DMINOR_VERSION=${MINOR_VERSION} -DCLASSIFIER_VERSION='\"${CLASSIFIER_VERSION}\"' -DDEMO_LED=${DEMO_LED} ")

include(platform/${KAA_PLATFORM}/${KAA_PLATFORM}.cmake)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/src
                    ${CMAKE_CURRENT_SOURCE_DIR}/libs/kaa/src
                   )

add_executable(${APP_NAME} src/kaa_demo.c)
target_link_libraries(${APP_NAME} kaac)

add_custom_command(TARGET ${APP_NAME} POST_BUILD COMMAND ${CMAKE_OBJCOPY} -O binary ${APP_NAME}.afx ${APP_NAME}.bin)
