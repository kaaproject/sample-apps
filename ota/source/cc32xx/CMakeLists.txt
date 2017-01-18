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

project (C-SDK-sample)
enable_language(C)

if(KAA_PLATFORM STREQUAL "" OR KAA_PLATFORM STREQUAL "posix")
  set(KAA_PLATFORM "posix")
  set(APP_LIBS kaac_s)
  set(SAMPLE_SOURCE_FILES
            src/kaa_demo.c 
     )
  link_directories(${CMAKE_CURRENT_SOURCE_DIR}/build)
else()
  include(${CMAKE_CURRENT_SOURCE_DIR}/platform/${KAA_PLATFORM}/${KAA_PLATFORM}.cmake)
endif()

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=gnu99 -g -Wall -Wextra -DSSID='\"${SSID}\"' -DPWD='\"${PWD}\"' \
-DMAJOR_VERSION=${MAJOR_VERSION} -DMINOR_VERSION=${MINOR_VERSION} -DCLASSIFIER_VERSION='\"${CLASSIFIER_VERSION}\"' -DDEMO_LED=${DEMO_LED} ")


include_directories(${CMAKE_CURRENT_SOURCE_DIR}/src
                    ${CMAKE_CURRENT_SOURCE_DIR}/libs/kaa/src
                   )

add_executable(${APP_NAME} ${SAMPLE_SOURCE_FILES})
target_link_libraries(${APP_NAME} ${APP_LIBS})

add_custom_command(TARGET ${APP_NAME} POST_BUILD COMMAND ${CMAKE_OBJCOPY} -O binary ${APP_NAME}.afx ${APP_NAME}.bin)
