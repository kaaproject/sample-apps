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

package org.kaaproject.kaa.examples.credentials;

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.channel.failover.FailoverDecision;
import org.kaaproject.kaa.client.channel.failover.FailoverStatus;
import org.kaaproject.kaa.client.channel.failover.strategies.DefaultFailoverStrategy;
import org.kaaproject.kaa.client.configuration.base.SimpleConfigurationStorage;
import org.kaaproject.kaa.client.profile.ProfileContainer;
import org.kaaproject.kaa.schema.system.EmptyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A demo application that shows how to use the Kaa credentials API.
 * z
 *
 * @author Maksym Liashenko
 */
public class CredentialsDemo {
    private static final Logger LOG = LoggerFactory.getLogger(CredentialsDemo.class);
    private static KaaClient kaaClient;

    public static void main(String[] args) throws InterruptedException {
/*
         * Create the Kaa desktop context for the application.
         */
        DesktopKaaPlatformContext desktopKaaPlatformContext = new DesktopKaaPlatformContext();

        /*
         * Create a Kaa client and add a listener which displays the Kaa client
         * configuration as soon as the Kaa client is started.
         */
        kaaClient = Kaa.newClient(desktopKaaPlatformContext, new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {
                super.onStarted();
                LOG.info("Kaa client started");

                LOG.info("Endpoint ID:" + kaaClient.getEndpointKeyHash());

            }
        }, false);
        kaaClient.setProfileContainer(new ProfileContainer() {
            @Override
            public EmptyData getProfile() {
                return new EmptyData();
            }
        });

        kaaClient.setFailoverStrategy(new CustomFailoverStrategy());
        /*
         * Persist configuration in a local storage to avoid downloading it each
         * time the Kaa client is started.
         */
        kaaClient.setConfigurationStorage(new SimpleConfigurationStorage(desktopKaaPlatformContext, "saved_config.cfg"));

        /*
         * Start the Kaa client and connect it to the Kaa server.
         */
        kaaClient.start();

        readSymbol();
        LOG.info("Stopping client...");
        /*
         * Stop the Kaa client and connect it to the Kaa server.
         */
        kaaClient.stop();
    }

    private static void readSymbol() {
        try {
            System.in.read();
        } catch (IOException e) {
            LOG.error("IOException has occurred: " + e.getMessage());
        }
    }

    private static class CustomFailoverStrategy extends DefaultFailoverStrategy {

        @Override
        public FailoverDecision onFailover(FailoverStatus failoverStatus) {
            LOG.info("Failover... - " + failoverStatus);
            switch (failoverStatus) {
                case ENDPOINT_VERIFICATION_FAILED:
                    LOG.info("\nCREDENTIALS IS NOT PROVISIONING! EXIT...\n");
                    System.exit(0);
                default:
                    return super.onFailover(failoverStatus);
            }
        }
    }
}
