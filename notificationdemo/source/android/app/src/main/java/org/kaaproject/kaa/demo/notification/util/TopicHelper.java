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

import android.content.Context;

import org.kaaproject.kaa.demo.notification.entity.TopicPojo;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.demo.notification.storage.TopicStorage;
import org.kaaproject.kaa.schema.example.Notification;

import java.util.*;

/**
 * Library for handy work with topic and notification structure.
 */
public class TopicHelper {

    public static TopicPojo get(List<TopicPojo> topics, long topicId) {
        for (TopicPojo t : topics) {
            if (topicId == t.getTopicId())
                return t;
        }
        return null;
    }

    public static synchronized List<TopicPojo> sync(List<TopicPojo> oldTopics, List<Topic> newTopics) {
        ArrayList<TopicPojo> result = new ArrayList<>();
        for (Topic topic : newTopics) {
            long topicId = topic.getId();
            if (get(oldTopics, topicId) != null) {

                /*
                 * if we have some notification before topic list
                 */
                TopicPojo buff = new TopicPojo(topic);
                putAllNotifications(buff, get(oldTopics, topicId));
                buff.setSelected(get(oldTopics, topicId).isSelected());

                result.add(buff);
            } else {
                result.add(new TopicPojo(topic));
            }
        }

        return result;
    }

    private static void putAllNotifications(TopicPojo buff, TopicPojo topicPojo) {
        if (topicPojo == null) {
            return;
        }
        for (Notification n : topicPojo.getNotifications()) {
            buff.addNotification(n);
        }
    }

    public static String getTopicName(List<TopicPojo> topics, long topicId) {
        TopicPojo model = get(topics, topicId);
        return model != null ? model.getTopicName() : "";
    }

    public static List<TopicPojo> addNotification(List<TopicPojo> topics, long topicId, Notification notification) {
        TopicPojo model = get(topics, topicId);
        if (model != null) {
            model.addNotification(notification);
        } else {
            /*
             * can get notification without topic list
             */
            TopicPojo buff = new TopicPojo();

            buff.setTopicId(topicId);
            buff.addNotification(notification);
            topics.add(buff);
        }
        return topics;
    }

    private static List<TopicPojo> getCommonList(Context context, List<TopicPojo> topicModelMap) {
        List<TopicPojo> topics = TopicStorage.get().load(context).getTopics();

        List<TopicPojo> result = new ArrayList<>();
        result.addAll(topicModelMap);

        for (TopicPojo t : topics) {
            if (!topicModelMap.contains(t.getTopicId())) {
                result.add(t);
            }
        }
        return result;
    }

    private static List<TopicPojo> getTopicModelList(Map<Long, TopicPojo> topicModelMap) {
        List<TopicPojo> list = new ArrayList<>(topicModelMap.values());
        Collections.reverse(list);
        return list;
    }
}
