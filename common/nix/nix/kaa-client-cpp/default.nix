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

{ stdenv, lib, cmake, pkgconfig, boost, avro-cpp, botanUnstable, sqlite, python
, which
, flags ? ""
, doUnpack ? false
}:

stdenv.mkDerivation {
  name = "kaa-client-cpp";
  cmakeFlags = flags;

  src = ./../..;

  preConfigure = lib.optional doUnpack ''
    KAA_SDK_TAR_NAME=$(find -name "kaa-c*.tar.gz")
    tar -zxf "$KAA_SDK_TAR_NAME" -C "libs/kaa"
    cd libs/kaa
    ${stdenv.shell} ./avrogen.sh
    cd -

  '';

  buildInputs = [
    cmake
    pkgconfig
    boost
    avro-cpp
    botanUnstable
    sqlite
    python
    which
  ];
}
