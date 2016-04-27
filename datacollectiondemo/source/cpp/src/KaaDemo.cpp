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
#include <cstdint>
#include <string>
#include <list>

#include <kaa/Kaa.hpp>
#include <kaa/log/strategies/RecordCountLogUploadStrategy.hpp>

using namespace kaa;

/*
 * A demo application that shows how to use the Kaa logging API.
 */
int main()
{
    std::cout << "Data collection demo started" << std::endl;

    const std::size_t LOGS_TO_SEND_COUNT = 5;

    /*
     * Initialize the Kaa endpoint.
     */
    auto kaaClient =  Kaa::newClient();

    /*
     * Set a custom strategy for uploading logs.
     */
    kaaClient->setLogUploadStrategy(std::make_shared<RecordCountLogUploadStrategy>(1, kaaClient->getKaaClientContext()));

    /*
     * Run the Kaa endpoint.
     */
    kaaClient->start();

    std::list<std::pair<RecordFuture, std::size_t>> futurePairs;

    // Send LOGS_TO_SEND_COUNT logs in a loop.
    std::size_t logNumber = 0;
    while (logNumber++ < LOGS_TO_SEND_COUNT) {
        KaaUserLogRecord logRecord;
        logRecord.level = kaa_log::Level::KAA_INFO;
        logRecord.tag = "TAG";
        logRecord.message = "MESSAGE_" + std::to_string(logNumber);
        logRecord.timeStamp = TimeUtils::getCurrentTimeInMs();

        futurePairs.push_back(std::make_pair(std::move(kaaClient->addLogRecord(logRecord)), logRecord.timeStamp));
        std::cout << "Sent " << logNumber << "th record" << std::endl;
    }

    for (auto& pair : futurePairs) {
        try {
            RecordInfo recordInfo = pair.first.get();
            BucketInfo bucketInfo = recordInfo.getBucketInfo();

            std::size_t timeSpent = (recordInfo.getRecordAddedTimestampMs() - pair.second)
                                        + recordInfo.getRecordDeliveryTimeMs();

            std::cout << "Received log record delivery info. Bucket Id [" <<  bucketInfo.getBucketId() << "]. "
                      << "Record delivery time [" << timeSpent << " ms]." << std::endl;
        } catch (std::exception& e) {
            std::cout << "Exception was caught while waiting for callback future" << e.what();
        }
    }

    /*
     * Stop the Kaa endpoint.
     */
    kaaClient->stop();

    std::cout << "Data collection demo stopped" << std::endl;

    return 0;
}
