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

#This code is identical or almost identical to the code for Kaa library.

{ stdenv
, lib
, writeTextFile
, cmake

, astyle ? null
, doxygen ? null

, clang ? null
, openssl ? null

, cmocka ? null
, cppcheck ? null
, valgrind ? null
, python ? null
, gcc-arm-embedded ? null
, jre ? null

, gcc-xtensa-lx106 ? null
, esp8266-rtos-sdk ? null
, cc3200-sdk ? null

, raspberrypi-tools ? null
, raspberrypi-openssl ? null
, raspberrypi-wiringPi ? null

, clangSupport ? false
, posixSupport ? false
, cc3200Support ? false
, esp8266Support ? false
, raspberrypiSupport ? false
, testSupport ? false
, withTooling ? false
, withWerror ? false
, doUnpack ? false
, flags ? ""
}:

assert clangSupport -> clang != null && openssl != null;
assert posixSupport -> openssl != null;
assert esp8266Support -> gcc-xtensa-lx106 != null && esp8266-rtos-sdk != null && jre != null;
assert cc3200Support -> cc3200-sdk != null && gcc-arm-embedded != null && jre != null;
assert raspberrypiSupport -> raspberrypi-tools != null && raspberrypi-openssl != null;
assert testSupport -> posixSupport != null && cmocka != null && cppcheck != null &&
                      valgrind != null && python != null;


stdenv.mkDerivation {
  name = "kaa-client-c";
  cmakeFlags = 
    # if raspberrypiSupport then
    #  "-DCMAKE_PREFIX_PATH=${raspberrypi-openssl} -DCMAKE_TOOLCHAIN_FILE=libs/kaa/toolchains/rpi.cmake"
    # else
    if cc3200Support then 
      "-DCC32XX_SDK='${cc3200-sdk}/lib/cc3200-sdk/cc3200-sdk' -DCC32XX_TOOLCHAIN_PATH='${gcc-arm-embedded}' " + flags 
    else 
    if esp8266Support then
      "-DESP8266_TOOLCHAIN_PATH='${gcc-xtensa-lx106}' -DESP8266_SDK_PATH='${esp8266-rtos-sdk}/lib/esp8266-rtos-sdk'" + flags
    else
    if clangSupport then
      "-DCMAKE_C_COMPILER=clang -DCMAKE_CXX_COMPILER=clang++" + flags
    else
      flags;

 

  src = ./../..;
 
  preConfigure = lib.optional doUnpack ''
    KAA_SDK_TAR_NAME=$(find -name "kaa-c*.tar.gz")
    tar -zxf $KAA_SDK_TAR_NAME -C "libs/kaa"
  '';

  buildInputs = [
    cmake
  ] ++ lib.optional withTooling [
    astyle
    doxygen
  ] ++ lib.optional clangSupport [
    clang
    openssl
  ] ++ lib.optional posixSupport [
    openssl
  ] ++ lib.optional testSupport [
    cmocka
    cppcheck
    valgrind
    python
  ] ++ lib.optional esp8266Support [
    gcc-xtensa-lx106
    esp8266-rtos-sdk
    jre
  ] ++ lib.optional cc3200Support [
    cc3200-sdk
    gcc-arm-embedded
    jre
  ] ++ lib.optional raspberrypiSupport [
    raspberrypi-tools
    raspberrypi-openssl
    raspberrypi-wiringPi
  ];

  preBuild = lib.optional raspberrypiSupport ''
    export CPATH=${raspberrypi-wiringPi}/include:$CPATH
    export LIBRARY_PATH=${raspberrypi-wiringPi}/lib:$LIBRARY_PATH
  '';

  enableParallelBuilding = true;

  shellHook = ''
    export CTEST_OUTPUT_ON_FAILURE=1
  '';
}
