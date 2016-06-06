package org.kaaproject.kaa.demo.cityguide.kaa;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaClientPlatformContext;
import org.kaaproject.kaa.client.configuration.base.SimpleConfigurationStorage;
import org.kaaproject.kaa.demo.cityguide.Area;
import org.kaaproject.kaa.demo.cityguide.AvailableArea;
import org.kaaproject.kaa.demo.cityguide.CityGuideConfig;

import java.util.List;

public class KaaConfigurationSlave {

    private static final String CONFIG_FILE_NAME = "cityGuideConfig.data";
    private CityGuideConfig mConfig;

    /**
     * Set a configuration storage file to persist configuration.
     */
    public void createConfigurationStorage(KaaClientPlatformContext clientPlatformContext, KaaClient client) {
        client.setConfigurationStorage(new SimpleConfigurationStorage(
                clientPlatformContext, CONFIG_FILE_NAME));

    }

    public void update(CityGuideConfig config) {
        this.mConfig = config;
    }

    public List<AvailableArea> getAvailableAreas() {
        return mConfig.getAvailableAreas();
    }

    public List<Area> getAreas() {
        return mConfig.getAreas();
    }
}
