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

project(Cpp-SDK-sample)
enable_language(CXX)

add_subdirectory(libs/kaa)

find_package(Boost REQUIRED
             COMPONENTS filesystem)

set(SAMPLE_SOURCE_FILES src/KaaDemo.cpp)

add_executable(demo_client ${SAMPLE_SOURCE_FILES})

target_include_directories(demo_client PRIVATE ${Boost_INCLUDE_DIRS})
target_link_libraries(demo_client kaacpp ${Boost_LIBRARIES})
