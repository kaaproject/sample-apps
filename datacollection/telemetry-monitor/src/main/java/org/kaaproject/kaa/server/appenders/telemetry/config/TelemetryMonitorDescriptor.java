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