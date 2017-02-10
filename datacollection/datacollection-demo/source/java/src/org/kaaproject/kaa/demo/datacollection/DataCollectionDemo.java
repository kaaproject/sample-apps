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

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.configuration.base.ConfigurationListener;
import org.kaaproject.kaa.client.logging.BucketInfo;
import org.kaaproject.kaa.client.logging.RecordInfo;
import org.kaaproject.kaa.client.logging.future.RecordFuture;
import org.kaaproject.kaa.client.logging.strategies.RecordCountLogUploadStrategy;
import org.kaaproject.kaa.schema.sample.Configuration;
import org.kaaproject.kaa.schema.sample.DataCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A demo application that shows how to use the Kaa logging API.
 */
public class DataCollectionDemo {

    private static final Logger LOG = LoggerFactory.getLogger(DataCollectionDemo.class);
    private static final int LOGS_DEFAULT_THRESHOLD = 1;
    private static final int MAX_SECONDS_TO_INIT_KAA = 2;
    private static final int MAX_SECONDS_BEFORE_STOP = 3;

    private static int samplePeriodInSeconds = 1;
    private static volatile AtomicInteger sentRecordsCount = new AtomicInteger(0);
    private static volatile AtomicInteger confirmationsCount = new AtomicInteger(0);

    private static Random rand = new Random();
    private static ScheduledExecutorService executor;
    private static ScheduledFuture<?> executorHandle;

    public static void main(String[] args) {
        LOG.info("--= Data collection demo started =--");

        /*
         * Create a Kaa client with the Kaa desktop context.
         */
        KaaClient kaaClient = Kaa.newClient(new DesktopKaaPlatformContext(), new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {
                LOG.info("--= Kaa client started =--");
            }

            @Override
            public void onStopped() {
                LOG.info("--= Kaa client stopped =--");
            }
        }, true);

        /*
         * Set record count strategy for uploading every log record as soon as it is created.
         */
        kaaClient.setLogUploadStrategy(new RecordCountLogUploadStrategy(LOGS_DEFAULT_THRESHOLD));

        /*
         * Displays endpoint's configuration and change sampling period each time configuration will be updated.
         */
        kaaClient.addConfigurationListener(new ConfigurationListener() {
            @Override
            public void onConfigurationUpdate(Configuration configuration) {
                LOG.info("--= Endpoint configuration was updated =--");
                displayConfiguration(configuration);

                Integer newSamplePeriod = configuration.getSamplePeriod();
                if ((newSamplePeriod != null) && (newSamplePeriod > 0)) {
                    changeMeasurementPeriod(kaaClient, newSamplePeriod);
                } else {
                    LOG.warn("Sample period value (= {}) in updated configuration is wrong, so ignore it.", newSamplePeriod);
                }
            }
        });

        /*
         * Start the Kaa client and connect it to the Kaa server.
         */
        kaaClient.start();
        sleepForSeconds(MAX_SECONDS_TO_INIT_KAA);

        /*
         * Start periodical temperature value generating and sending results to Kaa node
         */
        startMeasurement(kaaClient);

        LOG.info("*** Press Enter to stop sending log records ***");
        waitForAnyInput();

        /*
         * Stop generating and sending data to Kaa node
         */
        stopMeasurement();

        /*
         * Stop the Kaa client and release all the resources which were in use.
         */
        kaaClient.stop();
        displayResults();
        LOG.info("--= Data collection demo stopped =--");
    }

    private static void startMeasurement(KaaClient kaaClient) {
        executor = Executors.newSingleThreadScheduledExecutor();
        executorHandle =  executor.scheduleAtFixedRate(new MeasureSender(kaaClient), 0, samplePeriodInSeconds, TimeUnit.SECONDS);
        LOG.info("--= Temperature measurement is started =--");
    }

    private static class MeasureSender implements Runnable {
        KaaClient kaaClient;

        MeasureSender(KaaClient kaaClient) {
            this.kaaClient = kaaClient;
        }

        @Override
        public void run() {
            sentRecordsCount.incrementAndGet();
            DataCollection record = generateTemperatureSample();
            RecordFuture future = kaaClient.addLogRecord(record); // submit log record for sending to Kaa node
            LOG.info("Log record {} submitted for sending", record.toString());
            try {
                RecordInfo recordInfo = future.get(); // wait for log record delivery error
                BucketInfo bucketInfo = recordInfo.getBucketInfo();
                LOG.info("Received log record delivery info. Bucket Id [{}]. Record delivery time [{} ms].",
                        bucketInfo.getBucketId(), recordInfo.getRecordDeliveryTimeMs());
                confirmationsCount.incrementAndGet();
            } catch (Exception e) {
                LOG.error("Exception was caught while waiting for log's delivery report.", e);
            }
        }
    }

    private static void changeMeasurementPeriod(KaaClient kaaClient, Integer newPeriod) {
        if (executorHandle != null) {
            executorHandle.cancel(false);
        }
        samplePeriodInSeconds = newPeriod;
        executorHandle =  executor.scheduleAtFixedRate(new MeasureSender(kaaClient), 0, samplePeriodInSeconds, TimeUnit.SECONDS);
        LOG.info("Set new sample period = {} seconds.", samplePeriodInSeconds);
    }

    private static void stopMeasurement() {
        LOG.info("Stopping measurements...");
        try {
            executor.awaitTermination(MAX_SECONDS_BEFORE_STOP, TimeUnit.SECONDS);
            executor.shutdownNow();
            LOG.info("--= Temperature measurement is finished =--");
        } catch (InterruptedException e) {
            LOG.warn("Can't stop temperature measurement correctly.", e);
        }
    }

    private static DataCollection generateTemperatureSample() {
        long t = System.currentTimeMillis()/1000;
        double result = (0.6 * (Math.random() - 0.5) + Math.sin((2 * Math.PI * t)/90)) * 25 + 15;
        System.out.println(Math.round(result));
        Integer temperature = Long.valueOf(Math.round(result)).intValue();
        return new DataCollection(temperature, t);
    }

    private static void displayConfiguration(Configuration configuration) {
        LOG.info("Configuration = {}", configuration.toString());
    }

    private static void sleepForSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void waitForAnyInput() {
        try {
            System.in.read();
        } catch (IOException e) {
            LOG.warn("Error happens when waiting for user input.", e);
        }
    }

    private static void displayResults() {
        LOG.info("--= Measurement summary =--");
        LOG.info("Current sample period = {} seconds", samplePeriodInSeconds);
        LOG.info("Total temperature samples sent = {}", sentRecordsCount);
        LOG.info("Total confirmed = {}", confirmationsCount);
    }
}
