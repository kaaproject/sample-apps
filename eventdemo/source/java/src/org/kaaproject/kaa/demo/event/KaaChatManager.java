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

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.demo.event.utils.EventUtil;
import org.kaaproject.kaa.examples.event.Chat;
import org.kaaproject.kaa.examples.event.ChatEvent;
import org.kaaproject.kaa.examples.event.ChatEventType;
import org.kaaproject.kaa.examples.event.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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
    private Chat chatEventFamily;

    public KaaChatManager() {
    }

    public void start() throws IOException {
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

            startupLatch.await();
            // EventUtil.sleepForSeconds(3);

            //Obtain the event family factory.
            final EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
            //Obtain the concrete event family.
            chatEventFamily = eventFamilyFactory.getChat();

            // Add event listeners to the family factory.
            chatEventFamily.addListener(new Chat.Listener() {

                @Override
                public void onEvent(Message messageEvent, String senderId) {
                    if ((currentChatName != null) && (currentChatName.equals(messageEvent.getChatName()))) {
                        System.out.println(messageEvent.getMessage());
                    }
                }

                @Override
                public void onEvent(ChatEvent chatEvent, String senderId) {

                    String chatName = chatEvent.getChatName().trim();
                    if (chatEvent.getEventType() == ChatEventType.CREATE) {
                        if (createChatLocal(chatName) == true) {
                            LOG.info("The list of chat rooms has been updated.");
                            LOG.info("New chat \"{}\" was CREATED.", chatName);
                        }
                    }

                    if (chatEvent.getEventType() == ChatEventType.DELETE) {
                        if (deleteChatLocal(chatName) == true) {
                            LOG.info("The list of chat rooms has been updated.");
                            LOG.info("Chat \"{}\" was DELETED.", chatName);
                        }
                    }
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

            attachLatch.await();
            // EventUtil.sleepForSeconds(3);
        } catch (InterruptedException e) {
            LOG.warn("Thread interrupted when wait for attach current endpoint to user", e);
        }
    }

    public void printAllChats() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n\nThe list of available chat rooms:\n\n");
        for (String chatName: chatList) {
            builder.append("\"").append(chatName).append("\"\n");
        }
        LOG.info(builder.toString());
    }

    public void joinChatRoom() {
        LOG.info("Enter chat room name:\n");
        String chatName = EventUtil.getUserInput().trim();
        if (!chatList.contains(chatName)) {
            LOG.info("Chat \"{}\" NOT FOUND. Return to main menu.", chatName);
        } else {
            String nickName = "";
            while (nickName.isEmpty()) {
                LOG.info("Enter your name:");
                nickName = EventUtil.getUserInput();
            }

            LOG.info("You have joined \"{}\" chat as \"{}\".", chatName, nickName);
            LOG.info("Type message and press \"Enter\" to send it or type \"quit\" to live this chat.");

            currentChatName = chatName;

            String message = "";
            while (!message.equals("quit")) {
                if (!chatList.contains(currentChatName)) {
                    LOG.info("Chat \"{}\" has been DELETED.", currentChatName);
                    break;
                } else {
                    if (!message.trim().isEmpty()) {
                        chatEventFamily.sendEventToAll(new Message(currentChatName, nickName + ": " + message));
                    }
                }
                message = EventUtil.getUserInput();
            }
            LOG.info("You are leaving chat \"{}\". Return to main menu.", currentChatName);
            chatEventFamily.sendEventToAll(new Message(currentChatName, "chat info: " + nickName + " has left the chat."));
            currentChatName = null;
        }
    }

    public void createChatRoom() {
        LOG.info("Enter new chat name:\n");
        String chatName = EventUtil.getUserInput().trim();
        LOG.info("Creating chat \"{}\" ...", chatName);

        if (createChatLocal(chatName) == true) {
            LOG.info("New chat \"{}\" was CREATED.", chatName);
            chatEventFamily.sendEventToAll(new ChatEvent(chatName, ChatEventType.CREATE));
        }
    }

    public void deleteChatRoom() {
        LOG.info("Enter chat name to delete:\n");
        String chatName = EventUtil.getUserInput().trim();
        LOG.info("Deleting chat \"{}\" ...", chatName);

        if (deleteChatLocal(chatName) == true) {
            LOG.info("Chat \"{}\" was DELETED.", chatName);
            chatEventFamily.sendEventToAll(new ChatEvent(chatName, ChatEventType.DELETE));
        }
    }

    private boolean createChatLocal(String chatName) {
        if (chatList.contains(chatName)) {
            LOG.info("Chat \"{}\" is already exists. New chat not created.", chatName);
            return false;
        } else {
            chatList.add(chatName);
            return true;
        }
    }

    private boolean deleteChatLocal(String chatName) {
        if (!chatList.contains(chatName)) {
            LOG.info("Chat \"{}\" NOT FOUND. Nothing to delete.", chatName);
            return false;
        } else {
            chatList.remove(chatName);
            return true;
        }
    }

    public void stop() {
        kaaClient.stop();
        EventUtil.sleepForSeconds(5);
    }

}
