/**
 * Copyright 2014-2016 CyberVision, Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.demo.notification.util;

import org.kaaproject.kaa.demo.notification.entity.TopicPojo;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.schema.example.Notification;

import java.util.*;

public class TopicHelper {

    public static synchronized Map<Long, TopicPojo> initTopics(Map<Long, TopicPojo> topicModelMap, List<Topic> updatedTopics) {
//        Set<Long> newIds = new HashSet<>();

        for (Topic topic : updatedTopics) {
            Long topicId = topic.getId();
            if (!topicModelMap.containsKey(topicId)) {
                topicModelMap.put(topicId, new TopicPojo(topic));
//                newIds.add(topicId);
            } else {
                // if we have some notification before topic list
                TopicPojo buff = new TopicPojo(topic);
                if (topicModelMap.get(topicId) != null)
                    buff.addAllNotifications(topicModelMap.get(topicId).getNotifications());
                topicModelMap.put(topicId, buff);
            }
        }
//        Iterator<Map.Entry<Long, TopicPojo>> iterator = topicModelMap.entrySet().iterator();
//        while (iterator.hasNext()) {
//            long id = iterator.next().getKey();
//            if (!newIds.contains(id)) {
//                iterator.remove();
//            }
//        }

        return topicModelMap;
    }

    public static String getTopicName(Map<Long, TopicPojo> topicModelMap, long topicId) {
        TopicPojo model = topicModelMap.get(topicId);
        return model != null ? model.getTopicName() : "";
    }

    public static List<TopicPojo> getTopicModelList(Map<Long, TopicPojo> topicModelMap) {
        List<TopicPojo> list = new ArrayList<>(topicModelMap.values());
        Collections.reverse(list);
        return list;
    }

    public static void addNotification(Map<Long, TopicPojo> topicModelMap, long topicId, Notification notification) {
        TopicPojo model = topicModelMap.get(topicId);
        if (model != null) {
            model.addNotification(notification);
        } else {
            // can get notificat
            // ion without topic list
            TopicPojo buff = new TopicPojo();
            buff.addNotification(notification);
            topicModelMap.put(topicId, buff);
        }
    }
}
