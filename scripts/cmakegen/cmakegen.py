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
import os
import yaml
from string import Template

CMAKE_LISTS_TEMPLATE = open(os.path.join(os.path.dirname(__file__),'CMakeLists.txt')).read()
EMBEDDED_TEMPLATE = open(os.path.join(os.path.dirname(__file__),'Embedded.cmake')).read()
POSIX_TEMPLATE = open(os.path.join(os.path.dirname(__file__),'Posix.cmake')).read()
DEFINITION_TEMPLATE = open(os.path.join(os.path.dirname(__file__),'Definition.cmake')).read()

class CMakeGenTemplate(Template):
    delimiter = '%'

class GeneratorException(Exception):
    pass

class FeaturesGenerator:
    def __init__(self, features):
        self._features = list(features) if features is not None else []

    def generate(self, language):
        if self._features is None:
            return str()
        template_c = 'set(WITH_EXTENSION_%extension OFF)\n'
        template_cpp = 'set(KAA_WITHOUT_%extension 1)\n'
        if language == 'c':
            return self._generate_from_template(template_c)
        elif language == 'cpp':
            return self._generate_from_template(template_cpp)
        else:
            raise GeneratorException('Unknown language: '+language)

    def _generate_from_template(self, template):
        features = ['configuration', 'notifications', 'logging', 'events']
        output = ''
        for f in features:
            if not f in self._features:
                output = output + CMakeGenTemplate(template).substitute(extension = f.upper())
        return output


class DefinitionsGenerator:
    def __init__(self, definitions):
        self._definitions = dict(definitions) if definitions is not None else dict()

    def add_definitions(self, **kwargs):
        self._definitions.update(kwargs)

    def generate(self):
        out = str()
        for k, v in self._definitions.iteritems():
            out = out + CMakeGenTemplate(DEFINITION_TEMPLATE).safe_substitute(variable=k, value=v)
        return out

class Generator:
    def __init__(self, basedir, appname, path, languages, **kwargs):
        self._appname = str(appname)
        self._path = os.path.abspath(os.path.join(basedir, str(path)))
        self._languages = list(languages)
        self._build_embedded = kwargs.get('build_embedded')
        self._features = list(kwargs.get('features', []))
        self._definitions = dict(kwargs.get('definitions', {}))
        if self._build_embedded and not 'c' in self._languages:
            raise GeneratorException('build_embedded option is supported only for C projects')


    def generate(self):
        for lang in self._languages:
            with open(os.path.join(self._path ,lang, 'CMakeLists.txt'),'w+') as f:
                f.write(self._generate_for_language(lang))
            print self._generate_for_language(lang)

    def _generate_for_language(self, language):
        applibs = 'kaac' if language == 'c' else 'kaacpp'

        if self._build_embedded and language == 'c':
            applibs = applibs + ' target_support'

        definitions_generator = DefinitionsGenerator(self._definitions)
        features_generator = FeaturesGenerator(self._features)

        if self._build_embedded and language == 'c':
            target_decl_template = EMBEDDED_TEMPLATE
            definitions_generator.add_definitions(
                    WIFI_SSID = 'WiFi SSID',
                    WIFI_PWD = 'Password',
                    KAA_TARGET = 'posix')
        else:
            target_decl_template = POSIX_TEMPLATE

        base_params = {
            'APP_NAME': self._appname,
            'APP_LANG': 'C' if language == 'c' else 'CXX',
            'APP_LIBRARIES': applibs,
            'APP_SOURCES': 'src/kaa_demo.c' if language == 'c' else 'src/KaaDemo.cpp'
        }

        section_params = {
            'KAA_FEATURES': features_generator.generate(language),
            'DEMO_DEFINITIONS': definitions_generator.generate(),
            'DEFINE_EXECUTABLE': CMakeGenTemplate(target_decl_template).safe_substitute(base_params)
        }

        return CMakeGenTemplate(CMAKE_LISTS_TEMPLATE).safe_substitute(dict(base_params, **section_params))

if __name__ == "__main__":
    config = yaml.load(open(os.path.join(os.path.dirname(__file__), 'config.yaml')).read())

    if len(sys.argv) != 2:
        sys.exit('Usage: %s base_dir' % sys.argv[0])


    for app in config:
        appcfg = config[app]
        print '='*50
        print 'Generating CMakeLists.txt for '+ appcfg['appname']
        print '='*50
        Generator(sys.argv[1],
            appcfg.pop('appname'),
            appcfg.pop('path'),
            appcfg.pop('languages'),
            **appcfg).generate()

