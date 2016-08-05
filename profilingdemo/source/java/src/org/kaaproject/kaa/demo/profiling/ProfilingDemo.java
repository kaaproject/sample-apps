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
 * A demo application class that use the Kaa endpoint profiling and grouping API.
 *
 * @author Maksym Liashenko
 */
public class ProfilingDemo {

    private static final Logger LOG = LoggerFactory.getLogger(ProfilingDemo.class);

    public static final int KAA_CLIENT_NUMBER = 3;
    /**
     * In this dir all Kaa clients creates it's keys for starting
     */
    public static final String PROPERTIES_OUT_KEYS_DIR = "./res/out/kaaTempDir";

    private static ArrayList<KaaClient> kaaClients = new ArrayList<>();
    private static ArrayList<InnerData> profiles = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        LOG.info("Profiling demo started");

        for (int i = 0; i < KAA_CLIENT_NUMBER; i++) {
            try {
                startKaaClient(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        LOG.info("--= Press any key to exit =--");
        try {
            System.in.read();
        } catch (IOException e) {
            LOG.error("IOException was caught - ", e);
        }

        // Stop the Kaa client and release all the resources which were in use.
        try {
            stopKaaClients();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOG.info("Profiling demo stopped");
    }

    public static void startKaaClient(final int index) throws IOException {

        KaaClientProperties kaaClientProperties = new KaaClientProperties();
        kaaClientProperties.setWorkingDirectory(PROPERTIES_OUT_KEYS_DIR + index);

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
                InnerData data = init().get(index);

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

    public static void stopKaaClients() throws IOException {
        LOG.info("Stopping clients...");
        for (KaaClient k : kaaClients) {
            if (k != null)
                k.stop();
        }
    }

    public static void displayConfiguration(int index) {
        if (index >= kaaClients.size()) {
            LOG.error("Index more than KaaClient's list size!");
            return;
        }

        KaaClient kaaClient = kaaClients.get(index);
        if (kaaClient != null) {
            PagerConfiguration configuration = kaaClient.getConfiguration();

            boolean isAudioSupport = configuration.getAudioSubscriptionActive();
            boolean isVideoSupport = configuration.getVideoSubscriptionActive();
            boolean isVibroSupport = configuration.getVibroSubscriptionActive();

            LOG.info("KeyHash - " + kaaClient.getEndpointKeyHash());

            LOG.info(index + "-th profiling body (have vibro-, audio-, video-support):");
            LOG.info("{} - {} - {}", isVibroSupport, isAudioSupport, isVideoSupport);
        } else {
            LOG.info("Kaa Client is null!");
        }
    }


    public static ArrayList<InnerData> init() {
        if (!profiles.isEmpty()) {
            return profiles;
        }

        profiles.add(new InnerData(false, false, true));
        profiles.add(new InnerData(true, false, true));
        profiles.add(new InnerData(true, true, true));

        return profiles;
    }

    static class InnerData {
        private boolean isVibro;
        private boolean isAudio;
        private boolean isVideo;

        public InnerData(boolean isAudio, boolean isVibro, boolean isVideo) {
            this.isVideo = isVideo;
            this.isAudio = isAudio;
            this.isVibro = isVibro;
        }

        public boolean isVibro() {
            return isVibro;
        }

        public boolean isAudio() {
            return isAudio;
        }

        public boolean isVideo() {
            return isVideo;
        }
    }
}

