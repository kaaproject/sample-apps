package org.kaaproject.examples.storm;
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

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.logging.DefaultLogUploadStrategy;
import org.kaaproject.kaa.client.logging.LogStorageStatus;
import org.kaaproject.kaa.client.logging.LogUploadStrategyDecision;
import org.kaaproject.kaa.examples.powerplant.PowerReport;
import org.kaaproject.kaa.examples.powerplant.PowerSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by dmitry-sergeev on 13.08.15.
 */
public class StormDataAnalyticsDemo {

    private static final Logger LOG = LoggerFactory.getLogger(DataCollectionDemo.class);
    private static final int LOGS_TO_SEND_COUNT = 1000;
    private static final int ZONE_COUNT = 10;
    private static final int PANEL_COUNT = 10;
    private static final int MAX_PANEL_POWER = 100;
    private static Random random = new Random();

    static double getRandomDouble(int max) {
        return random.nextDouble() * max;
    }
    /*
     * A demo application that shows how to use the Kaa logging API.
     */
    public static void main(String[] args) throws Exception{
        LOG.info("Storm data analytics demo started");
        LOG.info("--= Press any key to exit =--");

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
        });
        // The default strategy uploads logs after either a threshold logs count
        // or a threshold logs size has been reached.
        // The following custom strategy uploads every log record as soon as it is created.
        // Set a custom strategy for uploading logs.
        kaaClient.setLogUploadStrategy(new DefaultLogUploadStrategy() {
            @Override
            public LogUploadStrategyDecision isUploadNeeded(LogStorageStatus status) {
                if (status.getRecordCount() >= 1) {
                    return LogUploadStrategyDecision.UPLOAD;
                }
                return LogUploadStrategyDecision.NOOP;
            }
        });
        // Start the Kaa client and connect it to the Kaa storm.server.
        kaaClient.start();

        // Send LOGS_TO_SEND_COUNT logs in a loop.
        int logNumber = 0;
        while(logNumber++ < LOGS_TO_SEND_COUNT){
            PowerReport powerReport = new PowerReport();
            powerReport.setTimestamp(System.currentTimeMillis());

            List<PowerSample> samples = new ArrayList<PowerSample>();
            for(int zoneId = 0; zoneId < ZONE_COUNT; ++zoneId){
                for(int panelId = 0; panelId < PANEL_COUNT; ++panelId){
                    PowerSample sample = new PowerSample();
                    sample.setZoneId(zoneId);
                    sample.setPanelId(panelId);
                    sample.setPower(getRandomDouble(MAX_PANEL_POWER));

                    samples.add(sample);
                }
            }

            powerReport.setSamples(samples);
            kaaClient.addLogRecord(powerReport);

            TimeUnit.SECONDS.sleep(1);
        }

        // Wait for the Enter key before exiting.
        System.in.read();
        // Stop the Kaa client and release all the resources which were in use.
        kaaClient.stop();

        LOG.info("Storm data analytics demo stopped");

    }
}
