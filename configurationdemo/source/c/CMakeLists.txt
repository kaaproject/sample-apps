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

project(C-SDK-sample)
enable_language(C)

# Our application name.
set(APP_NAME demo_client)

# List of additional libraries depends on target chosen.
set(ADDITIONAL_LIBS)

# Target-independent flags.
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=gnu99 -g -Wall -Wextra")

# Path to the Kaa SDK.
add_subdirectory(libs/kaa)
# Directory containing target support library.
add_subdirectory(targets/${KAA_TARGET})

# Demo executable.
add_executable(${APP_NAME} src/kaa_demo.c)

# All libraries that demo app depends on.
target_link_libraries(${APP_NAME} kaac_s target_support ${APP_LIBS})

# For most embedded targets binary stripping is required.
if (${KAA_PRODUCE_BINARY})
    add_custom_command(TARGET ${APP_NAME} POST_BUILD
            COMMAND ${CMAKE_OBJCOPY} -O binary ${APP_NAME} ${APP_NAME}.bin)
endif ()

