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

package org.kaaproject.kaa.demo.event;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.admin.AuthResultDto;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.demo.event.utils.EventUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KaaChatManager {

    private static final Logger LOG = LoggerFactory.getLogger(KaaChatManager.class);

    private List<String> chatList = new ArrayList<>();
    {
        chatList.add("Living room");
        chatList.add("Guest room");
    }
    private String currentChatName;

    private static final String KEYS_DIR = "keys_for_java_event_demo";

    private KaaClient kaaClient;

    public KaaChatManager() {

    }

    public void start() {
        try {

            // Setup working directory for endpoint
            KaaClientProperties endpointProperties = new KaaClientProperties();
            endpointProperties.setWorkingDirectory(KEYS_DIR);

            // Create the Kaa desktop context for the application
            DesktopKaaPlatformContext desktopKaaPlatformContext = new DesktopKaaPlatformContext(endpointProperties);

            // Create a Kaa client and add a listener which creates a log record
            // as soon as the Kaa client is started.
            final CountDownLatch startupLatch = new CountDownLatch(1);
            kaaClient = Kaa.newClient(desktopKaaPlatformContext, new SimpleKaaClientStateListener() {
                @Override
                public void onStarted() {
                    LOG.info("--= Kaa client started =--");
                    startupLatch.countDown();
                }

                @Override
                public void onStopped() {
                    LOG.info("--= Kaa client stopped =--");
                }
            }, true);

            //Start the Kaa client and connect it to the Kaa server.
            kaaClient.start();

            startupLatch.wait();
            // EventUtil.sleepForSeconds(3);


            // TODO: add event listener using 'currentChatName'

            //Obtain the event family factory.
            final EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
            //Obtain the concrete event family.
            final Chat chat = eventFamilyFactory.getChat();

            // Add event listeners to the family factory.
            chat.addListener(new Chat.Listener() {

                @Override
                public void onEvent(Message messageEvent, String senderId) {
                    LOG.info("{}", messageEvent.toString());

                }

                @Override
                public void onEvent(ChatEvent chatEvent, String senderId) {
                    LOG.info("ChatEvent event received! Sender ID: [{}]. Event: {}", chatEvent, senderId);

                    // TODO: add handlers
                }
            });


        } catch (InterruptedException e) {
            LOG.warn("Thread interrupted when wait for attach current endpoint to user", e);
        }
    }

    public void attachToUser(String userAccessToken,String userId) {
        try {
            // Attach the endpoint to the user
            // This demo application uses a trustful verifier, therefore
            // any user credentials sent by the endpoint are accepted as valid.
            final CountDownLatch attachLatch = new CountDownLatch(1);
            kaaClient.attachUser(userId, userAccessToken, new UserAttachCallback() {
                @Override
                public void onAttachResult(UserAttachResponse response) {
                    LOG.info("Attach to user result: {}", response.getResult());
                    if (response.getResult() == SyncResponseResultType.SUCCESS) {
                        LOG.info("Current endpoint have been successfully attached to user [ID={}]!", userId);
                    } else {
                        LOG.error("Attaching current endpoint to user [ID={}] FAILED.", userId);
                        LOG.error("Attach response: {}", response);
                        LOG.error("Events exchange will be NOT POSSIBLE.");
                    }
                    attachLatch.countDown();
                }
            });

            attachLatch.wait();
            // EventUtil.sleepForSeconds(3);
        } catch (InterruptedException e) {
            LOG.warn("Thread interrupted when wait for attach current endpoint to user", e);
        }
    }

    public void printAllChats() {
        LOG.info("\n\nThe list of available chat rooms:\n\n");
        for (String chatName: chatList) {
            LOG.info("\"{}\"", chatName);
        }
        LOG.info("\n");
    }

    public void joinChatRoom() {
        LOG.info("Enter chat room name:\n");
        String chatName = EventUtil.getUserInput().trim();
        if (!chatList.contains(chatName)) {
            LOG.info("Chat \"{}\" NOT FOUND. Return to main menu.", chatName);
        } else {
            LOG.info("You have joined \"{}\" chat.", chatName);
            LOG.info("Type message and press \"Enter\" to send it or type \"quit\" to live this chat.");

            currentChatName = chatName;

            String message = "";
            while (!message.equals("quit")) {
                message = EventUtil.getUserInput();
                // TODO: send 'message' event
            }
            currentChatName = null;
        }
    }

    public void createChatRoom() {
        LOG.info("Enter new chat name:\n");
        String chatName = EventUtil.getUserInput().trim();

        // TODO: send 'create' event
    }

    public void deleteChatRoom() {
        LOG.info("Enter chat name to delete:\n");
        String chatName = EventUtil.getUserInput().trim();

        // TODO: send 'delete' event
    }

    public void stop() {
        kaaClient.stop();
    }

}
