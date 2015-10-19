#
# Copyright 2014-2015 CyberVision, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#!/bin/bash

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
KAA_SDK_TAR="kaa-client*.tar.gz"

#Wifi settings
SSID="xxx"
PASSWORD="xxxxxxxxx"
#Firmware version
MAJOR_VERSION=1
MINOR_VERSION=0

function build_thirdparty {
    if [[ ! -d "$KAA_C_LIB_HEADER_PATH" &&  ! -d "$KAA_CPP_LIB_HEADER_PATH" ]]
    then
        KAA_SDK_TAR_NAME=$(find $PROJECT_HOME -iname $KAA_SDK_TAR)

        if [ -z "$KAA_SDK_TAR_NAME" ]
        then
            echo "Please, put the generated C/C++ SDK tarball into the libs/kaa folder and re-run the script."
            exit 1
        fi

        mkdir -p $KAA_LIB_PATH &&
        tar -zxf $KAA_SDK_TAR_NAME -C $KAA_LIB_PATH
    fi

    if [ ! -d "$KAA_LIB_PATH/$BUILD_DIR" ]
    then
        cd $KAA_LIB_PATH &&
        mkdir -p $BUILD_DIR && cd $BUILD_DIR
		
		ENV_VAR=" -DKAA_PLATFORM=cc32xx \
			  -DCMAKE_TOOLCHAIN_FILE=../toolchains/cc32xx.cmake \
              -DKAA_MAX_LOG_LEVEL=2"
		
		if [ "$(expr substr $(uname -s) 1 9)" == "CYGWIN_NT" ]; then
			ENV_VAR=$ENV_VAR" -DKAA_TOOLCHAIN_PATH=c:/cygwin64/opt/kaa"
		fi

		echo $ENV_VAR
		
        cmake -G "Unix Makefiles" $ENV_VAR ..
    fi
	
    cd "$PROJECT_HOME/$KAA_LIB_PATH/$BUILD_DIR"
    make -j4 &&
    cd $PROJECT_HOME
}

function build_app {
    echo "Enter WiFi SSID:"
    read SSID
    echo "Enter WiFi Password:"
    read PASSWORD
    echo "Enter firmware major version:"
    read MAJOR_VERSION
    echo "Enter firmware minor version:"
    read MINOR_VERSION
    cd $PROJECT_HOME &&
    mkdir -p "$PROJECT_HOME/$BUILD_DIR" &&
    cp "$KAA_LIB_PATH/$BUILD_DIR/"libkaa* "$PROJECT_HOME/$BUILD_DIR/" &&
    cd $BUILD_DIR
    ENV_VAR=" -DKAA_PLATFORM=cc32xx -DAPP_NAME=$APP_NAME"
	if [ "$(expr substr $(uname -s) 1 9)" == "CYGWIN_NT" ]; then
		ENV_VAR=$ENV_VAR" -DKAA_TOOLCHAIN_PATH=c:/cygwin64/opt/kaa"
	fi
	
	#source PATH=$PATH:/opt/kaa/gcc-arm-none-eabi/bin
#Cha5hk123
    cmake -G "Unix Makefiles" -DSSID=$SSID -DPWD=$PASSWORD -DMAJOR_VERSION=$MAJOR_VERSION -DMINOR_VERSION=$MINOR_VERSION $ENV_VAR .. &&
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

#for cmd in $@
#do
cmd=$1

case "$cmd" in
    build)
        build_thirdparty  &&
        build_app
    ;;

    run)
        run
    ;;

    deploy)
        clean
        build_thirdparty
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

