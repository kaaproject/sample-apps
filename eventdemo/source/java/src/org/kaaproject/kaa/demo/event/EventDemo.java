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

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.demo.event.utils.EventUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A demo application that shows how to send/receive events to/from endpoints using the Kaa Event API.
 */
public class EventDemo {

    private static final Logger LOG = LoggerFactory.getLogger(EventDemo.class);

    // A Kaa client.
    private static KaaClient kaaClient;

    //Credentials for attaching an endpoint to the user.
    private static final String USER_EXTERNAL_ID = "user@email.com";
    private static final String USER_ACCESS_TOKEN = "token";

    public static void main(String[] args) throws IOException {
        LOG.info("Event demo started");

        KaaChatManager chatManager = new KaaChatManager();

        // start endpoint
        chatManager.start();

        // attach endpoint to user - only endpoints attached to the same user
        // can do events exchange among themselves
        chatManager.attachToUser(USER_EXTERNAL_ID, USER_ACCESS_TOKEN);

        // print chat list
        chatManager.printAllChats();


        // Print menu on screen and execute menu commands until user choose "exit" action
        while (true) {
            chatManager.printAllChats();
            LOG.info("Choose action by entering corresponding number:");
            LOG.info("\n1. Join chat room\n" +
                    "2. Create chat room\n" +
                    "3. Delete chat room\n" +
                    "4. Exit application");
            switch (EventUtil.getUserInput()) {
                case "1":
                    LOG.info("Going to \"Join chat room\".");
                    chatManager.joinChatRoom();
                    break;
                case "2":
                    LOG.info("Going to \"Create chat room\".");
                    chatManager.createChatRoom();
                    break;
                case "3":
                    LOG.info("Going to \"Delete chat room\".");
                    chatManager.deleteChatRoom();
                    break;
                case "4":
                    LOG.info("Going to \"Exit\". Have a nice day!");
                    chatManager.stop();
                    LOG.info("Event demo stopped");
                    System.exit(0);
                    break;
                default:
                    LOG.info("You input incorrect symbol. Please, try again.");
            }
        }
    }
}
