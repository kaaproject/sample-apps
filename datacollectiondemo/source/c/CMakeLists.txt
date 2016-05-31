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
project (C-SDK-sample LANGUAGES C)

find_package (OpenSSL REQUIRED)

set(APP_NAME "demo_client")
set(KAA_SDK_DIR "client-c")

execute_process(COMMAND ${CMAKE_COMMAND} -E make_directory ${KAA_SDK_DIR}
                COMMAND sh -c "tar -zxf kaa-c*.tar.gz -C ${KAA_SDK_DIR}"
                WORKING_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/libs/kaa"
                )

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=gnu99 -g -Wall -Wextra")

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/src
                    ${CMAKE_CURRENT_SOURCE_DIR}/libs/kaa/${KAA_SDK_DIR}/src
                   )

link_directories(${CMAKE_CURRENT_SOURCE_DIR}/build)

set(SAMPLE_SOURCE_FILES src/kaa_demo.c)

add_subdirectory(libs/kaa/${KAA_SDK_DIR})

add_executable(${APP_NAME} ${SAMPLE_SOURCE_FILES})
target_link_libraries(${APP_NAME} kaac crypto)