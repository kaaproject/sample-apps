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
KAA_SDK_TAR="kaa-c*.tar.gz"
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

function build_app {
    cd $PROJECT_HOME &&
    mkdir -p "$PROJECT_HOME/$BUILD_DIR" &&
    cd $BUILD_DIR &&
    cmake -DCMAKE_BUILD_TYPE=Debug \
          -DKAA_WITHOUT_EVENTS=1 \
          -DKAA_WITHOUT_LOGGING=1 \
          -DKAA_WITHOUT_NOTIFICATIONS=1 \
          -DKAA_WITHOUT_OPERATION_LONG_POLL_CHANNEL=1 \
          -DKAA_WITHOUT_OPERATION_HTTP_CHANNEL=1 \
          -DKAA_MAX_LOG_LEVEL=4 \
          -DKAA_RUNTIME_KEY_GENERATION=ON \
           $KAA_TOOLCHAIN_PATH_SDK \
          ..
    make -j4
}

function clean {
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

done
