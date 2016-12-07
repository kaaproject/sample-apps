/**
 * Copyright 2014-2016 CyberVision, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.appenders.telemetry.appender;


public class DataCollection {

    public enum DataCollectionField {
        TEMPERATURE("temperature"),
        TIME_STAMP("timeStamp");

        private String name;

        DataCollectionField(String value) {
            this.name = value;
        }

        public String getName() {
            return name;
        }

    }

    private Integer temperature;
    private Long timeStamp;

    public static DataCollection of(Integer temperature, Long timeStamp) {
        return new DataCollection(temperature, timeStamp);
    }

    private DataCollection(Integer temperature, Long timeStamp) {
        this.temperature = temperature;
        this.timeStamp = timeStamp;
    }

    public Integer getTemperature() {
        return temperature;
    }

    public void setTemperature(Integer temperature) {
        this.temperature = temperature;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
