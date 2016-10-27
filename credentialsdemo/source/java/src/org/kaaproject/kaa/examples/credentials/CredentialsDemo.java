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
import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.channel.failover.FailoverDecision;
import org.kaaproject.kaa.client.channel.failover.FailoverStatus;
import org.kaaproject.kaa.client.channel.failover.strategies.DefaultFailoverStrategy;
import org.kaaproject.kaa.client.exceptions.KaaRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A demo application that shows how to use the Kaa credentials API.
 */
public class CredentialsDemo {
    private static final Logger LOG = LoggerFactory.getLogger(CredentialsDemo.class);
    private static KaaClient kaaClient;

    public static void main(String[] args) throws InterruptedException, IOException {

        LOG.info("Credentials demo started");

        /*
         * Create the Kaa desktop context for the application.
         */
        DesktopKaaPlatformContext desktopKaaPlatformContext = new DesktopKaaPlatformContext();

        try {
        /*
         * Create a Kaa client and add a listener which displays the Kaa client
         * endpoint key hash, when the Kaa client is started.
         */
            kaaClient = Kaa.newClient(desktopKaaPlatformContext, new SimpleKaaClientStateListener() {
                @Override
                public void onStarted() {
                    super.onStarted();
                    LOG.info("Kaa client started");
                    LOG.info("Endpoint ID:" + kaaClient.getEndpointKeyHash());
                }
            }, false);


            kaaClient.setFailoverStrategy(new CustomFailoverStrategy());

            /*
             * Start the Kaa client and connect it to the Kaa server.
             */
            kaaClient.start();

            TimeUnit.SECONDS.sleep(3);
            LOG.info("Device state: REGISTERED");

            readSymbol();

            /*
             * Stop the Kaa client and connect it to the Kaa server.
             */
            LOG.info("Stopping application.");
            kaaClient.stop();

        } catch (KaaRuntimeException e) {
            LOG.info("Cannot connect to server - no credentials found.");
            LOG.info("Stopping application.");
        }

    }

    private static void readSymbol() {
        try {
            System.in.read();
        } catch (IOException e) {
            LOG.error("IOException has occurred: " + e.getMessage());
        }
    }

    /**
     * Extended from {@link DefaultFailoverStrategy}. Give a possibility to manage device behavior when it can't goes
     * verification process on Kaa node service on Sandbox
     */
    private static class CustomFailoverStrategy extends DefaultFailoverStrategy {

        @Override
        public FailoverDecision onFailover(FailoverStatus failoverStatus) {
            LOG.info("Failover happen. Status: " + failoverStatus);
            switch (failoverStatus) {
                case ENDPOINT_VERIFICATION_FAILED:
                    LOG.info("VERIFICATION FAILED. Credentials is not provisioned!");
                    LOG.info("No operation is performed according to fail-over strategy decision");
                    LOG.info("Stopping application.");
                    System.exit(0);
                default:
                    return super.onFailover(failoverStatus);
            }
        }
    }
}
