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
public class VerifiersMobileDemo {
    private static final Logger LOG = LoggerFactory.getLogger(VerifiersMobileDemo.class);

    //Credentials for attaching an endpoint to the user.
    private static final String USER_EXTERNAL_ID = "user02@mail.com";
    private static final String USER_ACCESS_TOKEN = "token";
    //Directory where enpoint store its keys
    private static final String KEYS_DIR = "verifiers_keys_for_mobile_sim";

    private KaaClient kaaClient;

    private static CountDownLatch startLatch;
    private static Integer messageCounter = 0;
    private static UserAttachResponse userAttachResponse;



    public static void main(String[] args) throws Throwable {
        new VerifiersMobileDemo().getThingsDone();
    }

    public void getThingsDone() throws Throwable {
        KaaClientProperties endpointMainProperties = new KaaClientProperties();
        endpointMainProperties.setWorkingDirectory(KEYS_DIR);
        kaaClient = Kaa.newClient(new DesktopKaaPlatformContext(endpointMainProperties), new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {
                LOG.info("Endpoint 'Attacher' started");
                startLatch.countDown();
            }

            @Override
            public void onStopped() {
                LOG.info("Endpoint 'Attacher' stopped");
            }
        }, true);

        startLatch = new CountDownLatch(1);
        kaaClient.start();
        startLatch.await();

        // TimeUnit.SECONDS.sleep(3);

        // Attach the endpoint running the Kaa client to the user
        LOG.info("Attaching current endpoint [ID: {}] to user {} ...", kaaClient.getEndpointKeyHash(), USER_EXTERNAL_ID);
        final CountDownLatch selfAttachLatch  = new CountDownLatch(1);
        kaaClient.attachUser(USER_EXTERNAL_ID, USER_ACCESS_TOKEN, new UserAttachCallback() {
            @Override
            public void onAttachResult(UserAttachResponse response) {
                LOG.info("'Attacher' self-attach to user response: " + response.getResult());
                userAttachResponse = response;
                selfAttachLatch.countDown();
            }
        });

        //waiting user attachment and attach of Endpoint B
        selfAttachLatch.await();

        // Call attachEndpoint if the endpoint was successfully attached.
        if (userAttachResponse.getResult() == SyncResponseResultType.SUCCESS) {
            attachEndpoint();
        } else {
            LOG.error("Can't do self-attach to user '{}' (using Trustful credentials service). User access token: {}", USER_EXTERNAL_ID, USER_ACCESS_TOKEN);
        }

        // Shut down all the Kaa client tasks and release
        // all network connections and application resources.
        kaaClient.stop();

        LOG.info("Endpoint demo stopped");
    }

    public void attachEndpoint() {
        try {

            String answer = "Y";
            while (answer.equals("Y")) {

                String token = readUserInput("Enter access token of endpoint to attach (to current user):");
                EndpointAccessToken accessToken = new EndpointAccessToken(token.trim());

                LOG.info("Attaching endpoint with access token = '{}' to current owner user... ", token);
                final CountDownLatch assistedAttachLatch  = new CountDownLatch(1);
                kaaClient.attachEndpoint(accessToken, new OnAttachEndpointOperationCallback() {
                    @Override
                    public void onAttach(SyncResponseResultType result, EndpointKeyHash resultContext) {
                        LOG.info("Endpoint assisted attach result: {}", result);
                        LOG.info("Attached endpoint ID: {}", resultContext.getKeyHash());
                        assistedAttachLatch.countDown();
                    }
                });

                //wait for attachment
                assistedAttachLatch.await();

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

        // Broadcast the message to test assisted attach
        MessageEvent message = new MessageEvent("Message #" + (messageCounter++) + ": Hello from Mobile App !!!");
        LOG.info("Sending broadcast message to all enpoints...");
        LOG.info("Message text: {}", message.getMessage());
        eventFamily.sendEventToAll(message);
    }

    private String readUserInput(String message) {
        LOG.info(message);
        Scanner scanner = new Scanner(System.in);
        return scanner.next();
    }
}
