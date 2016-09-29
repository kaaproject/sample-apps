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

package org.kaaproject.kaa.demo.notification.storage;

import org.kaaproject.kaa.demo.notification.entity.TopicPojo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TopicStorage {

    private Set<TopicPojo> topics = new LinkedHashSet<>();

    private static TopicStorage instance;

    public static synchronized TopicStorage get() {
        if (instance == null) {
            instance = new TopicStorage();
        }
        return instance;
    }

    public TopicStorage subscribe(long topicId) {
        for (TopicPojo t : topics) {
            if (t.getTopicId() == topicId) {
                t.setSelected(true);
            }
        }
        return this;
    }

    public TopicStorage unsubsccribe(long topicId) {
        for (TopicPojo t : topics) {
            if (t.getTopicId() == topicId) {
                t.setSelected(false);
            }
        }
        return this;
    }

    public List<TopicPojo> getTopics() {
        return new ArrayList<TopicPojo>() {{
            addAll(topics);
        }};
    }

    public TopicStorage setTopics(List<TopicPojo> newTopics) {
        topics.clear();
        topics.addAll(newTopics);

        return this;
    }

}
