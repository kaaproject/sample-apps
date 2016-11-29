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

package org.kaaproject.kaa.examples.gpiocontol.model;

import org.kaaproject.kaa.examples.gpiocontrol.GpioStatus;

import java.util.List;

public class Device {

    private String mModel;
    private String mDeviceName;

    private List<GpioStatus> mGpioStatuses;
    private String mKaaEndpointId;

    public Device(String model, String deviceName, List<GpioStatus> gpioStatusList, String kaaEndpointId) {
        mModel = model;
        mDeviceName = deviceName;
        mGpioStatuses = gpioStatusList;
        mKaaEndpointId = kaaEndpointId;
    }

    public String getModel() {
        return mModel;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public List<GpioStatus> getGpioStatuses() {
        return mGpioStatuses;
    }

    public String getKaaEndpointId() {
        return mKaaEndpointId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        return mModel != null ? mModel.equals(device.mModel) : device.mModel == null &&
                (mDeviceName != null ?
                        mDeviceName.equals(device.mDeviceName) : device.mDeviceName == null &&
                        (mKaaEndpointId != null ?
                                mKaaEndpointId.equals(device.mKaaEndpointId) : device.mKaaEndpointId == null));
    }

    @Override
    public int hashCode() {
        int result = mModel != null ? mModel.hashCode() : 0;
        result = 31 * result + (mDeviceName != null ? mDeviceName.hashCode() : 0);
        result = 31 * result + (mKaaEndpointId != null ? mKaaEndpointId.hashCode() : 0);
        return result;
    }
}

