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

project(ProfilingDemo C)

# Disable unused features
set(WITH_EXTENSION_NOTIFICATIONS OFF CACHE BOOL "")
set(WITH_EXTENSION_LOGGING OFF CACHE BOOL "")
set(WITH_EXTENSION_EVENTS OFF CACHE BOOL "")

# Set configuration variables
if (NOT DEFINED KAA_RUNTIME_KEY_GENERATION)
    set(KAA_RUNTIME_KEY_GENERATION "ON")
endif (NOT DEFINED KAA_RUNTIME_KEY_GENERATION)
add_definitions(-DKAA_RUNTIME_KEY_GENERATION="${KAA_RUNTIME_KEY_GENERATION}")

add_executable(demo_client src/kaa_demo.c)
target_link_libraries(demo_client kaac)
