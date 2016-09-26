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

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.configuration.base.ConfigurationListener;
import org.kaaproject.kaa.client.logging.BucketInfo;
import org.kaaproject.kaa.client.logging.RecordInfo;
import org.kaaproject.kaa.client.logging.future.RecordFuture;
import org.kaaproject.kaa.client.logging.strategies.RecordCountLogUploadStrategy;
import org.kaaproject.kaa.client.logging.strategies.RecordCountWithTimeLimitLogUploadStrategy;
import org.kaaproject.kaa.schema.sample.Configuration;
import org.kaaproject.kaa.schema.sample.DataCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * A demo application that shows how to use the Kaa logging API.
 */
public class DataCollectionDemo {

    private static final int LOGS_DEFAULT_THRESHOLD = 1;
    private static final int LOGS_MAX_WAIT_TIME = 5;
    private static final int MIN_TEMPERATURE = -25;
    private static final int MAX_TEMPERATURE = 45;

    private static int samplePeriodInSeconds = 1;
    private static volatile boolean isMeasurementsRunning = false;

    private static Random rand = new Random();

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
        // This means that every log record will be sent (uploaded) as soon as it is created.
        kaaClient.setLogUploadStrategy(new RecordCountLogUploadStrategy(LOGS_DEFAULT_THRESHOLD));

        //Add a listener to display endpoint's profile/configuration data each time configuration is updated.
        kaaClient.addConfigurationListener(new ConfigurationListener() {
            @Override
            public void onConfigurationUpdate(Configuration configuration) {
                LOG.info("Endpoint configuration was updated.");
                displayConfiguration(configuration);

                Integer samplePeriod = configuration.getSamplePeriod();
                if (samplePeriod == null) {
                    LOG.warn("Sample period value in updated configuration is NULL. Update will be ignored.");
                } else {
                    if (samplePeriod > 0) {
                        samplePeriodInSeconds = samplePeriod;
                        LOG.info("Set new sample period = {} seconds.", samplePeriod);
                    } else {
                        LOG.warn("Sample period value (= {}) in updated configuration is less than 1 so ignore it.", samplePeriod);
                    }
                }
            }
        });

        // Start the Kaa client and connect it to the Kaa server.
        kaaClient.start();


        sleepForMilliseconds(2000);

        // send measures in cycle
        Thread measureThread = new Thread(new Runnable() {
            @Override
            public void run() {
                isMeasurementsRunning = true;
                LOG.info("Temperature measuring is started");
                while (isMeasurementsRunning) {
                    sendLogRecord(kaaClient);
                    sleepForMilliseconds(samplePeriodInSeconds*1000);
                }
                LOG.info("Temperature measuring is stopped");
            }
        });
        measureThread.start();

        waitForAnyInput();

        // Stop measurement thread
        isMeasurementsRunning = false;

        // Stop the Kaa client and release all the resources which were in use.
        kaaClient.stop();
        LOG.info("Data collection demo stopped");
    }

    private static void waitForAnyInput() {
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendLogRecord(KaaClient kaaClient) {

        DataCollection record = generateLog();
        RecordFuture future = kaaClient.addLogRecord(record);
        LOG.info("Log record {} submitted for sending", record.toString());

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    RecordInfo recordInfo = future.get();
                    BucketInfo bucketInfo = recordInfo.getBucketInfo();
                    LOG.info("Received log record delivery info. Bucket Id [{}]. Record delivery time [{} ms].",
                            bucketInfo.getBucketId(), recordInfo.getRecordDeliveryTimeMs());
                } catch (Exception e) {
                    LOG.error("Exception was caught while waiting for callback future", e);
                }
            }
        });

        thread.start();
    }


    private static void displayConfiguration(Configuration configuration) {
        LOG.info("Configuration = {}", configuration.toString());
    }

    private static DataCollection generateLog() {
        Integer temperature = MIN_TEMPERATURE + rand.nextInt((MAX_TEMPERATURE - MIN_TEMPERATURE) + 1);
        return new DataCollection(temperature);
    }

    private static void sleepForMilliseconds(int milliseconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
