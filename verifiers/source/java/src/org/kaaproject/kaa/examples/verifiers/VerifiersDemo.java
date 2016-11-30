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

import org.kaaproject.kaa.client.*;
import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.channel.failover.FailoverDecision;
import org.kaaproject.kaa.client.channel.failover.FailoverStatus;
import org.kaaproject.kaa.client.channel.failover.strategies.DefaultFailoverStrategy;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.registration.AttachEndpointToUserCallback;
import org.kaaproject.kaa.client.logging.LogStorage;
import org.kaaproject.kaa.demo.verifiersdemo.MessageEvent;
import org.kaaproject.kaa.demo.verifiersdemo.VerifiersDemoEventClassFamily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A demo application that shows how to use the Kaa endpoint ownership API.
 */
public class VerifiersDemo {
    private static final Logger LOG = LoggerFactory.getLogger(VerifiersDemo.class);
    private static final String KEYS_DIR = "verifiers_keys_for_java_demo";

    private static KaaClient kaaClient;

    public static void main(String[] args) throws Exception {
        /*
         * Setup working directory for endpoint
         */
        KaaClientProperties endpointProperties = new KaaClientProperties();
        endpointProperties.setWorkingDirectory(KEYS_DIR);

        /*
         * Create the Kaa desktop context for the application.
         */
        DesktopKaaPlatformContext desktopKaaPlatformContext = new DesktopKaaPlatformContext(endpointProperties);

        /*
         * Create a Kaa client and add a listener which displays the Kaa client
         * endpoint ID (represented by EP key hash), when the Kaa client is started.
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
         * Set listener that listen for event when current endpoint will be attached to user.
         */
        kaaClient.setAttachedListener(new AttachEndpointToUserCallback() {
            @Override
            public void onAttachedToUser(String userExternalId, String endpointAccessToken) {
                LOG.info("--= Endpoint was attached to user. =--");
                LOG.info("User external ID: {}, returned access token: {}", userExternalId, endpointAccessToken);
            }
        });

        /*
         * Add message listener to receive test messages.
         */
        addMessageListener();

        /*
         * Start the Kaa client and connect it to the Kaa server. Then await for client starting
         */
        kaaClient.start();
        sleepForSeconds(5);

        LOG.info("Endpoint ID:" + kaaClient.getEndpointKeyHash());
        LOG.info("Endpoint access token:" + kaaClient.getEndpointAccessToken());
        LOG.info("Copy this token to mobile application in order to do assisted attach of this endpoint to user (current mobile application owner).");

        /*
         * Await for user press "Enter" key
         */
        readSymbol();

        /*
         * Stop the Kaa client and connect it to the Kaa server.
         */
        LOG.info("Stopping client...");
        kaaClient.stop();
    }

    /**
     * Add message listener to receive messages in the form of events.
     */
    private static void addMessageListener() {
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
    }

    /**
     * Awaits for any user input (actually for pressing "Enter" key).
     */
    private static void readSymbol() {
        try {
            System.in.read();
        } catch (IOException e) {
            LOG.error("IOException has occurred: " + e.getMessage());
        }
    }

    /**
     * Stops current thread for specified timeout.
     * @param seconds timeout to wait in seconds.
     */
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
