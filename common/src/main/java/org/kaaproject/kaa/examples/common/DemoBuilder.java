/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.examples.common;

import org.apache.commons.lang3.tuple.Pair;
import org.kaaproject.kaa.examples.common.projects.ProjectsConfig;
import org.kaaproject.kaa.examples.util.cmd.CommandLine;
import org.kaaproject.kaa.server.common.admin.AdminClient;

import java.util.stream.Stream;

public interface DemoBuilder {

    void buildDemoApplication(AdminClient client) throws Exception;

    ProjectsConfig getProjectConfigs();

    default Stream<Pair<CommandLine, String>> getAdditionalCommandsAndParams() {
        return Stream.empty();
    }
    
}
