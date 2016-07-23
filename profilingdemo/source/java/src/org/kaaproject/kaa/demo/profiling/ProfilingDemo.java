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

package org.kaaproject.kaa.demo.profiling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A demo application class that use the Kaa endpoint profiling and grouping API.
 *
 * @author Maksym Liashenko
 */
public class ProfilingDemo {

    private static final Logger LOG = LoggerFactory.getLogger(ProfilingDemo.class);

    public static void main(String[] args) throws IOException {
        LOG.info("Profiling demo started");

        KaaManager manager = new KaaManager();
        for (int i = 0; i < KaaManager.KAA_CLIENT_NUMBER; i++) {
            try {
                manager.startKaaClient(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        LOG.info("--= Press any key to exit =--");
        try {
            System.in.read();
        } catch (IOException e) {
            LOG.error("IOException was caught - ", e);
        }

        // Stop the Kaa client and release all the resources which were in use.
        try {
            manager.stopKaaClients();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOG.info("Profiling demo stopped");
    }
}