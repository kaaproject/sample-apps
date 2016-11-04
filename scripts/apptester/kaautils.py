# -*- coding: utf-8 -*-

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

"""

kaautils.py

This module contains useful methods to operate with Kaa

"""

import requests
import time

class KaaNodeError(Exception):
    pass

class KaaSDKLanguage(object):
    """List of supported SDK languages"""
    C = 'C'
    CPP = 'CPP'
    JAVA = 'JAVA'
    OBJECTIVE_C = 'OBJC'

class KaaUser(object):
    """Represents Kaa user"""

    def __init__(self, name, password):
        """
        :param name: Kaa user name
        :type name: string
        :param password: Kaa user password
        :type password: string
        """

        self.name = name
        self.password = password

class KaaNode(object):
    """Allows to communicate with Kaa node via REST API"""

    def __init__(self, host, port):
        """
        :param host: Kaa node IP address
        :type host: string
        :param port: Kaa node port
        :type port: string or integer
        """

        self.host = str(host)
        self.port = str(port)

    def download_sdk(self, profile_id, language, kaauser, ofile):
        """Downloads specific SDK from Kaa server and writes it to a file.

        :param profile_id: Kaa SDK profile ID.
        :type profile_id: integer
        :param language: Represents SKD language.
        :type language: KaaSDKLanguage
        :param kaauser: The Kaa User.
        :type kaauser: KaaUser
        :param ofile: Output filename. NOTE: If the file is already exists,
        it will be overwritten.
        :type ofile: string
        """

        url = 'http://{}:{}/kaaAdmin/rest/api/sdk?sdkProfileId={}' \
        '&targetPlatform={}'.format(self.host, self.port, str(profile_id), language)

        req = requests.post(url, auth=(kaauser.name, kaauser.password))
        if req.status_code != requests.codes.ok:
            raise KaaNodeError('Unable to download SDK.' \
                               'Return code: {d}'.format(req.status_code))

        with open(ofile, 'w') as output_file:
            output_file.write(req.content)

    def get_applications(self, kaauser):
        """Gets the list for Kaa application. Returns result in JSON format.

        :param kaauser:  The Kaa user.
        :type kaauser: KaaUser
        """

        url = 'http://{}:{}/kaaAdmin/rest/api/applications'.format(self.host,
                                                             self.port)

        req = requests.get(url, auth=(kaauser.name, kaauser.password))
        if req.status_code != requests.codes.ok:
            raise KaaNodeError('Unable to get list of applications. ' \
                               'Return code: {d}'.format(req.status_code))

        return req.json()

    def get_sdk_profiles(self, appname, kaauser):
        """Gets the SDK profiles for application. Returns result in JSON format.

        :param appname: The name of the application.
        :type appname: string
        :param kaauser: The Kaa server IP address.
        :type kaauser: KaaUser
        """

        apps = self.get_applications(kaauser)
        token = None
        for app in apps:
            if app['name'] == appname:
                token = app['applicationToken']
                break

        if not token:
            raise KaaNodeError('Application: "{}" was not found'.format(appname))

        url = 'http://{}:{}/kaaAdmin/rest/api/sdkProfiles/{}'.fromat(self.host,
                                                               self.port,
                                                               str(token))

        req = requests.get(url, auth=(kaauser.name, kaauser.password))
        if req.status_code != requests.codes.ok:
            raise KaaNodeError('Unable to get SDK profiles. '\
                               'Return code: {d}'.format(req.status_code))

        return req.json()

    def wait_for_server(self, timeout):
        """Waits for Kaa REST server to be ready for operations.

        :param timeout: Timeout in seconds.
        :type timeout: integer
        """

        start = time.time()
        while time.time() - start < timeout:
            try:
                # Performing REST request to the server.
                # In case the server is ready it will respond with 401 (Unauthorized),
                # otherwise, an exception will be thrown.

                url = 'http://{}:{}/kaaAdmin/rest/api'.format(self.host, self.port)
                requests.get(url)

                return
            except requests.ConnectionError as ex:
                time.sleep(1)

        raise KaaNodeError("Timeout error")

class SandboxFrame(object):
    """Allows to communicate with Kaa Sandbox via REST API."""

    def __init__(self, host, port):
        """
        :param host: Sandbox IP address
        :type host: string
        :param port: Sandbox port
        :type port: string or integer
        """
        self.host = str(host)
        self.port = str(port)

    def get_demo_projects(self):
        """Gets the list for Kaa application from sandbox. Returns result in JSON format."""
        url = 'http://{}:{}/sandbox/rest/api/demoProjects'.format(self.host, self.port)
        req = requests.get(url)
        if req.status_code != requests.codes.ok:
            raise KaaSanboxError('Unable to get list of applications from Sandbox. ' \
                                'Return code: {}'.format(req.status_code))

        return req.json()

    def is_build_successful(self, app_id):
        """Gets application build result. Returns True or False.
        :param app_id: name of application on sandbox.
        :type app_id: string.
        """
        url = 'http://{}:{}/sandbox/rest/api/isProjectDataExists?projectId={}&dataType=BINARY'.format(self.host, self.port, app_id)
        req = requests.get(url)
        if req.status_code != requests.codes.ok:
            raise KaaSanboxError('Unable to check is it BINARY file in the Sandbox. ' \
                                'Return code: {d}'.format(req.status_code))

        return req.json()

    def build_android_java_demo(self, app_id):
        """Build demo applications from sandbox. Returns logs of build in JSON format.
        :param app_id: name of application on sandbox.
        :type app_id: string.
        """
        url = 'http://{}:{}/sandbox/rest/api/buildProjectData?projectId={}&dataType=BINARY'.format(self.host, self.port, app_id)
        header = 'Content-Type: application/json'
        req = requests.post(url, header)
        if req.status_code != requests.codes.ok:
            raise KaaSanboxError('Can not build application {}'.format(app_id))

        return req.content
