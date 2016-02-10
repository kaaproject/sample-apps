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

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.sample.PowerReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulatorManager {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatorManager.class);

    private final DataGenerator dataGenerator;
    private final KaaClient kaaClient;

    private LogUploadThread currentTask;
    private AtomicInteger threadSeq = new AtomicInteger();

    public SimulatorManager(KaaClient kaaClient, int zoneCount, int panelCount) {
        this.kaaClient = kaaClient;
        this.dataGenerator = new DataGenerator(zoneCount, panelCount);
    }

    public synchronized void stop() {
        if (currentTask != null) {
            currentTask.interrupt();
        }
    }

    public synchronized void start() {
        LOG.debug("Start log uploading process.");
        stop();
        currentTask = new LogUploadThread(threadSeq.incrementAndGet());
        currentTask.start();
    }

    public class LogUploadThread extends Thread {

        private final int taskId;

        public LogUploadThread(int taskId) {
            this.taskId = taskId;
        }

        @Override
        public void run() {
            LOG.info("[{}] Log task started", taskId);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    List<PowerReport> records = dataGenerator.generateLogs();
                    long timestamp = System.currentTimeMillis();
                    for (PowerReport gr : records) {
                        gr.setTimestamp(timestamp);
                        LOG.info("[{}] Sending log record: {}", taskId, gr);
                        kaaClient.addLogRecord(gr);
                    }
                    TimeUnit.SECONDS.sleep(1L);
                } catch (InterruptedException e) {
                    LOG.info("[{}] Log task interrupted", taskId);
                    break;
                }
            }
            LOG.info("[{}] Log task stopped", taskId);
        }
    }
}
