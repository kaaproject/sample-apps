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

package org.kaaproject.kaa.demo.notification.entity;

import org.kaaproject.kaa.common.endpoint.gen.SubscriptionType;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.schema.sample.notification.SecurityAlert;

import java.util.LinkedList;
import java.util.List;

/**
 * Wrapper on Kaa entity {@link Topic}. Add new fields and filter output info.
 * Implements {@link Object} methods.
 */
public class TopicPojo {

    /**
     * Topic identifier
     */
    private long topicId;
    /**
     * Object from server, after we get it
     */
    private Topic serverTopic;
    /**
     * List aff all get obtained notification of this topic
     */
    private LinkedList<SecurityAlert> notifications;
    /**
     * Check, if user select this topic. Not interesting, if topic is mandatory
     *
     * @see <a href="http://docs.kaaproject.org/display/KAA/Notifications">Topics</a>
     */
    private boolean selected;

    public TopicPojo() {
        topicId = -1;
        notifications = new LinkedList<>();
    }

    public TopicPojo(Topic serverTopic) {
        this.serverTopic = serverTopic;
        topicId = serverTopic.getId();

        if (isMandatoryTopic()) {
            selected = true;
        }
        notifications = new LinkedList<>();
    }

    public String getTopicName() {
        /*
         * Avoid NullPointerException
         */
        if (serverTopic != null)
            return serverTopic.getName();
        else {
            return null;
        }
    }

    public void setTopicId(long topicId) {
        this.topicId = topicId;
    }

    public long getTopicId() {
        return topicId;
    }

    public boolean isSelected() {
        return selected;
    }

    /**
     * Tip: you can delete all notifications. You must get them all back.
     *
     * @param selected
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isMandatoryTopic() {
        if (serverTopic == null) {
            return false;
        }
        return serverTopic.getSubscriptionType() == SubscriptionType.MANDATORY_SUBSCRIPTION;
    }

    public int getNotificationsCount() {
        return notifications.size();
    }

    public List<SecurityAlert> getNotifications() {
        return notifications;
    }

    public void addNotification(SecurityAlert notification) {
        /*
         * Avoid equality of notifications
         */
        for (SecurityAlert n : notifications) {
            if (n.hashCode() == notification.hashCode()) {
                return;
            }
        }
        notifications.addFirst(notification);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TopicPojo)) return false;

        TopicPojo topicPojo = (TopicPojo) o;

        if (topicId != topicPojo.topicId) return false;
        if (selected != topicPojo.selected) return false;
        if (serverTopic != null ? !serverTopic.equals(topicPojo.serverTopic) : topicPojo.serverTopic != null)
            return false;
        return notifications != null ? notifications.equals(topicPojo.notifications) : topicPojo.notifications == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (topicId ^ (topicId >>> 32));
        result = 31 * result + (serverTopic != null ? serverTopic.hashCode() : 0);
        result = 31 * result + (notifications != null ? notifications.hashCode() : 0);
        result = 31 * result + (selected ? 1 : 0);
        return result;
    }
}
