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

import org.kaaproject.kaa.common.endpoint.gen.SubscriptionType;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.schema.sample.notification.SecurityAlert;

import java.util.LinkedList;
import java.util.List;

public class TopicModel {

    private final Topic topic;
    private final LinkedList<SecurityAlert> securityAlerts;

    private boolean selected;
    private boolean subscribedTo;

    public TopicModel(Topic topic) {
        this.topic = topic;
        if (topic.getSubscriptionType() == SubscriptionType.MANDATORY_SUBSCRIPTION) {
            selected = true;
        }
        securityAlerts = new LinkedList<>();
    }

    public String getTopicName() {
        return topic.getName();
    }

    public Long getTopicId() {
        return topic.getId();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isMandatoryTopic() {
        return topic.getSubscriptionType() == SubscriptionType.MANDATORY_SUBSCRIPTION;
    }

    public int getNotificationsCount() {
        return securityAlerts.size();
    }

    @SuppressWarnings("serial")
    public List<SecurityAlert> getNotifications() {
        if (securityAlerts.size() > 0) {
            return securityAlerts;
        } else {
            return new LinkedList<SecurityAlert>() {{
                add(new SecurityAlert());
            }};
        }
    }

    public void addNotification(SecurityAlert notification) {
        securityAlerts.addFirst(notification);
    }

    public void setSubscribedTo(boolean subscribedTo) {
        if (!subscribedTo) {
            securityAlerts.clear();
        }
        this.subscribedTo = subscribedTo;
    }

    public boolean isSubscribedTo() {
        return subscribedTo;
    }
}
