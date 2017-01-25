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

# Target support library that provides abstractions for posix target.
add_library(target_support 
	    STATIC
	    target_gpio_led.c)

target_include_directories(target_support PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})
target_include_directories(target_support PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/../common)
target_link_libraries(target_support m)

# Expose WiFi credentials
target_compile_definitions(target_support
        INTERFACE
        -DDEMO_ACCESS_TOKEN="${DEMO_ACCESS_TOKEN}")
