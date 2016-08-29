#
#  Copyright 2016 CyberVision, Inc.
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

{stdenv, fetchgit}:
stdenv.mkDerivation {
  name = "wiringPi";

  src = fetchgit {
    url = "git://git.drogon.net/wiringPi";
    rev    = "b0a60c3302973ca1878d149d61f2f612c8f27fac";
    sha256 = "1zh9arb7spplm51hfrpfsxsv9bw8a186d438f4jkma9yadz1k0w3";
  };

  buildPhase = ''
    mkdir -p $out/include $out/lib $out/bin
    export Q=
    export WIRINGPI_SUDO=
    export PREFIX=
    export DESTDIR=$out
    export LDCONFIG=
    export CPATH=$out/include:$CPATH
    export WIRINGPI_SUID=0
    echo "calling build"
    ./build
  '';

  installPhase = ":";
}

