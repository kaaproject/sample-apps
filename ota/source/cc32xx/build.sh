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


RUN_DIR=`pwd`

function help {
    echo "Choose one of the following: {build|run|deploy|clean}"
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

#Wifi settings
SSID="xxx"
PASSWORD="xxxxxxxxx"
#Firmware version
MAJOR_VERSION=1
MINOR_VERSION=0
DEMO_LED=0

function build_app {
    read -p "Enter WiFi SSID: " SSID
    read -p "Enter WiFi Password: " PASSWORD
    read -p "Enter firmware major version: " MAJOR_VERSION
    read -p "Enter firmware minor version: " MINOR_VERSION
    read -p "Enter firmware classifier: " CLASSIFIER_VERSION
    read -p "Enter flags of an active leds[ red=0x01 orange=0x02 green=0x04 ]: " DEMO_LED 

    if [ -z $DEMO_LED ]
    then
        DEMO_LED=0
    fi

    cd $PROJECT_HOME &&
    mkdir -p "$PROJECT_HOME/$BUILD_DIR" &&
    cd $BUILD_DIR
    cmake -DKAA_PLATFORM=cc32xx -DCMAKE_TOOLCHAIN_FILE=../libs/kaa/toolchains/cc32xx.cmake -DBUILD_TESTING=OFF -DSSID=$SSID -DPWD=$PASSWORD -DMAJOR_VERSION=$MAJOR_VERSION -DMINOR_VERSION=$MINOR_VERSION $ENV_VAR -DCLASSIFIER_VERSION=$CLASSIFIER_VERSION -DDEMO_LED="$DEMO_LED" ..
    make
}

function clean {
    rm -rf "$KAA_LIB_PATH/$BUILD_DIR"
    rm -rf "$PROJECT_HOME/$BUILD_DIR"
}

function run {
    mkdir -p $PROJECT_HOME/../../fmw_bin
    cp $PROJECT_HOME/$BUILD_DIR/demo_client.bin $PROJECT_HOME/../../fmw_bin/demo_client_0x0$DEMO_LED.bin
}

#for cmd in $@
#do
cmd=$1

case "$cmd" in
    build)
        build_app
    ;;

    run)
        run
    ;;

    deploy)
        clean
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

