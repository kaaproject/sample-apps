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
import java.util.concurrent.TimeUnit;

/**
 * A demo application class that use the Kaa endpoint profiling and grouping API.
 */
public class ProfilingDemo {

    private static final Logger LOG = LoggerFactory.getLogger(ProfilingDemo.class);
    /**
     * In this dir all Kaa clients creates it's keys for starting
     */
    public static final String KAA_CLIENT_WORKING_DIR = "./res/kaaClientWorkingDir";

    private static ArrayList<KaaClient> kaaClients = new ArrayList<>();
    /**
     * Collection of client client-side profiles, that change on client side
     */
    private static ArrayList<PagerClientProfile> profiles = new ArrayList<>();

    static {
        profiles.add(new PagerClientProfile(false, false, true));
        profiles.add(new PagerClientProfile(true, false, true));
        profiles.add(new PagerClientProfile(true, true, true));
    }

    public static void main(String[] args) throws IOException {
        LOG.info("Profiling demo started");

        for (int i = 0; i < profiles.size(); i++) {
            try {
                startKaaClient(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LOG.info("--= Press any key to exit =--");
        try {
            System.in.read();
        } catch (IOException e) {
            LOG.error("IOException was caught - ", e);
        }

        /*
         * Stop the Kaa client and release all the resources which were in use.
         */
        try {
            stopKaaClients();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOG.info("Profiling demo stopped");
    }

    public static void startKaaClient(final int index) throws IOException {

        KaaClientProperties kaaClientProperties = new KaaClientProperties();
        kaaClientProperties.setWorkingDirectory(KAA_CLIENT_WORKING_DIR + index);

        /*
         * Create the Kaa desktop context for the application.
         */
        DesktopKaaPlatformContext desktopKaaPlatformContext = new DesktopKaaPlatformContext(kaaClientProperties);

        /*
         * Create a Kaa client and add a listener which displays the Kaa client profiling
         * as soon as the Kaa client is started.
         */
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
                return profiles.get(index);
            }
        });

        /*
         * Persist profiling in a local storage to avoid downloading it each time the Kaa client is started.
         */
        kaaClient.setConfigurationStorage(new SimpleConfigurationStorage(desktopKaaPlatformContext,
                KAA_CLIENT_WORKING_DIR + index + "/saved_config.cfg"));

        /*
         * Add a listener which displays the Kaa client profiling each time it is updated.
         */
        kaaClient.addConfigurationListener(new ConfigurationListener() {
            @Override
            public void onConfigurationUpdate(PagerConfiguration configuration) {
                displayConfiguration(index);
            }
        });

        /*
         * Start the Kaa client and connect it to the Kaa server.
         */
        kaaClient.start();
    }

    public static void stopKaaClients() throws IOException {
        LOG.info("Stopping clients...");
        for (KaaClient k : kaaClients) {
            if (k != null)
                k.stop();
        }
    }

    public synchronized static void displayConfiguration(int index) {
        if (index >= kaaClients.size()) {
            LOG.error("Index more than KaaClient's list size!");
            return;
        }

        KaaClient kaaClient = kaaClients.get(index);
        if (kaaClient != null) {
            PagerClientProfile data = profiles.get(index);
            LOG.info("{}-th profiling body (have audio-, video-, vibration-support):" + index);
            LOG.info("{} - {} - {}", data.getAudioSupport(), data.getVideoSupport(), data.getVibroSupport());

            PagerConfiguration configuration = kaaClient.getConfiguration();

            boolean isAudioSupport = configuration.getAudioSubscriptionActive();
            boolean isVideoSupport = configuration.getVideoSubscriptionActive();
            boolean isVibroSupport = configuration.getVibroSubscriptionActive();

            LOG.info("KeyHash - " + kaaClient.getEndpointKeyHash());

            LOG.info("{}-th profiling body (have audio-, video-, vibration-support):" + index);
            LOG.info("{} - {} - {}", isAudioSupport, isVideoSupport, isVibroSupport);

        } else {
            LOG.info("Kaa Client is null!");
        }
    }
}

