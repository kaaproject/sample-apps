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
project(NotificationDemo CXX)

# Disable unused features
set(KAA_WITHOUT_CONFIGURATION 1)
set(KAA_WITHOUT_LOGGING 1)
set(KAA_WITHOUT_EVENTS 1)



if (NOT DEFINED KAA_MAX_LOG_LEVEL)
set(KAA_MAX_LOG_LEVEL 3)
endif (NOT DEFINED KAA_MAX_LOG_LEVEL)

if (NOT DEFINED KAA_SDK_PATH)
    add_subdirectory(libs/kaa)
else (NOT DEFINED KAA_SDK_PATH)
    add_subdirectory(${KAA_SDK_PATH})
endif (NOT DEFINED KAA_SDK_PATH)

add_executable(demo_client src/KaaDemo.cpp)
target_link_libraries(demo_client kaacpp)

install(TARGETS demo_client DESTINATION bin)
