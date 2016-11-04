/**
 * Copyright 2014-2016 CyberVision, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.demo.notification.util;

import org.apache.avro.reflect.Nullable;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.demo.notification.entity.TopicPojo;
import org.kaaproject.kaa.schema.sample.notification.SecurityAlert;

import java.util.ArrayList;
import java.util.List;


public class TopicHelper {

    public static synchronized List<TopicPojo> syncTopics(List<TopicPojo> oldTopics, List<Topic> newTopics) {
        ArrayList<TopicPojo> result = new ArrayList<>();

        for (Topic newTopic : newTopics) {
            long topicId = newTopic.getId();

            if (isTopicExist(oldTopics, topicId)) {
                TopicPojo existedTopic = getTopicWithId(oldTopics, topicId);
                result.add(existedTopic);
            } else {
                TopicPojo topicPojo = new TopicPojo();
                topicPojo.setTopicId(newTopic.getId());
                result.add(new TopicPojo(newTopic));
            }
        }

        return result;
    }

    private static boolean isTopicExist(List<TopicPojo> topics, long topicId) {
        return getTopicWithId(topics, topicId) != null;
    }

    public static List<TopicPojo> addNotificationToTopic(long topicId, List<TopicPojo> allTopics,
                                                         SecurityAlert notification) {
        TopicPojo topic = getTopicWithId(allTopics, topicId);

        if (topic != null) {
            topic.addNotification(notification);
        } else {
            TopicPojo buff = createNewTopic(topicId, notification);
            allTopics.add(buff);
        }

        return allTopics;
    }

    private static TopicPojo createNewTopic(long topicId, @Nullable SecurityAlert notification) {
        TopicPojo topic = new TopicPojo();
        topic.setTopicId(topicId);
        if (notification != null) {
            topic.addNotification(notification);
        }
        return topic;
    }

    private static TopicPojo getTopicWithId(List<TopicPojo> topics, long topicId) {
        for (TopicPojo topic : topics) {
            if (topicId == topic.getTopicId())
                return topic;
        }
        return null;
    }

    public static String getTopicName(List<TopicPojo> topics, long topicId) {
        TopicPojo topic = getTopicWithId(topics, topicId);
        return topic != null ? topic.getTopicName() : "";
    }

}
