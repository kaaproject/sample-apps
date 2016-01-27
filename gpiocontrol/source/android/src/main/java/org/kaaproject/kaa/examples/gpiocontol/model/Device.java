/*
 * Copyright 2014-2016 CyberVision, Inc.
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

import org.kaaproject.kaa.examples.gpiocontrol.GpioStatus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Device implements Serializable {

    private String model;
    private String deviceName;

    //Because GpioStatus isn't serializable we need HashMap to persist it
    private HashMap<Integer, Boolean> gpioStatuses;
    private String kaaEndpointId;

    public Device(String model, String deviceName, List<GpioStatus> gpioStatusList, String kaaEndpointId) {
        this.model = model;
        this.deviceName = deviceName;
        setGpioStatuses(gpioStatusList);
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

    public List<GpioStatus> getGpioStatuses() {
        List<GpioStatus> gpioStatusList = new LinkedList<>();
        for(Map.Entry<Integer, Boolean> gpio : gpioStatuses.entrySet()){
            gpioStatusList.add(new GpioStatus(gpio.getKey(), gpio.getValue()));
        }
        return gpioStatusList;
    }

    public void setGpioStatuses(List<GpioStatus> gpioStatusList) {
        gpioStatuses = new LinkedHashMap<>();
        for(GpioStatus gpioStatus : gpioStatusList){
            this.gpioStatuses.put(gpioStatus.getId(), gpioStatus.getStatus());
        }
    }

    public String getKaaEndpointId() {
        return kaaEndpointId;
    }

    public void setKaaEndpointId(String kaaToken) {
        this.kaaEndpointId = kaaToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        if (model != null ? !model.equals(device.model) : device.model != null) return false;
        if (deviceName != null ? !deviceName.equals(device.deviceName) : device.deviceName != null)
            return false;
        if (gpioStatuses != null ? !gpioStatuses.equals(device.gpioStatuses) : device.gpioStatuses != null)
            return false;
        return !(kaaEndpointId != null ? !kaaEndpointId.equals(device.kaaEndpointId) : device.kaaEndpointId != null);

    }

    @Override
    public int hashCode() {
        int result = model != null ? model.hashCode() : 0;
        result = 31 * result + (deviceName != null ? deviceName.hashCode() : 0);
        result = 31 * result + (gpioStatuses != null ? gpioStatuses.hashCode() : 0);
        result = 31 * result + (kaaEndpointId != null ? kaaEndpointId.hashCode() : 0);
        return result;
    }
}

