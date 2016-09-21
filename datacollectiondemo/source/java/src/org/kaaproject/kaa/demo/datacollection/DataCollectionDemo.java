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

package org.kaaproject.kaa.demo.datacollection;

import org.kaaproject.example.datacollection.LogUploadThreshold;
import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.configuration.base.ConfigurationListener;
import org.kaaproject.kaa.client.logging.BucketInfo;
import org.kaaproject.kaa.client.logging.RecordInfo;
import org.kaaproject.kaa.client.logging.future.RecordFuture;
import org.kaaproject.kaa.client.logging.strategies.RecordCountLogUploadStrategy;
import org.kaaproject.kaa.schema.sample.logging.LogData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A demo application that shows how to use the Kaa logging API.
 */
public class DataCollectionDemo {

    private static final int LOGS_TO_SEND_COUNT = 5;
    private static final int MIN_TEMPERATURE = -25;
    private static final int MAX_TEMPERATURE = 45;

    private static final Logger LOG = LoggerFactory.getLogger(DataCollectionDemo.class);

    public static void main(String[] args) {
        LOG.info("Data collection demo started");
        //Create a Kaa client with the Kaa desktop context.
        KaaClient kaaClient = Kaa.newClient(new DesktopKaaPlatformContext(), new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {
                LOG.info("Kaa client started");
            }

            @Override
            public void onStopped() {
                LOG.info("Kaa client stopped");
            }
        }, true);

        // Set record count strategy for uploading logs with count threshold set to 1.
        // Defined strategy configuration informs to upload every log record as soon as it is created.
        // The default strategy uploads logs after either a threshold logs count
        // or a threshold logs size has been reached.
        kaaClient.setLogUploadStrategy(new RecordCountLogUploadStrategy(1));

        //Add a listener to display endpoint's profile/configuration data each time configuration is updated.
        kaaClient.addConfigurationListener(new ConfigurationListener() {
            @Override
            public void onConfigurationUpdate(LogUploadThreshold configuration) {
                LOG.info("Endpoint configuration was updated.");
                displayConfiguration(configuration);
            }
        });

        // Start the Kaa client and connect it to the Kaa server.
        kaaClient.start();

        /*
         Comment out this part in order to pass the Sandbox build process.
         */

        // Collect log record delivery futures and corresponding log record creation timestamps.
        Map<RecordFuture, Long> futuresMap = new HashMap<>();

        // Send logs in a loop
        for (LogData log : generateLogs(LOGS_TO_SEND_COUNT)) {
            futuresMap.put(kaaClient.addLogRecord(log), log.getTimeStamp());
            LOG.info("Log record {} sent", log.toString());
        }

        // Iterate over log record delivery futures and wait for delivery
        // acknowledgment for each record.
        for (RecordFuture future : futuresMap.keySet()) {
            try {
                RecordInfo recordInfo = future.get();
                BucketInfo bucketInfo = recordInfo.getBucketInfo();
                Long timeSpent = (recordInfo.getRecordAddedTimestampMs() - futuresMap.get(future))
                        + recordInfo.getRecordDeliveryTimeMs();
                LOG.info(
                        "Received log record delivery info. Bucket Id [{}]. Record delivery time [{} ms].",
                        bucketInfo.getBucketId(), timeSpent);
            } catch (Exception e) {
                LOG.error(
                        "Exception was caught while waiting for callback future",
                        e);
            }
        }

        // Stop the Kaa client and release all the resources which were in use.
        kaaClient.stop();
        LOG.info("Data collection demo stopped");
    }

    private static void displayConfiguration(LogUploadThreshold configuration) {
        LOG.info("Configuration = {}", configuration.toString());
    }

    private static List<LogData> generateLogs(int logCount) {
        Random rand = new Random();
        Integer temperature;
        List<LogData> logs = new LinkedList<>();
        for (int i = 0; i < logCount; i++) {
            temperature = MIN_TEMPERATURE + rand.nextInt((MAX_TEMPERATURE - MIN_TEMPERATURE) + 1);
            logs.add(new LogData(temperature, System.currentTimeMillis()));
        }
        return logs;
    }
}
