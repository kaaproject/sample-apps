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
#include <condition_variable>

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
    NotificationListener(std::shared_ptr<IKaaClient> kaaClient, bool &quitFlag,
            std::condition_variable &doQuit, std::mutex &quitMutex):
        kaaClient_(kaaClient), quitFlag_(quitFlag), doQuit_(doQuit), quitMutex_(quitMutex)
    {}

    void onNotification(const int64_t topicId, const KaaNotification &notification) override
    {
        auto topics = kaaClient_->getTopics();
        const auto &topic = std::find_if(topics.begin(), topics.end(),
                [topicId](const Topic &t) { return t.id == topicId; });
        std::cout << (boost::format("Notification for topic id '%1%' and name '%2%' received") % topicId % topic->name) << std::endl;
        std::cout << (boost::format("Alert type %1%, Alert message %2%") %
                alertTypeToString(notification.alertType) %
                (notification.alertMessage.empty() ? "Body is empty" : notification.alertMessage)) << std::endl;
        if (topic->subscriptionType == SubscriptionType::OPTIONAL_SUBSCRIPTION) {
            std::unique_lock<std::mutex> lock(quitMutex_);
            quitFlag_ = true;
            doQuit_.notify_one();
        }
    }

private:
    std::shared_ptr<IKaaClient> kaaClient_;
    bool &quitFlag_;
    std::condition_variable &doQuit_;
    std::mutex &quitMutex_;
};

class TopicListListener : public INotificationTopicListListener {
public:

    TopicListListener(std::shared_ptr<IKaaClient> kaaClient, bool &quitFlag,
            std::condition_variable &doQuit, std::mutex &quitMutex):
        kaaClient_(kaaClient), quitFlag_(quitFlag), doQuit_(doQuit), quitMutex_(quitMutex)
    {}

    void onListUpdated(const Topics& topics) override
    {
        std::cout << "Topic list was updated" << std::endl;
        showTopicList(topics);
        optionalTopicsList_ = extractOptionalTopicIds(topics);
        while (true) {
            std::cout << "Enter topic id to subscribe to" << std::endl;
            std::cout << "Enter 'quit' to exit" << std::endl;
            std::cin.clear();

            int64_t topicId;
            std::string input;
            std::cin >> input;

            if (input == "quit") {
                std::unique_lock<std::mutex> lock(quitMutex_);
                quitFlag_ = true;
                doQuit_.notify_one();
                return;
            }

            try {
                topicId = std::stoll(input);
            } catch (std::exception& e) {
                std::cout << (boost::format("Incorrect topicId: '%1%'") % input) << std::endl;
                continue;
            }

            try {
                kaaClient_->subscribeToTopic(topicId);
                kaaClient_->syncTopicSubscriptions();
                std::cout << (boost::format("Subscribed to topic '%1%'") % topicId) << std::endl;
                return;
            } catch (KaaException &e) {
                std::cout << (boost::format("Topic '%1%' is unavailable, can't subscribe") % topicId) << std::endl;
                continue;
            }
        }
    }

    std::list<int64_t> getOptionalTopicList() const
    {
        return optionalTopicsList_;
    }

private:
    std::list<int64_t> optionalTopicsList_;
    std::shared_ptr<IKaaClient> kaaClient_;
    bool &quitFlag_;
    std::condition_variable &doQuit_;
    std::mutex &quitMutex_;
};

int main()
{
    auto kaaClient = Kaa::newClient();

    bool quitFlag = false;
    std::condition_variable doQuit;
    std::mutex quitMutex;

    TopicListListener topicListListener(kaaClient, quitFlag, doQuit, quitMutex);
    kaaClient->addTopicListListener(topicListListener);

    NotificationListener notificationListener(kaaClient, quitFlag, doQuit, quitMutex);
    kaaClient->addNotificationListener(notificationListener);
    
    kaaClient->start();

    std::cout << "Notification demo started" << std::endl;

    {
        std::unique_lock<std::mutex> lock(quitMutex);
        doQuit.wait(lock, [&quitFlag] { return quitFlag; });
    }

    kaaClient->stop();

    return 0;
}

