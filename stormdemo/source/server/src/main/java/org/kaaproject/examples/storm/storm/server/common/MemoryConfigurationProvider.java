/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.examples.storm.storm.server.common;

import org.apache.flume.annotations.InterfaceAudience;
import org.apache.flume.annotations.InterfaceStability;
import org.apache.flume.conf.FlumeConfiguration;
import org.apache.flume.node.AbstractConfigurationProvider;

import java.util.Map;

@InterfaceAudience.Private
@InterfaceStability.Unstable
class MemoryConfigurationProvider extends AbstractConfigurationProvider {
    private final Map<String, String> properties;

    MemoryConfigurationProvider(String name, Map<String, String> properties) {
        super(name);
        this.properties = properties;
    }

    @Override
    protected FlumeConfiguration getFlumeConfiguration() {
        return new FlumeConfiguration(properties);
    }
}
