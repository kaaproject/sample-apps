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

************************************
PREFACE
************************************

This demo application illustrates the Kaa event feature using the Kaa C SDK.
To read more about the logging feature, visit http://docs.kaaproject.org/display/KAA/Notifications.

************************************
INSTALLATION
************************************

To run the demo application, do the following:

1. Install third-party libraries to build the Kaa C SDK.
Follow http://docs.kaaproject.org/display/KAA/Third-party+components#Third-partycomponents-Kaaendpoint to see the full
list of third-party dependencies for the Kaa C SDK.
2. Download and install the Kaa sandbox (http://docs.kaaproject.org/display/KAA/sandbox).
3. Before generating the C SDK, make sure that you provided the SDK with a user verifier and the following fields for the event class family were specified as follows:
 Name: ThermostatEventClassFamily
 Namespace: org.kaaproject.kaa.schema.sample.event.thermo
 Class name: ThermostatEventClassFamily
 (The Avro event schema required for the demo application is located in the 'avro' directory).
4. Generate the C SDK using the Kaa sandbox. Put the SDK archive into the 'libs/kaa' directory.
5. Use the build.sh script to build and run the demo application (the 'deploy' option).

************************************
ABOUT
************************************

If you are interested in the Kaa IoT framework and want to read more about it, visit our official site
http://www.kaaproject.org/. The documentation can be found at http://docs.kaaproject.org/display/KAA/Kaa+IoT+Platform+Home.

