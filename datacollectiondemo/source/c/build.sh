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

# Exits immediately if error occurs
set -e

RUN_DIR=`pwd`

help() {
    echo "Choose one of the following: {build|run|deploy|clean}"
    echo "Supported targets: posix, cc32xx, esp8266"
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
# TODO: comment about inconvenience between KAA_TARGET and KAA_PLATFORM
KAA_TARGET=
KAA_PRODUCE_BINARY=
KAA_REQUIRE_CREDENTIALS=

select_arch() {
    echo "Please enter a target (default is posix):"
    read target

    case "$target" in
    posix)
        KAA_TARGET=posix
        ;;

    "")
        KAA_TARGET=posix
        ;;
    *)
        # Interpret custom string as target name
        KAA_TOOLCHAIN_PATH_SDK="-DCMAKE_TOOLCHAIN_FILE=$RUN_DIR/libs/kaa/toolchains/$target.cmake"
        KAA_TARGET=${target}
        KAA_PRODUCE_BINARY=true
        KAA_REQUIRE_CREDENTIALS=true
        ;;
    esac
}

unpack_sdk() {
    if [ ! -d "$KAA_C_LIB_HEADER_PATH" ] && [ ! -d "$KAA_CPP_LIB_HEADER_PATH" ]
    then
        KAA_SDK_TAR_NAME=$(find $PROJECT_HOME -iname $KAA_SDK_TAR)

        if [ -z "$KAA_SDK_TAR_NAME" ]
        then
            echo "Please, put the generated C/C++ SDK tarball into the libs/kaa folder and re-run the script."
            exit 1
        fi

        mkdir -p $KAA_LIB_PATH
        tar -zxf $KAA_SDK_TAR_NAME -C $KAA_LIB_PATH
    fi
}
build_app() {
    SSID=
    PASSWORD=

    cd $PROJECT_HOME
    mkdir -p "$PROJECT_HOME/$BUILD_DIR"
    cd $BUILD_DIR

    if [ $KAA_REQUIRE_CREDENTIALS ]
    then
        echo "Enter WiFi SSID:"
        read SSID
        echo "Enter WiFi Password:"
        read PASSWORD
    fi

    cmake -DKAA_PLATFORM=$KAA_TARGET \
          -DKAA_TARGET=$KAA_TARGET \
          -DKAA_PRODUCE_BINARY=$KAA_PRODUCE_BINARY \
          -DWIFI_SSID=$SSID \
          -DWIFI_PASSWORD=$PASSWORD \
          -DCMAKE_BUILD_TYPE=MinSizeRel \
          -DWITH_EXTENSION_EVENT=OFF \
          -DWITH_EXTENSION_NOTIFICATION=OFF \
          -DKAA_MAX_LOG_LEVEL=3 \
          "$KAA_TOOLCHAIN_PATH_SDK" ..
    make
}

clean() {
    rm -rf "$PROJECT_HOME/$BUILD_DIR"
    rm -rf "$PROJECT_HOME/$BUILD_DIR"
}

run() {
    cd "$PROJECT_HOME/$BUILD_DIR"
    ./$APP_NAME
}

for cmd in $@
do

case "$cmd" in
    build)
        select_arch
        unpack_sdk
        build_app
    ;;

    run)
        run
    ;;

    deploy)
        clean
        select_arch
        unpack_sdk
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

done
