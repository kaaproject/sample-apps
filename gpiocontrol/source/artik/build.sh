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
    echo "Supported platforms: x86-64, edison"
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
KAA_TOOLCHAIN_PATH_SDK=""

function select_arch {
    echo "Please enter architecture(default is x86-64):"
    read arch
    case "$arch" in
        edison)
	  KAA_TOOLCHAIN_PATH_SDK="-DCMAKE_TOOLCHAIN_FILE=$RUN_DIR/libs/kaa/toolchains/$arch.cmake"
        ;;
        *)
          KAA_TOOLCHAIN_PATH_SDK=""
        ;;
    esac
}

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
        chmod 755 ./avrogen.sh &&
        ./avrogen.sh && 
        mkdir -p $BUILD_DIR && cd $BUILD_DIR &&
        cmake -DKAA_DEBUG_ENABLED=1 \
              -DKAA_WITHOUT_LOGGING=1 \
              -DKAA_WITHOUT_CONFIGURATION=1 \
              -DKAA_WITHOUT_NOTIFICATIONS=1 \
              -DKAA_WITHOUT_OPERATION_LONG_POLL_CHANNEL=1 \
              -DKAA_WITHOUT_OPERATION_HTTP_CHANNEL=1 \
              -DKAA_MAX_LOG_LEVEL=3 \
               $KAA_TOOLCHAIN_PATH_SDK \
              ..
    fi

    cd "$PROJECT_HOME/$KAA_LIB_PATH/$BUILD_DIR"
    make -j4 &&
    cd $PROJECT_HOME
}

function build_app {
    echo "Enter Access token:"
    read DEMO_ACCESS_TOKEN
    cd $PROJECT_HOME &&
    mkdir -p "$PROJECT_HOME/$BUILD_DIR" &&
    cp "$KAA_LIB_PATH/$BUILD_DIR/"libkaa* "$PROJECT_HOME/$BUILD_DIR/" &&
    cd $BUILD_DIR &&
    cmake -DAPP_NAME=$APP_NAME $KAA_TOOLCHAIN_PATH_SDK -DDEMO_ACCESS_TOKEN=$DEMO_ACCESS_TOKEN ..
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
        build_thirdparty &&
        build_app
    ;;

    run)
        run
    ;;

    deploy)
        clean
        select_arch
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

done

