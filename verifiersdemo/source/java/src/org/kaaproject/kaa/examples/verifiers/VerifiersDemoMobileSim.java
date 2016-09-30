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
import org.kaaproject.kaa.client.event.EndpointAccessToken;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.registration.OnAttachEndpointOperationCallback;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.demo.verifiersdemo.MessageEvent;
import org.kaaproject.kaa.demo.verifiersdemo.VerifiersDemoEventClassFamily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A demo application that shows how to use the Kaa endpoint ownership API.
 */
public class VerifiersDemoMobileSim {
    private static final Logger LOG = LoggerFactory.getLogger(VerifiersDemoMobileSim.class);

    //Credentials for attaching an endpoint to the user.
    private static final String USER_EXTERNAL_ID = "user02@mail.com";
    private static final String USER_ACCESS_TOKEN = "token";
    //Directory where enpoint store its keys
    private static final String WORKING_DIR_PREFIX = "kaa_endpoint_";

    private KaaClientProperties endpointMainProperties;
    private KaaClient kaaClient;

    private static volatile CountDownLatch attachLatch;
    private static Integer messageCounter = 0;


    public static void main(String[] args) throws Throwable {
        new VerifiersDemoMobileSim().getThingsDone();
    }

    public void getThingsDone() throws Throwable {
        endpointMainProperties = new KaaClientProperties();
        endpointMainProperties.setWorkingDirectory(WORKING_DIR_PREFIX + "A");
        kaaClient = Kaa.newClient(new DesktopKaaPlatformContext(endpointMainProperties), new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {
                LOG.info("Endpoint A started");
            }

            @Override
            public void onStopped() {
                LOG.info("Endpoint A stopped");
            }
        }, true);

        kaaClient.start();

        TimeUnit.SECONDS.sleep(2);

        // Attach the endpoint running the Kaa client to the user by verifying
        // credentials sent by the endpoint against the user credentials
        // stored on the Kaa server.
        // This demo application uses a trustful verifier, therefore
        // any credentials sent by the endpoint are accepted as valid.
        LOG.info("Attaching user...");

        CountDownLatch attachLatch1  = new CountDownLatch(1);
        kaaClient.attachUser("75083888349433448407", USER_EXTERNAL_ID, USER_ACCESS_TOKEN, new UserAttachCallback() {
            @Override
            public void onAttachResult(UserAttachResponse response) {
                LOG.info("User attach response: " + response.getResult());

                // Call attachEndpoint if the endpoint was successfully attached.
                if (response.getResult() == SyncResponseResultType.SUCCESS) {
                    attachEndpoint();
                }

                // Shut down all the Kaa client tasks and release
                // all network connections and application resources
                // if the endpoint was not attached.
                else {
                    kaaClient.stop();
                    LOG.info("Endpoint demo stopped");
                }
                attachLatch1.countDown();
            }
        });

        //waiting user attachment and attach of Endpoint B
        attachLatch1.await();

        // Shut down all the Kaa client tasks and release
        // all network connections and application resources.
        kaaClient.stop();

        LOG.info("Endpoint demo stopped");
    }

    public void attachEndpoint() {
        try {

            String answer = "Y";
            while (answer.equals("Y")) {

                String token = readUserInput("To attach endpoint to current user, please enter Endpoint access token:");
                EndpointAccessToken accessToken = new EndpointAccessToken(token.trim());

                LOG.info("Attaching endpoint with access token = '{}' to current owner user... ", token);
                kaaClient.attachEndpoint(accessToken, new OnAttachEndpointOperationCallback() {
                    @Override
                    public void onAttach(SyncResponseResultType result, EndpointKeyHash resultContext) {
                        LOG.info("Endpoint attach result: {}, attached endpoint ID: ", result);
                        LOG.info("Attached endpoint ID: ", resultContext.getKeyHash());

                        LOG.info("\n\nSending test message...");
                        sendTestMessage();
                    }
                });

                //wait for attachment
                //attachLatch.await(3, TimeUnit.SECONDS);
                TimeUnit.SECONDS.sleep(2);

                sendTestMessage();

                answer = readUserInput("Attach another endpoint? (Y/N):").trim().toUpperCase();
            }

        } catch (Exception e) {
            LOG.error("Error occurred during endpoint attachment!", e);
        }
    }

    private void sendTestMessage() {
        //Obtain the event family factory, then event family
        final EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
        final VerifiersDemoEventClassFamily eventFamily = eventFamilyFactory.getVerifiersDemoEventClassFamily();

        // Broadcast the ChangeDegreeRequest event.
        eventFamily.sendEventToAll(new MessageEvent("Message #" + (messageCounter++) + ": Hello from Mobile App !!!"));
        LOG.info("Broadcast MessageEvent sent");
    }


    private static String readUserInput(String message) {
        LOG.info(message);
        Scanner scanner = new Scanner(System.in);
        return scanner.next();
    }
}
