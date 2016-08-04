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

package org.kaaproject.kaa.demo.notification;

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.notification.NotificationListener;
import org.kaaproject.kaa.client.notification.NotificationTopicListListener;
import org.kaaproject.kaa.client.notification.UnavailableTopicException;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionType;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.schema.sample.notification.SequrityAlert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * A demo application that shows how to use the Kaa notifications API.
 *
 * @author Maksym Liashenko
 */
public class NotificationDemo {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationDemo.class);
    private static KaaClient kaaClient;
    // All available topics from server
    private static List<Topic> topics;

    public static void main(String[] args) {
        LOG.info("Notification demo started");

        LOG.info("--= Press any key to exit =--");
        kaaClient = Kaa.newClient(new DesktopKaaPlatformContext(), new SimpleKaaClientStateListener(), true);

        // A listener that listens to the notification topic list updates.
        NotificationTopicListListener topicListListener = new BasicNotificationTopicListListener();
        kaaClient.addTopicListListener(topicListListener);

        // Add a notification listener that listens to all notifications.
        kaaClient.addNotificationListener(new NotificationListener() {
            @Override
            public void onNotification(long id, SequrityAlert sampleNotification) {
                LOG.info("Notification for topic id [{}] and name [{}] received.", id, getTopic(id).getName());
                LOG.info("Notification body: {} \n", sampleNotification.getAlertMessage());
                LOG.info("Notification alert type: {} \n", sampleNotification.getAlertType());
            }
        });

        // Start the Kaa client and connect it to the Kaa server.
        kaaClient.start();

        // Get available notification topics.
        topics = kaaClient.getTopics();

        // List the obtained notification topics.
        showTopics();

        LOG.info("Type topic ID in order to subscribe to ones or type any text to exit");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLong()) {
            long topicId = scanner.nextLong();
            if (getTopic(topicId) != null) {
                LOG.info("Subscribing to optional topic {}", topicId);
                subscribeTopic(topicId);
            } else {
                LOG.info("There is no input topic id. Please, input existing topic id.");
            }
        }

        // Stop listening to the notification topic list updates.
        kaaClient.removeTopicListListener(topicListListener);
        unsubscribeOptionalTopics();

        // Stop the Kaa client and release all the resources which were in use.
        kaaClient.stop();
        LOG.info("Notification demo stopped");
    }

    private static void showTopics() {
        if (topics == null || topics.isEmpty()) {
            LOG.info("Topic list is empty");
            return;
        }

        LOG.info("Available topics:");
        for (Topic topic : topics) {
            LOG.info("Topic id: {}, name: {}, type: {}", topic.getId(), topic.getName(), topic.getSubscriptionType());
        }

        LOG.info("Subscribed on topics:");
        for (Topic t : getOneTypeTopics(SubscriptionType.MANDATORY_SUBSCRIPTION)) {
            LOG.info("Topic id: {}, name: {}, type: {}", t.getId(), t.getName(), t.getSubscriptionType().name());
        }
    }

    private static List<Topic> getOneTypeTopics(SubscriptionType type) {
        List<Topic> topics = new ArrayList<>();
        for (Topic t : NotificationDemo.topics) {
            if (t.getSubscriptionType() == type) {
                topics.add(t);
            }
        }
        return topics;
    }

    private static void subscribeTopic(long topicId) {
        try {
            kaaClient.subscribeToTopic(topicId, true);
        } catch (UnavailableTopicException e) {
            e.printStackTrace();
        }

    }

    private static Topic getTopic(long id) {
        for (Topic t : topics)
            if (t.getId() == id)
                return t;
        return null;
    }

    private static void unsubscribeOptionalTopics() {
        List<Topic> topics = getOneTypeTopics(SubscriptionType.OPTIONAL_SUBSCRIPTION);
        for (Topic t : topics) {
            try {
                kaaClient.unsubscribeFromTopic(t.getId());
            } catch (UnavailableTopicException e) {
                // if not subscribe
            }
        }
    }

    // A listener that tracks the notification topic list updates
    // and subscribes the Kaa client to every new topic available.
    private static class BasicNotificationTopicListListener implements NotificationTopicListListener {
        @Override
        public void onListUpdated(List<Topic> list) {
            LOG.info("Topic list is updating...");
            topics.clear();
            topics.addAll(list);

            showTopics();
        }
    }
}
