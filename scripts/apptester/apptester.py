#!/usr/bin/env python

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

import sys
import traceback
import os
import subprocess
import yaml
import argparse
import requests

from terminaltables import AsciiTable

from shutil import copytree
from shutil import copy2
from shutil import rmtree

from kaautils import KaaNode
from kaautils import KaaUser
from kaautils import KaaSDKLanguage
from kaautils import SandboxFrame


CONFIGFILE = os.path.join(os.path.dirname(__file__), 'apptester.yam')

class TestStatus(object):
    """Represents possible test and build results/status"""

    PASSED = 'PASSED'
    FAILED = 'FAILED'
    SKIPPED = 'SKIPPED'
    EXEMPTED = 'EXEMPTED'

class AppConfig(object):
    """Represents application build parameters"""

    def __init__(self, name, language, platform, srcpath, buildpath, buildcmd,
                runcmd=None, testmodule=None):
        self.name = name
        self.language = language
        self.platform = platform
        self.srcpath = srcpath
        self.buildpath = buildpath
        self.buildcmd = buildcmd
        self.runcmd = runcmd
        self.testmodule = testmodule

class Application(object):
    """Represents application"""

    def __init__(self, appconfig, kaanode, kaauser):
        self.config = appconfig
        self.kaanode = kaanode
        self.kaauser = kaauser
        self.status = None

        # Pieces of additional code that must be present in build directory.
        # Can be changed via set_dependencies
        self.dependencies = []

    def get_name(self):
        return self.config.name

    def get_language(self):
        return self.config.language

    def get_platform(self):
        return self.config.platform

    def set_dependencies(self, dependencies):
        # 'dependencies' is a list of lists in the next form [[src, dst], ...]
        # where src - file (or directory) that will be copied into dst.
        self.dependencies = dependencies

    def _prepare_build_environment(self):
        """All build preparations are done here"""

        # just copying application code into build directory for now
        # TODO KSF-74 add possibility to download demo code from Sandbox
        copytree(self.config.srcpath, self.config.buildpath)

        # currently, some applications require additional code in build directory
        for item in self.dependencies:
            src = item[0]
            dst = os.path.join(self.config.buildpath, item[1])
            if os.path.isfile(src):
                copy2(src, dst)
            else:
                copytree(src, dst)

        # currently SDK path is hardcoded in application's cmake file
        sdkdir = os.path.join(self.config.buildpath, 'libs/kaa')
        sdkfile = os.path.join(sdkdir, 'kaa-{}-sdk.tar.gz'.format \
                               (self.config.language.lower()))
        os.makedirs(sdkdir)

        profiles = self.kaanode.get_sdk_profiles(self.config.name,
                                                 self.kaauser)

        # For now all sample applications use only one SDK profile.
        # This may be changed in future.
        id = profiles[0]['id']
        self.kaanode.download_sdk(id, self.config.language,
                                  self.kaauser, sdkfile)

    def build(self):
        self._prepare_build_environment()

        cwd = os.getcwd()
        os.chdir(self.config.buildpath)

        try:
            process = subprocess.Popen(self.config.buildcmd, shell=True,
                                       stdout=subprocess.PIPE,
                                       stderr=subprocess.STDOUT,
                                       executable='/bin/bash')
            for line in iter(process.stdout.readline, ""):
                sys.stdout.write(line)
                sys.stdout.flush()
            process.wait()

            if process.returncode != 0:
                raise Exception('Build failed')  

        finally:
            os.chdir(cwd)

    def set_exempted(self):
        self.status = TestStatus.EXEMPTED

    def set_skipped(self):
        self.status = TestStatus.SKIPPED

    def test(self):
        pass

class AppTesterFramework(object):
    """Simple test framework for Kaa sample applications"""

    def __init__(self, config_file, kaanode, kaauser, rootpath, testdir, sandboxframe):

        self.kaanode = kaanode
        self.kaauser = kaauser
        self.testdir = testdir
        self.rootpath = rootpath
        self.sandboxframe = sandboxframe
        self.result_matrix = {}
        self.table_data = [['Application', 'Build', 'Test']]

        # list of applications that must be skipped during building ant testing
        self.skipped = []

        self.applications = self._create_applications(config_file)

    def _create_applications(self, config_file):
        """All the parsing of the configuration file is done here"""

        applications = []
        config = yaml.load(open(config_file).read())

        for app in config:
            name = config[app]['name']
            languages = config[app]['language']

            # for all supported languages
            for language in languages:
                platforms = languages[language]['platform']
                if language == 'c':
                    lang = KaaSDKLanguage.C
                elif language == 'cpp':
                    lang = KaaSDKLanguage.CPP
                elif language == 'java':
                    lang = KaaSDKLanguage.JAVA
                elif language == 'objc':
                    lang = KaaSDKLanguage.OBJECTIVE_C
                else:
                    raise ValueError('Unrecognized SDK language: "{}"'.format(language))

                # for all supported platforms
                for platform in platforms:
                    buildcmd = platforms[platform]['buildcmd']

                    # TODO APP-53 Add runcmd and testmodule
                    buildpath = os.path.join(self.testdir, app,
                                             language, platform)
                    srcpath = os.path.join(self.rootpath,
                                           languages[language]['src'])

                    deps = platforms[platform].get('dependencies', None)
                    status = platforms[platform].get('status', None)

                    appconfig = AppConfig(name, lang, platform,
                                          srcpath, buildpath, buildcmd)
                    application = Application(appconfig,
                                              self.kaanode, self.kaauser)
                    if deps:
                        for item in deps:
                            item[0] = os.path.join(self.rootpath, item[0])
                        application.set_dependencies(deps)

                    applications.append(application)

                    if status and status == TestStatus.EXEMPTED:
                        application.set_exempted()

                    if status and status == TestStatus.SKIPPED:
                        application.set_skipped()

        return applications

    def build_applications(self, name=None):
        for app in self.applications:
            # build only specific application
            if name and app.get_name() != name:
                continue
            try:
                if app.status == TestStatus.SKIPPED:
                    self.result_matrix[app] = TestStatus.SKIPPED
                    continue
                else:
                    print 'Building{} ({}) for {}\n'.format(app.get_name(),
                                                       app.get_language(),
                                                       app.get_platform())
                    app.build()
                    self.result_matrix[app] = TestStatus.PASSED
            except Exception as ex:
                traceback.print_exc(ex, file=sys.stdout)
                if app.status == TestStatus.EXEMPTED:
                    self.result_matrix[app] = TestStatus.EXEMPTED
                else:
                    self.result_matrix[app] = TestStatus.FAILED

        for app in self.result_matrix:
            app_data = '{} ({}) for {}:'.format(app.get_name(), app.get_language(),
                        app.get_platform())
            build_result = [app_data, self.result_matrix[app], 'N/A']
            self.table_data.append(build_result)

    def build_android_java_demo(self):
        output = self.sandboxframe.get_demo_projects()
        for item in output:
            try:
                if 'java' in item['id'] or 'android' in item['id']:                    
                    if item['destBinaryFile'] == '':
                        continue
                    else:
                        build_app = self.sandboxframe.build_android_java_demo(item['id'])
                        build_app_result = self.sandboxframe.is_build_successful(item['id'])

                        if build_app_result:
                            self.result_matrix[item['name']] = TestStatus.PASSED
                            print 'Building {}:\n{}'.format(item['name'], build_app)

                        elif not build_app_result:
                            self.result_matrix[item['name']] = TestStatus.FAILED
                            print 'Building {}:\n{}'.format(item['name'], build_app)

                        else:
                            print 'Unexpected result for {}'.format(item['name'])

            except Exception as ex:
                print type(ex), ex

        for app in self.result_matrix:
            if isinstance(app, Application):
                continue
            else:
                build_result = [app, self.result_matrix[app], 'N/A']
                self.table_data.append(build_result)

    def test_applications(self):
        # TODO APP-53 add testing
        pass

    def process_results(self, output=False):
        passed = True

        # TODO APP-53 Add test results

        for app in self.result_matrix:
            if self.result_matrix[app] == TestStatus.FAILED:
                passed = False
            if output:                   
                table = AsciiTable(self.table_data)
        print table.table

        return passed

def console_args_parser():
    parser = argparse.ArgumentParser(description='Sample Application tester')

    #added default value of current directory.
    parser.add_argument('rootpath',
                        help='path to sample applications repository', nargs='?') 
    parser.add_argument('-l',
                        '--list', help='show available applications and exit',
                        action='store_true')
    parser.add_argument('-a', metavar='application',
                        help='specify application')
    parser.add_argument('-j', help='build java/android applications', 
                        action='store_true')
    parser.add_argument('-c', help='build c/cpp applications',
                        action='store_true')
    parser.add_argument('-s', metavar='server',
                        type=str, help='Kaa server address')
    parser.add_argument('-p', metavar='port',
                        type=str, help='Kaa server port')
    parser.add_argument('--wait-timeout', metavar='wait_timeout',
                        type=int, default=5,
                        help='Waiting timeout for Kaa REST server (default: 5 seconds)')

    return parser

def main():

    config = yaml.load(open(CONFIGFILE).read())
    appconfig_file = os.path.join(os.path.dirname(__file__),
                                  config['appconfig'])
    appconfig = yaml.load(open(appconfig_file).read())

    parser = console_args_parser()
    args = parser.parse_args()

    if args.list:
        print 'Available applications are:'
        for app in appconfig:
            print app
        sys.exit(1)
    elif not args.rootpath:
        parser.print_help()
        sys.exit(1)

    name = None
    # user specify an application to test
    if args.a:
        if args.a not in appconfig:
            print 'Application "%s" was not found'%args.a
            sys.exit(1)
        name = appconfig[args.a]['name']

    host = args.s if args.s else config['host']
    port = args.p if args.p else config['port']

    kaauser = KaaUser(config['user'], config['password'])
    kaanode = KaaNode(host, port)
    builddir = config['builddir']

    sandboxframe = SandboxFrame(host, 9080)

    # clear build directory
    rmtree(builddir, ignore_errors=True)

    kaanode.wait_for_server(args.wait_timeout)

    tester = AppTesterFramework(appconfig_file, kaanode, kaauser,
                                args.rootpath, builddir, sandboxframe)
    if args.j:
        tester.build_android_java_demo()
    elif args.c:
        tester.build_applications(name)
    else:
        tester.build_applications(name)
        tester.build_android_java_demo()

    if tester.process_results(True):
        sys.exit(0)
    else:
        sys.exit(1)
 

if __name__ == "__main__":
    main()
