package org.kaaproject.kaa.demo.photoframe.app;

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
