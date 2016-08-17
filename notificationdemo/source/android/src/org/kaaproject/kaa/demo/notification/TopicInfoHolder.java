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

import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.schema.example.Notification;

import java.util.*;

public class TopicInfoHolder {

    private final Map<Long, TopicModel> topicModelMap;

    public static final TopicInfoHolder holder = new TopicInfoHolder();

    private TopicInfoHolder() {
        topicModelMap = new LinkedHashMap<>();
    }

    public String getTopicName(Long topicId) {
        TopicModel model = topicModelMap.get(topicId);
        return model != null ? model.getTopicName() : "";
    }

    public List<TopicModel> getTopicModelList() {
        List<TopicModel> list = new ArrayList<>(topicModelMap.values());
        Collections.reverse(list);
        return list;
    }

    public void addNotification(Long topicId, Notification notification) {
        TopicModel model = topicModelMap.get(topicId);
        if (null != model) {
            model.addNotification(notification);
        }
    }

    public synchronized void updateTopics(List<Topic> updatedTopics) {
        Set<Long> newIds = new HashSet<>();

        for (Topic topic : updatedTopics) {
        	Long topicId = topic.getId();
            if (!topicModelMap.containsKey(topicId)) {
                topicModelMap.put(topicId, new TopicModel(topic));
            }
            newIds.add(topicId);
        }
        Iterator<Map.Entry<Long, TopicModel>> iter = topicModelMap.entrySet().iterator();
        while (iter.hasNext()) {
        	Long id = iter.next().getKey();
            if (!newIds.contains(id)) {
                iter.remove();
            }
        }
    }

}
