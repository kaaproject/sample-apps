/**
 * Copyright 2014-2016 CyberVision, Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.demo.photoframe.communication;

import org.greenrobot.eventbus.EventBus;

public class Events {


    /**
     * An event class which is used to notify UI components
     * of a received command to play the album with the specified bucketId.
     */
    public static class PlayAlbumEvent {

        private final String mBucketId;

        public PlayAlbumEvent(String bucketId) {
            mBucketId = bucketId;
        }

        public String getBucketId() {
            return mBucketId;
        }

    }

    /**
     * An event class which is used to notify UI components
     * of a received command to stop the album playback.
     */
    public static class StopPlayEvent {

    }

    /**
     * A superclass for all application events dispatched via {@link EventBus}.
     */
    public static class BasicEvent {

        private String mErrorMessage;

        public BasicEvent() {
            super();
        }

        public BasicEvent(String errorMessage) {
            mErrorMessage = errorMessage;
        }

        public String getErrorMessage() {
            return mErrorMessage;
        }
    }


    /**
     * A superclass for all events associated with the remote endpoints (devices) identified by endpointKey.
     */
    public static class BasicEndpointEvent extends BasicEvent {

        private final String mEndpointKey;

        public BasicEndpointEvent(String endpointKey) {
            super();
            mEndpointKey = endpointKey;
        }

        public BasicEndpointEvent(String endpointKey, String errorMessage) {
            super(errorMessage);
            mEndpointKey = endpointKey;
        }

        public String getEndpointKey() {
            return mEndpointKey;
        }

    }

    /**
     * An event class which is used to notify UI components that the Kaa client has started.
     */
    public static class KaaStartedEvent extends BasicEvent {

        public KaaStartedEvent() {
            super();
        }

        public KaaStartedEvent(String errorMessage) {
            super(errorMessage);
        }
    }

    /**
     * An event class which is used to notify UI components
     * of the completion of the endpoint detach operation.
     */
    public static class UserDetachEvent extends BasicEvent {

        public UserDetachEvent() {
            super();
        }

        public UserDetachEvent(String errorMessage) {
            super(errorMessage);
        }
    }


    /**
     * An event class which is used to notify UI components
     * of the completion of the endpoint attach operation.
     */
    public static class UserAttachEvent extends BasicEvent {

        public UserAttachEvent() {
            super();
        }

        public UserAttachEvent(String errorMessage) {
            super(errorMessage);
        }

    }


    /**
     * An event class which is used to notify UI components
     * of the receipt of the information about a remote device.
     */
    public static class DeviceInfoEvent extends BasicEndpointEvent {

        public DeviceInfoEvent(String endpointKey) {
            super(endpointKey);
        }

        public DeviceInfoEvent(String endpointKey, String errorMessage) {
            super(endpointKey, errorMessage);
        }

    }

    /**
     * An event class which is used to notify UI components of
     * the receipt of the information about the play status of a remote device.
     */
    public static class PlayInfoEvent extends BasicEndpointEvent {

        public PlayInfoEvent(String endpointKey) {
            super(endpointKey);
        }

        public PlayInfoEvent(String endpointKey, String errorMessage) {
            super(endpointKey, errorMessage);
        }

    }

    /**
     * An event class which is used to notify UI components of the receipt of the albums list from a remote device.
     */
    public static class AlbumListEvent extends BasicEndpointEvent {

        public AlbumListEvent(String endpointKey) {
            super(endpointKey);
        }

        public AlbumListEvent(String endpointKey, String errorMessage) {
            super(endpointKey, errorMessage);
        }

    }

}
