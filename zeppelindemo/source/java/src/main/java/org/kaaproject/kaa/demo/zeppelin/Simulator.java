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

package org.kaaproject.kaa.demo.zeppelin;

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaClientPlatformContext;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.logging.strategies.RecordCountLogUploadStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Simulator {

    public static final int ZONE_COUNT = 6;
    public static final int PANEL_COUNT = 4;

    private static final Logger LOG = LoggerFactory.getLogger(Simulator.class);

    public static void main(String[] args) {
        LOG.info("Zeppelin data analytics demo started");
        LOG.info("--= Press any key to exit =--");
        int zoneCount = ZONE_COUNT;
        int panelCount = PANEL_COUNT;
        if (args.length == 2) {
            zoneCount = Integer.parseInt(args[0]);
            if (zoneCount > 6) {
                throw new RuntimeException("Too much value for zone count");
            }
            panelCount = Integer.parseInt(args[1]);
        }
        KaaClientPlatformContext context = new DesktopKaaPlatformContext();
        KaaClient kaaClient = Kaa.newClient(context, new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {
                LOG.info("Kaa client started");
            }

            @Override
            public void onStopped() {
                LOG.info("Kaa client stopped");
            }
        });

        // Set a custom strategy for uploading logs.
        // The default strategy uploads logs after either a threshold logs count
        // or a threshold logs size has been reached.
        // The following custom strategy uploads every log record as soon as it
        // is created.
        kaaClient.setLogUploadStrategy(new RecordCountLogUploadStrategy(1));
        kaaClient.start();

        SimulatorManager manager = new SimulatorManager(kaaClient, zoneCount, panelCount);
        manager.start();

        try {
            System.in.read();
        } catch (IOException e) {
            LOG.error("IOException was caught", e);
            manager.stop();
        }

        // Stop the Kaa client and release all the resources which were in use.
        kaaClient.stop();
        LOG.info("Zeppelin data analytics demo stopped");
    }
}
