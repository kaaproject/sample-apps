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

package org.kaaproject.kaa.examples.credentials.kaa;

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

/**
 * @author Maksym Liashenko
 */
public class KaaClientManager {

    private static final Logger LOG = LoggerFactory.getLogger(KaaClientManager.class);
    private KaaClient kaaClient;

    public void start() {
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
        });
        kaaClient.setProfileContainer(new ProfileContainer() {
            @Override
            public EmptyData getProfile() {
                return new EmptyData();
            }
        });

        kaaClient.setFailoverStrategy(new DefaultFailoverStrategy() {
            @Override
            public FailoverDecision onFailover(FailoverStatus failoverStatus) {
                LOG.info("\n\n\nERROR\n\n\n");
                return super.onFailover(failoverStatus);
            }
        });
        /*
         * Persist configuration in a local storage to avoid downloading it each
         * time the Kaa client is started.
         */
        kaaClient.setConfigurationStorage(new SimpleConfigurationStorage(desktopKaaPlatformContext, "saved_config.cfg"));

        /*
         * Start the Kaa client and connect it to the Kaa server.
         */
        kaaClient.start();
    }

    public void stop() {
        kaaClient.stop();
    }
}
