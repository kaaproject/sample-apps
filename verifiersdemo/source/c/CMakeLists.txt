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
project(VerifiersDemo C)

# Disable unused features
set(WITH_EXTENSION_CONFIGURATION OFF)
set(WITH_EXTENSION_NOTIFICATIONS OFF)
set(WITH_EXTENSION_LOGGING OFF)

if (NOT DEFINED DEMO_ACCESS_TOKEN)
    set(DEMO_ACCESS_TOKEN "asscess_token")
endif (NOT DEFINED DEMO_ACCESS_TOKEN)
add_definitions(-DDEMO_ACCESS_TOKEN=${DEMO_ACCESS_TOKEN})

if (NOT DEFINED KAA_MAX_LOG_LEVEL)
    set(KAA_MAX_LOG_LEVEL "3")
endif (NOT DEFINED KAA_MAX_LOG_LEVEL)


if (NOT DEFINED KAA_SDK_PATH)
    add_subdirectory(libs/kaa)
else (NOT DEFINED KAA_SDK_PATH)
    add_subdirectory(${KAA_SDK_PATH})
endif (NOT DEFINED KAA_SDK_PATH)

add_executable(demo_client src/kaa_demo.c)
target_link_libraries(demo_client kaac)

install(TARGETS demo_client DESTINATION bin)
