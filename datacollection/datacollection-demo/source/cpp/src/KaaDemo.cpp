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
#include <string>
#include <cstdint>

#include <boost/asio.hpp>
#include <boost/bind.hpp>

#include <kaa/Kaa.hpp>
#include <kaa/IKaaClient.hpp>
#include <kaa/configuration/manager/IConfigurationReceiver.hpp>
#include <kaa/configuration/storage/FileConfigurationStorage.hpp>
#include <kaa/log/strategies/RecordCountLogUploadStrategy.hpp>

using namespace kaa;

class TemperatureSensor : public IConfigurationReceiver {
public:
    TemperatureSensor():
        kaaClient_(Kaa::newClient()),
        samplePeriod_(0),
        interval_(samplePeriod_),
        timer_(service_, interval_)
    {
        // Set a custom strategy for uploading logs.
        // The logs will be uploaded to server
        // each time the number of buckets reaches
        // the logUploadThreshold value
        kaaClient_->setLogUploadStrategy(
            std::make_shared<RecordCountLogUploadStrategy>(int(logUploadThreshold), kaaClient_->getKaaClientContext()));

        // Set up a configuration subsystem.
        IConfigurationStoragePtr storage(
            std::make_shared<FileConfigurationStorage>(std::string(savedConfig_)));
        kaaClient_->setConfigurationStorage(storage);
        kaaClient_->addConfigurationListener(*this);
    }

    ~TemperatureSensor()
    {
        // Stop the Kaa endpoint.
        kaaClient_->stop();
        std::lock_guard<std::mutex> guard(iostream_mutex_);
        std::cout << "Data collection demo stopped" << std::endl;
    }

    void run()
    {
        // Run the Kaa endpoint.
        kaaClient_->start();
        samplePeriod_ = kaaClient_->getConfiguration().samplePeriod;
        timer_.async_wait(boost::bind(&TemperatureSensor::sendTemperature, this));
        timer_.expires_from_now(boost::posix_time::seconds(samplePeriod_));
        service_.run();
    }

private:
    static constexpr auto savedConfig_ = "saved_config.cfg";
    static constexpr int logUploadThreshold = 5;
    std::shared_ptr<IKaaClient> kaaClient_;
    int32_t samplePeriod_;
    boost::asio::io_service service_;
    boost::posix_time::seconds interval_;
    boost::asio::deadline_timer timer_;
    std::mutex iostream_mutex_;

    int32_t getTemperature()
    {
        // For sake of example random data is used
        return rand() % 10 + 25;
    }

    void sendTemperature()
    {
        KaaUserLogRecord logRecord;
        logRecord.temperature = getTemperature();
        logRecord.timeStamp = std::time(nullptr);
        // Send value of temperature
        kaaClient_->addLogRecord(logRecord);
        // Show log
        {
            std::lock_guard<std::mutex> guard(iostream_mutex_);
            std::cout << "Sampled temperature: " << logRecord.temperature
                << ", timestamp: " << logRecord.timeStamp << std::endl;
        }
        // Change timer expiry period
        timer_.expires_at(timer_.expires_at() + boost::posix_time::seconds(samplePeriod_));
        // Posts the timer event
        timer_.async_wait(boost::bind(&TemperatureSensor::sendTemperature, this));
    }

    void onConfigurationUpdated(const KaaRootConfiguration &configuration)
    {
        std::lock_guard<std::mutex> guard(iostream_mutex_);
	if (configuration.samplePeriod > 0) {
            samplePeriod_ = configuration.samplePeriod;
            std::cout << "Received configuration data. New sample period: "
                << configuration.samplePeriod << " seconds" << "\n";
        } else {
            std::cout << "Sample period value in updated configuration is wrong, so ignore it" << std::endl;
        }
    }
};

int main()
{
    try {
        TemperatureSensor sensor;
        sensor.run();
    } catch (std::exception& e) {
        std::cout << "Exception: " << e.what();
    }
    return 0;
}
