

package common;

import org.apache.flume.node.MaterializedConfiguration;

import java.util.Map;

/**
 Class for testing
 */
public class MaterializedConfigurationProvider {
    public MaterializedConfiguration get(String name, Map<String, String> properties) {
        MemoryConfigurationProvider confProvider =
                new MemoryConfigurationProvider(name, properties);
        return confProvider.getConfiguration();
    }
}
