/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


#include <memory>
#include <thread>
#include <cstdint>
#include <string>
#include <chrono>
#include <cstdlib>

#include <kaa/Kaa.hpp>
#include <kaa/log/ILogStorageStatus.hpp>
#include <kaa/log/DefaultLogUploadStrategy.hpp>
#include <kaa/KaaThread.hpp>

using namespace kaa;



// The default strategy uploads logs after either a threshold logs count
// or a threshold logs size has been reached.
// The following custom strategy uploads every log record as soon as it is created.
class LogUploadStrategy : public DefaultLogUploadStrategy {
public:
    LogUploadStrategy(IKaaClientContext &context) : DefaultLogUploadStrategy(context) {}

    virtual LogUploadStrategyDecision isUploadNeeded(ILogStorageStatus& status)
    {
        if (status.getRecordsCount() >= 1) {
            return LogUploadStrategyDecision::UPLOAD;
        }
        return LogUploadStrategyDecision::NOOP;
    }
};

int getRandomInt(int max) {
    return rand() % max;
}


double getRandomDouble(int max) {
    double r = (double) rand() / RAND_MAX;
    return r * max;
}

/*
 * A demo application that shows how to use the Kaa logging API.
 */
int main()
{
    const std::size_t LOGS_TO_SEND_COUNT = 1000;
    const std::size_t ZONE_COUNT = 10;
    const std::size_t PANEL_COUNT = 10;
    const std::size_t MAX_PANEL_POWER = 100;

    std::cout << "Spark data analytics demo started" << std::endl;
    std::cout << "--= Press Enter to exit =--" << std::endl;

    //Create a Kaa client with the Kaa desktop context.
    auto kaaClient =  Kaa::newClient();

    // Set a custom strategy for uploading logs.
    kaaClient->setLogUploadStrategy(std::make_shared<LogUploadStrategy>(kaaClient->getKaaClientContext()));

    // Start the Kaa client and connect it to the Kaa server.
    kaaClient->start();


    // Send LOGS_TO_SEND_COUNT logs in a loop.
    size_t logNumber = 0;
    while (logNumber++ < LOGS_TO_SEND_COUNT) {
        kaa::KaaUserLogRecord powerReport;
        powerReport.timestamp = std::chrono::time_point_cast<std::chrono::milliseconds>(
                                       std::chrono::high_resolution_clock::now()).time_since_epoch().count();

    std::vector<kaa_log::PowerSample> samples;

        for (std::size_t zoneId = 0; zoneId < ZONE_COUNT; ++zoneId) {
        for (std::size_t panelId = 0; panelId < PANEL_COUNT; ++panelId) {
                kaa_log::PowerSample sample;
                sample.zoneId = zoneId;
                sample.panelId = panelId;
                sample.power = getRandomDouble(MAX_PANEL_POWER);

                samples.push_back(sample);
            }
    }

    powerReport.samples = std::move(samples);
    kaaClient->addLogRecord(powerReport);

    std::this_thread::sleep_for(std::chrono::seconds(1));
    }

    // Wait for the Enter key before exiting.
    std::cin.get();

    // Stop the Kaa client and release all the resources which were in use.
    kaaClient->stop();

    std::cout << "Spark data analytics demo stopped" << std::endl;

    return 0;
}
