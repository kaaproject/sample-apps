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

package org.kaaproject.kaa.examples.verifiers;

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.channel.failover.FailoverDecision;
import org.kaaproject.kaa.client.channel.failover.FailoverStatus;
import org.kaaproject.kaa.client.channel.failover.strategies.DefaultFailoverStrategy;
import org.kaaproject.kaa.client.configuration.base.SimpleConfigurationStorage;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.profile.ProfileContainer;
import org.kaaproject.kaa.demo.verifiersdemo.MessageEvent;
import org.kaaproject.kaa.demo.verifiersdemo.VerifiersDemoEventClassFamily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A demo application that shows how to use the Kaa endpoint ownership API.
 */
public class VerifiersDemo {
    private static final Logger LOG = LoggerFactory.getLogger(VerifiersDemo.class);
    private static KaaClient kaaClient;

    public static void main(String[] args) throws InterruptedException {
        /*
         * Create the Kaa desktop context for the application.
         */
        DesktopKaaPlatformContext desktopKaaPlatformContext = new DesktopKaaPlatformContext();

        /*
         * Create a Kaa client and add a listener which displays the Kaa client
         * endpoint key hash, when the Kaa client is started.
         */
        kaaClient = Kaa.newClient(desktopKaaPlatformContext, new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {
                //super.onStarted();
                LOG.info("--= Kaa client started =--");
            }
        }, true);

        kaaClient.setFailoverStrategy(new CustomFailoverStrategy());

        /*
         * Start the Kaa client and connect it to the Kaa server.
         */
        kaaClient.start();

        // await for client starting
        sleepForSeconds(5);
        LOG.info("Endpoint ID (key hash):" + kaaClient.getEndpointKeyHash());
        LOG.info("Endpoint access token:" + kaaClient.getEndpointAccessToken());
        LOG.info("Copy this token to mobile application in order to do assisted attach of this endpoint to user (current mobile application owner).");

        //Obtain the event family factory, then event family
        final EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
        final VerifiersDemoEventClassFamily eventFamily = eventFamilyFactory.getVerifiersDemoEventClassFamily();

        // Add event listeners to the family factory.
        eventFamily.addListener(new VerifiersDemoEventClassFamily.Listener() {

            @Override
            public void onEvent(MessageEvent messageEvent, String senderId) {
                LOG.info("MessageEvent event received! Message: {}", messageEvent.getMessage());
            }

        });

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


    private static void sleepForSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Extended from {@link DefaultFailoverStrategy}. Give a possibility to manage device behavior when it can't goes
     * verification process on Kaa node service on Sandbox
     */
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
