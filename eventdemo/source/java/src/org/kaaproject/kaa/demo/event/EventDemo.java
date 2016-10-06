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

import java.io.IOException;
import java.lang.String;
import java.util.LinkedList;
import java.util.List;

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.EventListenersResolver;
import org.kaaproject.kaa.client.event.FindEventListenersCallback;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.client.transact.TransactionId;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.demo.event.utils.EventUtil;
import org.kaaproject.kaa.schema.sample.event.thermo.ChangeDegreeRequest;
import org.kaaproject.kaa.schema.sample.event.thermo.ThermostatEventClassFamily;
import org.kaaproject.kaa.schema.sample.event.thermo.ThermostatInfoRequest;
import org.kaaproject.kaa.schema.sample.event.thermo.ThermostatInfoResponse;
import org.kaaproject.kaa.schema.sample.event.thermo.ThermostatInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A demo application that shows how to send/receive events to/from endpoints using the Kaa event API. 
 */
public class EventDemo {

    private static final Logger LOG = LoggerFactory.getLogger(EventDemo.class);

    // A Kaa client.
    private static KaaClient kaaClient;

    //Credentials for attaching an endpoint to the user.
    private static final String USER_EXTERNAL_ID = "user@email.com";
    private static final String USER_ACCESS_TOKEN = "token";

    public static void main(String[] args) {
        LOG.info("Event demo started");

        KaaChatManager chatManager = new KaaChatManager();
        chatManager.start();
        chatManager.attachToUser(USER_EXTERNAL_ID, USER_ACCESS_TOKEN);
        chatManager.printAllChats();

        // TODO: print available (hardcoded) chats

        LOG.info("Choose action by entering corresponding number:");

//        1. Join chat room
//        2. Create chat room
//        3. Delete chat room
//        4. Exit application

        while (true) {
            LOG.info("\n1. Join chat room\n" +
                    "2. Create chat room\n" +
                    "3. Delete chat room\n" +
                    "4. Exit application\n");
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
                    System.exit(0);
                    break;
                default:
                    LOG.info("You input incorrect symbol. Please, try again.");
            }
        }

        chatManager.stop();

        LOG.info("Event demo stopped");
    }





    // TODO: after all will works fine, delete this method
    public static void onUserAttached() {

        List<String> listenerFQNs = new LinkedList<>();
        listenerFQNs.add(ThermostatInfoRequest.class.getName());
        listenerFQNs.add(ChangeDegreeRequest.class.getName());

        //Obtain the event family factory.
        final EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
        //Obtain the concrete event family.
        final ThermostatEventClassFamily tecf = eventFamilyFactory.getThermostatEventClassFamily();

        // Broadcast the ChangeDegreeRequest event.
        tecf.sendEventToAll(new ChangeDegreeRequest(10));
        LOG.info("Broadcast ChangeDegreeRequest sent");

        // Add event listeners to the family factory.
        tecf.addListener(new ThermostatEventClassFamily.Listener() {

            @Override
            public void onEvent(ChangeDegreeRequest changeDegreeRequest, String senderId) {
                LOG.info("ChangeDegreeRequest event received! change temperature by {} degrees, sender: {}", changeDegreeRequest.getDegree(), senderId);
            }

            @Override
            public void onEvent(ThermostatInfoResponse thermostatInfoResponse, String senderId) {
                LOG.info("ThermostatInfoResponse event received! thermostat info: {}, sender: {}", thermostatInfoResponse.getThermostatInfo(), senderId);
            }

            @Override
            public void onEvent(ThermostatInfoRequest thermostatInfoRequest, String senderId) {
                LOG.info("ThermostatInfoRequest event received! sender: {}", senderId);
                tecf.sendEvent(new ThermostatInfoResponse(new ThermostatInfo(20, 10, true)), senderId);
            }
        });

        //Find all the listeners listening to the events from the FQNs list.
        kaaClient.findEventListeners(listenerFQNs, new FindEventListenersCallback() {

            // Perform any necessary actions with the obtained event listeners.
            @Override
            public void onEventListenersReceived(List<String> eventListeners) {
                LOG.info("{} event listeners received", eventListeners.size());
                for (String listener : eventListeners) {
                    TransactionId trxId = eventFamilyFactory.startEventsBlock();
                    // Add a targeted events to the block.
                    tecf.addEventToBlock(trxId, new ThermostatInfoRequest(), listener);
                    tecf.addEventToBlock(trxId, new ChangeDegreeRequest(-30), listener);

                    // Send the added events in a batch.
                    eventFamilyFactory.submitEventsBlock(trxId);
                    LOG.info("ThermostatInfoRequest & ChangeDegreeRequest sent to endpoint with id {}", listener);
                    // Dismiss the event batch (if the batch was not submitted as shown in the previous line).
                    // eventFamilyFactory.removeEventsBlock(trxId);
                }
            }

            // Perform any necessary actions in case of failure.
            @Override
            public void onRequestFailed() {
                LOG.info("Request failed");
            }
        });
    }
}
