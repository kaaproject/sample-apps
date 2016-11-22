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

# Exit immediately if error occurs
set -e

RUN_DIR=`pwd`

help() {
    echo "Choose one of the following: {build|run|deploy|clean}"
    echo "Supported targets: cc32xx, esp8266"
    exit 1
}

if [ $# -eq 0 ]
then
    help
fi

APP_NAME="demo_client"
PROJECT_HOME=$(pwd)
BUILD_DIR="build"
LIBS_PATH="libs"
KAA_LIB_PATH="$LIBS_PATH/kaa"
KAA_C_LIB_HEADER_PATH="$KAA_LIB_PATH/src"
KAA_CPP_LIB_HEADER_PATH="$KAA_LIB_PATH/kaa"
KAA_SDK_TAR="kaa-c*.tar.gz"
KAA_TOOLCHAIN_PATH_SDK=""
KAA_TARGET=
KAA_PRODUCE_BINARY=
KAA_REQUIRE_CREDENTIALS=
DEMO_ACCESS_TOKEN=

if [ ! -d "$KAA_C_LIB_HEADER_PATH" -a  ! -d "$KAA_CPP_LIB_HEADER_PATH" ]; then
    KAA_SDK_TAR_NAME=$(find $PROJECT_HOME -iname $KAA_SDK_TAR)

    if [ -z "$KAA_SDK_TAR_NAME" ]
    then
        echo "Please, put the generated C/C++ SDK tarball into the libs/kaa folder and re-run the script."
        exit 1
    fi

    mkdir -p $KAA_LIB_PATH
    tar -zxf $KAA_SDK_TAR_NAME -C $KAA_LIB_PATH
fi

if [ -z ${DEMO_ACCESS_TOKEN} ]; then
    DEMO_ACCESS_TOKEN=$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 6 | head -n 1)
fi
echo "==================================="
echo " ACCESS TOKEN: " $DEMO_ACCESS_TOKEN
echo "==================================="


select_arch() {
    echo "Please enter a target:"
    read target

    case "$target" in
    "")
        help
        ;;
    posix)
        KAA_TARGET=$target
        KAA_PRODUCE_BINARY=true
        KAA_REQUIRE_CREDENTIALS=false
        ;;
    *)
        # Interpret custom string as target name
        KAA_TOOLCHAIN_PATH_SDK="-DCMAKE_TOOLCHAIN_FILE=$RUN_DIR/libs/kaa/toolchains/$target.cmake"
        KAA_TARGET=$target
        KAA_PRODUCE_BINARY=true
        KAA_REQUIRE_CREDENTIALS=true
        ;;
    esac
}

build_app() {
    SSID=
    PASSWORD=

    cd $PROJECT_HOME
    mkdir -p "$PROJECT_HOME/$BUILD_DIR"
    cd $BUILD_DIR

    if [ $KAA_REQUIRE_CREDENTIALS = true ]; then
        echo "Enter WiFi SSID:"
        read SSID
        echo "Enter WiFi Password:"
        read PASSWORD
    fi

    cmake -DCMAKE_BUILD_TYPE=MinSizeRel \
          -DKAA_PLATFORM=$KAA_TARGET \
          -DKAA_PRODUCE_BINARY=$KAA_PRODUCE_BINARY \
          -DWIFI_SSID=$SSID \
          -DWIFI_PASSWORD=$PASSWORD \
          -DCMAKE_BUILD_TYPE=MinSizeRel \
          -DWITH_EXTENSION_CONFIGURATION=OFF \
          -DWITH_EXTENSION_NOTIFICATION=OFF \
          -DWITH_EXTENSION_LOGGING=OFF \
          -DWITH_EXTENSION_USER=OFF \
          -DDEMO_ACCESS_TOKEN=$DEMO_ACCESS_TOKEN \
          -DWITH_ENCRYPTION=OFF \
          -DKAA_MAX_LOG_LEVEL=0 \
          ${KAA_TOOLCHAIN_PATH_SDK} ..
    make
}

clean() {
    rm -rf "$KAA_LIB_PATH/$BUILD_DIR"
    rm -rf "$PROJECT_HOME/$BUILD_DIR"
}

run() {
    cd "$PROJECT_HOME/$BUILD_DIR"
    ./$APP_NAME
}

case "$1" in
    build)
        select_arch
        build_app
    ;;

    run)
        run
    ;;

    deploy)
        clean
        select_arch
        build_app
        run
        ;;

    clean)
        clean
    ;;

    *)
        help
    ;;
esac
