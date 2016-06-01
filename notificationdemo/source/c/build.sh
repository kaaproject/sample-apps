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
    echo "Supported platforms: x86-64"
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
KAA_ARCH=x86-64

function select_arch {	
    echo "Please enter architecture(default is x86-64):"
    read arch
    KAA_TOOLCHAIN_PATH_SDK="-DCMAKE_TOOLCHAIN_FILE=$RUN_DIR/libs/kaa/toolchains/$arch.cmake"
    case "$arch" in
        edison)
          KAA_ARCH=x86-64
        ;;
        *)
          KAA_TOOLCHAIN_PATH_SDK=""
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

        mkdir -p $KAA_LIB_PATH &&
        tar -zxf $KAA_SDK_TAR_NAME -C $KAA_LIB_PATH
    fi
}

function build_app {
    cd $PROJECT_HOME
    mkdir -p "$PROJECT_HOME/$BUILD_DIR"
    cd $BUILD_DIR
    cmake -DCMAKE_BUILD_TYPE=Debug \
          -DKAA_WITHOUT_CONFIGURATION=1 \
          -DKAA_WITHOUT_EVENTS=1 \
          -DKAA_WITHOUT_LOGGING=1 \
          -DKAA_MAX_LOG_LEVEL=3 \
          -DAPP_NAME=$APP_NAME \
	      -DKAA_PLATFORM=$KAA_ARCH \
          $KAA_TOOLCHAIN_PATH_SDK ..
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
        help
    ;;
esac

done
