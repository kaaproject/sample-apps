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

#include <memory>

#include <kaa/Kaa.hpp>
#include <kaa/logging/Log.hpp>
#include <kaa/logging/LoggingUtils.hpp>
#include <kaa/notification/INotificationTopicListListener.hpp>
#include <kaa/common/exception/UnavailableTopicException.hpp>

using namespace kaa;

static void showTopicList(const Topics &topics)
{
    if (topics.empty()) {
        std::cout << "Topic list is empty" << std::endl;
    } else {
        for (const auto& topic : topics) {
            std::cout << (boost::format("Topic: id '%1%', name '%2%', type '%3%'")
                % topic.id % topic.name % LoggingUtils::toString(topic.subscriptionType)) << std::endl;
        }
    }
}

static std::list<int64_t> extractOptionalTopicIds(const Topics& topics)
{
    std::list<int64_t> topicIds;
    for (const auto& topic : topics) {
        if (topic.subscriptionType == SubscriptionType::OPTIONAL_SUBSCRIPTION) {
            topicIds.push_back(topic.id);
        }
    }
    return topicIds;
}

static std::string alertTypeToString(const kaa_notification::AlertType alertType)
{
    switch(alertType) {
        case kaa_notification::CodeRed:
            return "CodeRed";
        case kaa_notification::CodeYellow:
            return "CodeYellow";
        case kaa_notification::CodeGreen:
            return "CodeGreen";
        default:
            return "Unknown type";
    }
}

class NotificationListener : public INotificationListener {
public:
    virtual void onNotification(const int64_t topicId, const KaaNotification &notification)
    {
        std::cout << (boost::format("Notification for topic id '%1%' received") % topicId) << std::endl;
        std::cout << (boost::format("Alert type %1%, Alert message %2%") %
                alertTypeToString(notification.alertType) %
                notification.alertMessage.empty() ? "Body is empty" : notification.alertMessage() ) << std::endl;
    }
};

class TopicListListener : public INotificationTopicListListener {
public:

    virtual void onListUpdated(const Topics& topics)
    {
        std::cout << ("Topic list was updated") << std::endl;
        showTopicList(topics);
        optionalTopicsList_ = extractOptionalTopicIds(topics);
    }

    std::list<int64_t> getOptionalTopicList() const
    {
        return optionalTopicsList_;
    }

private:
    std::list<int64_t> optionalTopicsList_;
};

int main()
{
    auto kaaClient = Kaa::newClient();

    kaaClient->start();

    TopicListListener topicListListener;
    kaaClient->addTopicListListener(topicListListener);

    NotificationListener notificationListener;
    kaaClient->addNotificationListener(notificationListener);

    std::cout << "Press Enter to subscribe to optional topics" << std::endl;
    std::cin.clear();
    std::cin.get();

    try {
        kaaClient->subscribeToTopics(topicListListener.getOptionalTopicList());
    } catch (UnavailableTopicException& e) {
        std::cout << (boost::format("Topic is unavailable, can't subscribe: %1%") % e.what()) << std::endl;
    }

    std::cout << "Press Enter to exit" << std::endl;

    std::cin.get();
    kaaClient->stop();

    return 0;
}

