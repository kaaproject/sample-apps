#!/bin/sh
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

KAA_SDK_DIR="kaa-ep-sdk"

cd libs
mkdir "$KAA_SDK_DIR"

KAA_SDK_TAR=$(find . -name "kaa-*.tar.gz")

tar -zxvf "$KAA_SDK_TAR" -C "$KAA_SDK_DIR"
cd "$KAA_SDK_DIR"
pod setup
pod install
