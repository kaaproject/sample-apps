@REM
@REM  Copyright 2014-2016 CyberVision, Inc.
@REM
@REM  Licensed under the Apache License, Version 2.0 (the "License");
@REM  you may not use this file except in compliance with the License.
@REM  You may obtain a copy of the License at
@REM
@REM       http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM  Unless required by applicable law or agreed to in writing, software
@REM  distributed under the License is distributed on an "AS IS" BASIS,
@REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM  See the License for the specific language governing permissions and
@REM  limitations under the License.
@REM

@echo off

setlocal EnableDelayedExpansion

if "%1" == "" goto help
if not "%2" == "" goto help

set PROJECT_HOME=%CD%
set LIBS_PATH=libs
set KAA_LIB_PATH=%LIBS_PATH%\kaa
set KAA_C_LIB_HEADER_PATH=%KAA_LIB_PATH%\src
set KAA_CPP_LIB_HEADER_PATH=%KAA_LIB_PATH%\kaa
set KAA_SDK_TAR="kaa-c*.tar.gz"

set KAA_SDK_TAR_NAME=

call :checkEnv

if /i %1 == build (
    call :build
    goto :eof
)

if /i %1 == run goto run

if /i %1 == deploy (
    call :clean
    call :build
    call :run
    goto :eof
)

if /i %1 == clean goto clean

goto help

:build
    IF NOT EXIST %KAA_C_LIB_HEADER_PATH%\NUL (
       IF NOT EXIST %KAA_CPP_LIB_HEADER_PATH%\NUL (
        for /R %PROJECT_HOME% %%f in (kaa-c*.tar.gz) do (
                           set val=%%f
                           set KAA_SDK_TAR_NAME=!val:\=/!
        )
           md %KAA_LIB_PATH%
           7z x -y !KAA_SDK_TAR_NAME! -o%KAA_LIB_PATH%
        7z x -y !KAA_SDK_TAR_NAME:~0,-3! -o%KAA_LIB_PATH%
       )
    )
    cd %PROJECT_HOME%
    md %PROJECT_HOME%\build
    cd build
    cmake -G "NMake Makefiles" ..
    nmake
goto :eof

:clean
    call :deleteDir "%PROJECT_HOME%\build"
goto :eof

:run
    cd %PROJECT_HOME%\build
    call demo_client.exe
goto :eof

:deleteDir
    del /s /f /q %1\*.*
    for /f %%f in ('dir /ad /b %1\') do rd /s /q %1\%%f
    rd /s /q %1
goto :eof

:checkEnv
    IF EXIST %PROJECT_HOME%\env.bat (
        call %PROJECT_HOME%\env.bat
    )
goto :eof

:help
    echo "Choose one of the following: {build|run|deploy|clean}"
goto :eof

endlocal
