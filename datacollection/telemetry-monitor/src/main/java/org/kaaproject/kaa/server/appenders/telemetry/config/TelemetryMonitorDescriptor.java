/**
 * Copyright 2014-2016 CyberVision, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.appenders.telemetry.config;
import org.apache.avro.Schema;
import org.kaaproject.kaa.server.appenders.telemetry.appender.TelemetryMonitor;
import org.kaaproject.kaa.server.appenders.telemetry.config.gen.TelemetryMonitorConfiguration;
import org.kaaproject.kaa.server.common.plugin.KaaPluginConfig;
import org.kaaproject.kaa.server.common.plugin.PluginConfig;
import org.kaaproject.kaa.server.common.plugin.PluginType;

/**
 *
 * Sample descriptor for {@link TelemetryMonitor} appender.
 *
 */
@KaaPluginConfig(pluginType = PluginType.LOG_APPENDER)
public class TelemetryMonitorDescriptor implements PluginConfig {
    public TelemetryMonitorDescriptor() {
    }
    /**
     * Name of the appender will be used in Admin UI
     */
    @Override
    public String getPluginTypeName() {
        return "Telemetry Monitor";
    }
    /**
     * Returns name of the appender class.
     */
    @Override
    public String getPluginClassName() {
        return "org.kaaproject.kaa.server.appenders.telemetry.appender.TelemetryMonitor";
    }
    /**
     * Returns avro schema of the appender configuration.
     */
    @Override
    public Schema getPluginConfigSchema() {
        return TelemetryMonitorConfiguration.getClassSchema();
    }
}
