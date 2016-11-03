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

import org.kaaproject.kaa.sample.PowerReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class DataGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(DataGenerator.class);
    private static final Random RANDOM = new Random();
    public static final String PANEL_NAME_PREFIX = "Panel ";
    public static final double MIN_VALUE = 75.0;
    public static final int DELTA = 20;

    private final int zoneCount;
    private final int panelCount;
    private List<String> zones = new ArrayList<>();

    public DataGenerator(int zoneCount, int panelCount) {
        this.zoneCount = zoneCount;
        this.panelCount = panelCount;
        zones.add("Ivanpah");
        zones.add("SEGS");
        zones.add("Mojave");
        zones.add("Sierra");
        zones.add("Nellis");
        zones.add("NevadaSolarOne");
    }

    public List<PowerReport> generateLogs() {
        List<PowerReport> list = Collections.emptyList();
        try {
            list = generateLogs(zoneCount, panelCount);
        } catch (Exception e) {
            LOG.error("Catch exception e", e);
        }
        return list;
    }

    public List<PowerReport> generateLogs(int zoneCount, int panelCount) {
        List<PowerReport> logs = new LinkedList<>();
        for (int j = 0; j < zoneCount; j++) {
            for (int i = 0; i < panelCount; i++) {
                PowerReport report = new PowerReport();
                report.setZoneId(zones.get(j));
                report.setPanelId(PANEL_NAME_PREFIX + i);
                report.setPower(getRandomDoubleValue());
                logs.add(report);
            }
        }
        return logs;
    }

    private double getRandomDoubleValue() {
        return MIN_VALUE + RANDOM.nextDouble() * DELTA;
    }
}
