#!/bin/bash
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

function help_message {
    echo "Choose one of the following: {build|run|deploy|clean}"
    echo "Supported targets: cc32xx, esp8266"
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
# TODO: comment about inconvenience between KAA_TARGET and KAA_PLATFORM
KAA_TARGET=
KAA_PRODUCE_BINARY=
KAA_REQUIRE_CREDENTIALS=
DEMO_ACCESS_TOKEN=

if [ -z ${DEMO_ACCESS_TOKEN} ]; then
    DEMO_ACCESS_TOKEN=$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 6 | head -n 1)
fi
echo "==================================="
echo " ACCESS TOKEN: " $DEMO_ACCESS_TOKEN
echo "==================================="


function select_arch {
    echo "Please enter a target:"
    read target

    # TODO: better case handling
    case "$target" in
    posix)
        echo "posix platform is not supported by this demo"
        exit 0
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

function unpack_sdk {
    if [[ ! -d "$KAA_C_LIB_HEADER_PATH" &&  ! -d "$KAA_CPP_LIB_HEADER_PATH" ]]
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

function build_app {
    SSID=
    PASSWORD=

    cd $PROJECT_HOME
    mkdir -p "$PROJECT_HOME/$BUILD_DIR"
    cd $BUILD_DIR

    if [[ $KAA_REQUIRE_CREDENTIALS = true ]]
    then
        echo "Enter WiFi SSID:"
        read SSID
        echo "Enter WiFi Password:"
        read PASSWORD
    fi

    # TODO: APP-63 comments about KAA_PLATFORM and KAA_TARGET
    cmake -DCMAKE_BUILD_TYPE=MinSizeRel \
          -DKAA_PLATFORM=$KAA_TARGET \
          -DKAA_TARGET=$KAA_TARGET \
          -DKAA_PRODUCE_BINARY=$KAA_PRODUCE_BINARY \
          -DWIFI_SSID=$SSID \
          -DWIFI_PASSWORD=$PASSWORD \
          -DCMAKE_BUILD_TYPE=MinSizeRel \
          -DKAA_WITHOUT_CONFIGURATION=1 \
          -DKAA_WITHOUT_NOTIFICATION=1 \
          -DDEMO_ACCESS_TOKEN=$DEMO_ACCESS_TOKEN \
          -DKAA_MAX_LOG_LEVEL=3 \
          ${KAA_TOOLCHAIN_PATH_SDK} ..
    make
}

function clean {
    rm -rf "$KAA_LIB_PATH/$BUILD_DIR"
    rm -rf "$PROJECT_HOME/$BUILD_DIR"
}

function run {
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
        help_message
    ;;
esac

done
