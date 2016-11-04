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

package org.kaaproject.kaa.demo.cityguide.kaa;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaClientPlatformContext;
import org.kaaproject.kaa.client.configuration.base.SimpleConfigurationStorage;
import org.kaaproject.kaa.demo.cityguide.Area;
import org.kaaproject.kaa.demo.cityguide.AvailableArea;
import org.kaaproject.kaa.demo.cityguide.CityGuideConfig;

import java.util.List;

/**
 * Save user configuration information
 *
 * @see <a href="http://docs.kaaproject.org/display/KAA/Configuration">Kaa Configuration Docs</a>
 */
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
