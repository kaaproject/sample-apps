/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.examples.gpiocontol.model;

import java.io.Serializable;

public class Device implements Serializable {

    private String model;
    private String deviceName;
    private Boolean[] gpioStatus;
    private String kaaEndpointId;

    public Device(String model, String deviceName, Boolean[] gpioStatus, String kaaEndpointId) {
        this.model = model;
        this.deviceName = deviceName;
        this.gpioStatus = gpioStatus;
        this.kaaEndpointId = kaaEndpointId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Boolean[] getGpioStatus() {
        return gpioStatus;
    }

    public void setGpioStatus(Boolean[] gpioStatus) {
        this.gpioStatus = gpioStatus;
    }

    public String getKaaEndpointId() {
        return kaaEndpointId;
    }

    public void setKaaEndpointId(String kaaToken) {
        this.kaaEndpointId = kaaToken;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || !(o instanceof Device))
            return false;

        Device device = (Device)o;
        return this.deviceName.equals(device.deviceName)
                && this.model.equals(device.model)
                && this.kaaEndpointId.equals(device.kaaEndpointId);
    }
}
