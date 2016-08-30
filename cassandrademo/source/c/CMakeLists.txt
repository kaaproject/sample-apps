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

cmake_minimum_required(VERSION 2.8.8)
project(C-SDK-sample C)

find_package(Threads REQUIRED)

set(APP_NAME "demo_client")
set(KAA_SDK_DIR "client-c")

execute_process(COMMAND ${CMAKE_COMMAND} -E make_directory ${KAA_SDK_DIR})
execute_process(COMMAND sh -c "tar -zxf ${CMAKE_CURRENT_SOURCE_DIR}/libs/kaa/kaa-c*.tar.gz -C ${KAA_SDK_DIR}")

set(CMAKE_C_FLAGS "${CMAKE_CFLAGS} -std=c99 -g -Wall -Wextra")

set(SAMPLE_SOURCE_FILES src/kaa_demo.c src/dht11/dht11.c)

add_subdirectory(${CMAKE_CURRENT_BINARY_DIR}/${KAA_SDK_DIR}
                 ${CMAKE_CURRENT_BINARY_DIR}/${KAA_SDK_DIR}/build)

add_executable(${APP_NAME} ${SAMPLE_SOURCE_FILES})
target_link_libraries(${APP_NAME} kaac wiringPi ${CMAKE_THREAD_LIBS_INIT})
install(TARGETS ${APP_NAME} DESTINATION bin)
