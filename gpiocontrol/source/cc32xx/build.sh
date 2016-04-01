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

# TODO APP-63: this file must be reviewed and moved to common build script dir


# Exit immediately if error occurs
set -e

RUN_DIR=`pwd`

help_message() {
    echo "Choose one of the following: {build|run|deploy|clean}"
    echo "Supported targets: cc32xx" # TODO APP-63: extend these
    exit 1
}

if [ $# -eq 0 ]
then
    help_message
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
# TODO APP-63: comment about inconvenience between KAA_TARGET and KAA_PLATFORM
KAA_TARGET=cc32xx
KAA_PRODUCE_BINARY=1
DEMO_ACCESS_TOKEN=


if [ -z ${DEMO_ACCESS_TOKEN} ]; then
    DEMO_ACCESS_TOKEN=$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 6 | head -n 1)
fi
echo "==================================="
echo " ACCESS TOKEN: " $DEMO_ACCESS_TOKEN
echo "==================================="


unpack_sdk() {
    if [ ! -d "$KAA_C_LIB_HEADER_PATH"  -a ! -d "$KAA_CPP_LIB_HEADER_PATH" ]
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
    # Wifi settings
    SSID=
    PASSWORD=

    ENV_VAR=" -DKAA_PLATFORM=cc32xx -DAPP_NAME=$APP_NAME"
        if [ "$(expr substr $(uname -s) 1 9)" == "CYGWIN_NT" ]; then
                ENV_VAR=$ENV_VAR" -DKAA_TOOLCHAIN_PATH=c:/cygwin/opt/kaa"
        fi

    cd $PROJECT_HOME
    mkdir -p "$PROJECT_HOME/$BUILD_DIR"
    cd $BUILD_DIR

    echo "Enter WiFi SSID:"
    read SSID
    echo "Enter WiFi Password:"
    read PASSWORD

    KAA_TOOLCHAIN_PATH_SDK="-DCMAKE_TOOLCHAIN_FILE=$KAA_LIB_PATH/toolchains/cc32xx.cmake"

    # TODO: comments about KAA_PLATFORM and KAA_TARGET
    cmake -DKAA_PLATFORM=$KAA_TARGET \
          -DKAA_TARGET=$KAA_TARGET \
          -DKAA_PRODUCE_BINARY=$KAA_PRODUCE_BINARY \
          -DWIFI_SSID=$SSID \
          -DWIFI_PASSWORD=$PASSWORD \
          -DKAA_MAX_LOG_LEVEL=3 \
          -DDEMO_ACCESS_TOKEN=$DEMO_ACCESS_TOKEN \
          -DKAA_DEBUG_ENABLED=true \
          ${KAA_TOOLCHAIN_PATH_SDK} ..
    make
}

clean() {
    rm -rf "$KAA_LIB_PATH/$BUILD_DIR"
    rm -rf "$PROJECT_HOME/$BUILD_DIR"
}

run() {
    echo "To run demo, please have a look at http://docs.kaaproject.org/display/KAA/Texas+Instruments+CC3200#TexasInstrumentsCC3200-Example"
}

case "$1" in
    build)
        unpack_sdk
        build_app
    ;;

    run)
        run
    ;;

    deploy)
        clean
        unpack_sdk
        build_app
        run
        ;;

    clean)
        clean
    ;;

    *)
        help_message
    ;;
esac
