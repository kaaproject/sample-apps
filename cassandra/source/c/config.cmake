#
# Copyright 2014-2016 CyberVision, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

cmake_minimum_required(VERSION 3.0.2)

project(C-SDK-sample C)

find_package(Threads REQUIRED)

set(APP_NAME "demo_client")

set(CMAKE_C_FLAGS "${CMAKE_CFLAGS} -std=c99 -g -Wall -Wextra")

set(SAMPLE_SOURCE_FILES src/kaa_demo.c src/dht11/dht11.c)

set(WITH_EXTENSION_EVENT OFF CACHE BOOL "")
set(WITH_EXTENSION_NOTIFICATION OFF CACHE BOOL "")
set(WITH_EXTENSION_CONFIGURATION OFF CACHE BOOL "")

add_executable(${APP_NAME} ${SAMPLE_SOURCE_FILES})
target_link_libraries(${APP_NAME} kaac wiringPi ${CMAKE_THREAD_LIBS_INIT})
