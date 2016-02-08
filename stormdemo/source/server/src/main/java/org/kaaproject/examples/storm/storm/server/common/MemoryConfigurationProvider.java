

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
