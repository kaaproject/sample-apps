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
import org.kaaproject.kaa.schema.example.Notification;

import java.util.LinkedList;
import java.util.List;

public class TopicPojo {

    private Topic serverTopic;
    private LinkedList<Notification> notifications;

    private boolean selected;
    private boolean subscribedTo;

    public TopicPojo() {
        notifications = new LinkedList<>();
    }

    public TopicPojo(Topic serverTopic) {
        this.serverTopic = serverTopic;

        if (isMandatoryTopic()) {
            selected = true;
        }
        notifications = new LinkedList<>();
    }

    public String getTopicName() {
        if (serverTopic != null)
            return serverTopic.getName();
        else {
            return null;
        }
    }

    public Long getTopicId() {
        return serverTopic.getId();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isMandatoryTopic() {
        if(serverTopic == null) {
            return false;
        }
        return serverTopic.getSubscriptionType() == SubscriptionType.MANDATORY_SUBSCRIPTION;
    }

    public int getNotificationsCount() {
        return notifications.size();
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void addNotification(Notification notification) {
        notifications.addFirst(notification);
    }

    public void addAllNotifications(List<Notification> notifications) {
        notifications.addAll(notifications);
    }

    public void setSubscribedTo(boolean subscribedTo) {
        if (!subscribedTo) {
            notifications.clear();
        }
        this.subscribedTo = subscribedTo;
    }

    public boolean isSubscribedTo() {
        return subscribedTo;
    }
}
