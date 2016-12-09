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

project(CredentialsDemo CXX)

# Disable unused features
set(KAA_WITHOUT_CONFIGURATION 1)
set(KAA_WITHOUT_NOTIFICATIONS 1)
set(KAA_WITHOUT_LOGGING 1)
set(KAA_WITHOUT_EVENTS 1)

# Set configuration variables
if (NOT DEFINED KAA_RUNTIME_KEY_GENERATION)
    set(KAA_RUNTIME_KEY_GENERATION "OFF")
endif (NOT DEFINED KAA_RUNTIME_KEY_GENERATION)

add_executable(demo_client src/KaaDemo.cpp)
target_link_libraries(demo_client kaacpp)
