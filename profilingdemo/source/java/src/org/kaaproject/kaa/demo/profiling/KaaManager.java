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

package org.kaaproject.kaa.demo.profiling;

import org.kaaproject.examples.pager.PagerClientProfile;
import org.kaaproject.examples.pager.PagerConfiguration;
import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.configuration.base.ConfigurationListener;
import org.kaaproject.kaa.client.configuration.base.SimpleConfigurationStorage;
import org.kaaproject.kaa.client.profile.ProfileContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Class that shows Kaa profiling API, works with endpoints and it's profiles
 *
 * @author Maksym Liashenko
 * @see <a href="http://docs.kaaproject.org/display/KAA/Endpoint+profiling">Profiling API</a>
 */
public class KaaManager {

    public static final int KAA_CLIENT_NUMBER = 8;

    /**
     * In this dir all Kaa clients creates it's keys for starting
     */
    public static final String PROPERTIES_OUT_KEYS_DIR = "./res/out/kaaTempDir";

    private static final Logger LOG = LoggerFactory.getLogger(KaaManager.class);

    private ArrayList<KaaClient> kaaClients = new ArrayList<>();

    public void startKaaClient(final int index) throws IOException {

        KaaClientProperties kaaClientProperties = new KaaClientProperties();
        kaaClientProperties.setWorkingDirectory(KaaManager.PROPERTIES_OUT_KEYS_DIR + index);

        // Create the Kaa desktop context for the application.
        DesktopKaaPlatformContext desktopKaaPlatformContext = new DesktopKaaPlatformContext(kaaClientProperties);

        // Create a Kaa client and add a listener which displays the Kaa client profiling
        // as soon as the Kaa client is started.
        final KaaClient kaaClient = Kaa.newClient(desktopKaaPlatformContext, new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {
                super.onStarted();

                displayConfiguration(index);
            }
        }, true);
        kaaClients.add(kaaClient);

        kaaClient.setProfileContainer(new ProfileContainer() {
            @Override
            public PagerClientProfile getProfile() {
                PagerProfileData.InnerData data = PagerProfileData.init().get(index);

                PagerClientProfile pagerClientProfile = new PagerClientProfile();
                pagerClientProfile.setAudioSupport(data.isAudio());
                pagerClientProfile.setVibroSupport(data.isVibro());
                pagerClientProfile.setVideoSupport(data.isVideo());

                return pagerClientProfile;
            }
        });

        // Persist profiling in a local storage to avoid downloading it each time the Kaa client is started.
        kaaClient.setConfigurationStorage(new SimpleConfigurationStorage(desktopKaaPlatformContext, "saved_config.cfg"));

        // Add a listener which displays the Kaa client profiling each time it is updated.
        kaaClient.addConfigurationListener(new ConfigurationListener() {
            @Override
            public void onConfigurationUpdate(PagerConfiguration configuration) {
                displayConfiguration(index);
            }
        });

        // Start the Kaa client and connect it to the Kaa server.
        kaaClient.start();

    }

    public void stopKaaClients() throws IOException {
        LOG.info("Stopping clients...");
        for (KaaClient k : kaaClients) {
            if (k != null)
                k.stop();
        }
    }

    public void displayConfiguration(int index) {
        if (index >= kaaClients.size()) {
            LOG.error("Index more than KaaClient's list size!");
            return;
        }

        KaaClient kaaClient = kaaClients.get(index);
        if (kaaClient != null) {
            PagerConfiguration configuration = kaaClient.getConfiguration();

            boolean isAudioSupport = configuration.getAudioSupportEnabled();
            boolean isVideoSupport = configuration.getVideoSupportEnabled();
            boolean isVibroSupport = configuration.getVibroSupportEnabled();

            LOG.info("KeyHash - " + kaaClient.getEndpointKeyHash());

            LOG.info(index + "-th profiling body (have vibro-, audio-, video-support):");
            LOG.info("{} - {} - {}", isVibroSupport, isAudioSupport, isVideoSupport);
        } else {
            LOG.info("Kaa Client is null!");
        }
    }

}
