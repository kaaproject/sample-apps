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

# Our application name.
set(APP_NAME demo_client)

# Disable unused features
set(WITH_EXTENSION_CONFIGURATION OFF CACHE BOOL "")
set(WITH_EXTENSION_NOTIFICATION OFF CACHE BOOL "")
set(WITH_EXTENSION_LOGGING OFF CACHE BOOL "")
set(WITH_EXTENSION_CONFIGURATION OFF CACHE BOOL "")
set(WITH_EXTENSION_USER OFF CACHE BOOL "")
set(WITH_ENCRYPTION OFF CACHE BOOL "")

# Target-independent flags.
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c99  -pthread -lm  -g -Wall -Wextra")

# Directory containing target support library.
add_subdirectory(targets/${KAA_PLATFORM})
add_subdirectory(targets/common)

add_executable(${APP_NAME} src/kaa_demo.c)
target_link_libraries(${APP_NAME} kaac target_support)

 
