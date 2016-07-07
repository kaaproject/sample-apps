///**
// *  Copyright 2014-2016 CyberVision, Inc.
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *       http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// */
//
//package org.kaaproject.kaa.demo.credentials.model;
//
//import com.google.gson.Gson;
//import com.google.gson.JsonSyntaxException;
//import com.google.gson.annotations.SerializedName;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class DeviceState {
//
//    private static final Logger LOG = LoggerFactory.getLogger(DeviceState.class);
//
//    @SerializedName("active")
//    private ValueType state;
//
//    public ValueType getState() {
//        return state;
//    }
//
//    public void setState(ValueType state) {
//        this.state = state;
//    }
//
//    public class ValueType {
//        @SerializedName("boolean")
//        private boolean active;
//
//        public boolean isActive() {
//            return active;
//        }
//
//        public void setActive(boolean active) {
//            this.active = active;
//        }
//    }
//
//    public static String toJsonString(boolean isActive) {
//        DeviceState deviceStae = new DeviceState();
//        DeviceState.ValueType state = deviceState.new ValueType();
//        state.setActive(isActive);
//        deviceState.setState(state);
//
//        String jsonString = null;
//        try {
//            jsonString = new Gson().toJson(deviceState);
//        } catch (JsonSyntaxException e) {
//            LOG.error("Json parsing exception");
//        }
//
//        return jsonString;
//    }
//
//    public static boolean parseJsonString(String jsonString) {
//        if (jsonString == null) {
//            LOG.error("Invalid params. Json string is null");
//            return false;
//        }
//
//        DeviceState deviceState = null;
//        try {
//            deviceState = new Gson().fromJson(jsonString, DeviceState.class);
//        } catch (JsonSyntaxException e) {
//            LOG.error("Json parsing exception");
//        }
//
//        return deviceState != null && deviceState.getState().isActive();
//    }
//
//}
