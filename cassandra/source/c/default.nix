#
#  Copyright 2016 CyberVision, Inc.
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

args@
{ posixSupport ? null
, clangSupport ? null
, cc3200Support ? null
, esp8266Support ? null
, raspberrypiSupport ? null
, testSupport ? null
, withWerror ? null
, withTooling ? null
}:

let pkgs = import ./nix { };

in pkgs.kaa-client-c.override
{
  raspberrypiSupport = true;
  doUnpack = true;
  flags = "-DKAA_MAX_LOG_LEVEL=3 " +
          "-DCMAKE_BUILD_TYPE=Debug " +
          "-DWITH_EXTENSION_EVENT=OFF " +
          "-DWITH_EXTENSION_CONFIGURATION=OFF " +
          "-DWITH_EXTENSION_NOTIFICATION=OFF";
}
