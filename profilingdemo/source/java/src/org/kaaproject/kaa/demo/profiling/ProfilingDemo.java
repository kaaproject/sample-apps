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
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * A demo application class that use the Kaa endpoint profiling and grouping API.
 */
public class ProfilingDemo {

    private static final Logger LOG = LoggerFactory.getLogger(ProfilingDemo.class);

    // Path for directory to store endpoints working subdirectories
    private static final String KAA_CLIENT_WORKING_DIR = "./res/kaaClientWorkingDir";

    private static List<KaaClient> kaaClients = new ArrayList<>();

    // Client-side profiles list, for creating endpoints
    private static List<PagerClientProfile> profiles = new ArrayList<>();

    static {
        profiles.add(new PagerClientProfile(false, false, true));
        profiles.add(new PagerClientProfile(true, false, true));
        profiles.add(new PagerClientProfile(true, true, true));
    }

    public static void main(String[] args) throws IOException {
        LOG.info("Profiling demo started");

        // Creating endpoints using KaaClient class
        kaaClients = createKaaClients(profiles);

        // Start
        for (KaaClient client: kaaClients) {
            client.start();
        }

        // Waiting for clients to start and display theirs profiles/configuration
        sleepForSeconds(5);

        waitForAnyKeyPress();

        // Stop the Kaa clients and release all the resources
        stopKaaClients(kaaClients);

        LOG.info("Profiling demo stopped");
    }


    private static List<KaaClient> createKaaClients(List<PagerClientProfile> profiles) throws IOException {
        List<KaaClient> clients = new ArrayList<>();
        for (int i = 0; i < profiles.size(); i++) {
            try {
                KaaClient client = createKaaClient(i);
                clients.add(client);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return clients;
    }

    private static KaaClient createKaaClient(final int index) throws IOException {

        // Setup separate working folder for endpoint
        KaaClientProperties kaaClientProperties = new KaaClientProperties();
        kaaClientProperties.setWorkingDirectory(KAA_CLIENT_WORKING_DIR + index);

        // Create the Kaa desktop context for the application.
        DesktopKaaPlatformContext desktopKaaPlatformContext = new DesktopKaaPlatformContext(kaaClientProperties);

        // Create a Kaa client with listener which displays endpoint's profile/configuration data after client start
        final KaaClient kaaClient = Kaa.newClient(desktopKaaPlatformContext, new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {
                super.onStarted();
                displayConfiguration(index);
            }
        }, true);

        // Setup endpoint profile
        kaaClient.setProfileContainer(new ProfileContainer() {
            @Override
            public PagerClientProfile getProfile() {
                return profiles.get(index);
            }
        });

        // Persist configuration in a local storage to avoid downloading it each time the Kaa client is started.
        kaaClient.setConfigurationStorage(new SimpleConfigurationStorage(desktopKaaPlatformContext, KAA_CLIENT_WORKING_DIR + index + "/saved_config.cfg"));

        //Add a listener to display endpoint's profile/configuration data each time configuration is updated.
        kaaClient.addConfigurationListener(new ConfigurationListener() {
            @Override
            public void onConfigurationUpdate(PagerConfiguration configuration) {
                LOG.info("Endpoint #{} configuration was updated.", index);
                displayConfiguration(index);
            }
        });

        return kaaClient;
    }

    private static void sleepForSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void displayConfiguration(int index) {
        if (index >= kaaClients.size()) {
            LOG.error("Endpoint #{} not found: Kaa client #{} not found.", index, index);
            return;
        }

        KaaClient kaaClient = kaaClients.get(index);
        if (kaaClient != null) {

            LOG.info("Endpoint #{} data:", index);
            LOG.info("KeyHash - {}", kaaClient.getEndpointKeyHash());

            PagerClientProfile profile = profiles.get(index);
            LOG.info("Client-side endpoint profile (audio/video/vibro support): {} - {} - {}",
                            profile.getAudioSupport(), profile.getVideoSupport(), profile.getVibroSupport());

            PagerConfiguration conf = kaaClient.getConfiguration();
            LOG.info("Configuration (is audio/video/vibro subscription active): {} - {} - {}",
                            conf.getAudioSubscriptionActive(), conf.getVideoSubscriptionActive(), conf.getVibroSubscriptionActive());

        } else {
            LOG.info("Kaa Client is null!");
        }
    }

    private static void waitForAnyKeyPress() {
        LOG.info("--= Press any key to exit =--");
        try {
            System.in.read();
        } catch (IOException e) {
            LOG.error("IOException was caught - ", e);
        }
    }

    private static void stopKaaClients(List<KaaClient> clients) {
        LOG.info("Stopping clients...");
        for (KaaClient client : clients) {
            try {
                client.stop();
            } catch (Exception e) { // catch NullPointerException as well as IOException
                e.printStackTrace();
            }
        }
    }
}

