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

package org.kaaproject.kaa.demo.photoframe.kaa;

import org.greenrobot.eventbus.EventBus;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.demo.photoframe.AlbumInfo;
import org.kaaproject.kaa.demo.photoframe.AlbumListRequest;
import org.kaaproject.kaa.demo.photoframe.AlbumListResponse;
import org.kaaproject.kaa.demo.photoframe.DeviceInfoRequest;
import org.kaaproject.kaa.demo.photoframe.DeviceInfoResponse;
import org.kaaproject.kaa.demo.photoframe.PhotoFrameEventClassFamily;
import org.kaaproject.kaa.demo.photoframe.PlayAlbumRequest;
import org.kaaproject.kaa.demo.photoframe.PlayInfoRequest;
import org.kaaproject.kaa.demo.photoframe.PlayInfoResponse;
import org.kaaproject.kaa.demo.photoframe.PlayStatus;
import org.kaaproject.kaa.demo.photoframe.StopRequest;
import org.kaaproject.kaa.demo.photoframe.communication.Events;

import java.util.ArrayList;
import java.util.List;

/**
 * Class, that control only user event feature.
 * More you can see at @see <a href="http://docs.kaaproject.org/display/KAA/Events">Events</a>
 */
public class KaaEventsSlave implements PhotoFrameEventClassFamily.Listener {

    private KaaInfoSlave infoSlave;
    private PhotoFrameEventClassFamily mPhotoFrameEventClassFamily;
    private EventBus mEventBus;

    public KaaEventsSlave(KaaInfoSlave infoSlave) {
        this.infoSlave = infoSlave;
        this.mEventBus = EventBus.getDefault();
    }

    /**
     * Obtain a reference to the Photo frame event class family class
     * which is responsible for sending/receiving the declared family events.
     * Register a listener to receive the photo frame family events.
     */
    public void init(KaaClient client) {
        mPhotoFrameEventClassFamily = client.getEventFamilyFactory().getPhotoFrameEventClassFamily();
        mPhotoFrameEventClassFamily.addListener(this);
    }


    /**
     * Notify all the user endpoints about the device availability and play status
     * by sending them the DeviceInfoResponse and PlayInfoResponse events.
     */
    public void notifyRemoteDevices() {
        DeviceInfoResponse deviceInfoResponse = new DeviceInfoResponse();
        deviceInfoResponse.setDeviceInfo(infoSlave.getDeviceInfo());
        mPhotoFrameEventClassFamily.sendEventToAll(deviceInfoResponse);
        PlayInfoResponse playInfoResponse = new PlayInfoResponse();
        playInfoResponse.setPlayInfo(infoSlave.getPlayInfo());
        mPhotoFrameEventClassFamily.sendEventToAll(playInfoResponse);
    }

    /**
     * Update the current device status reflected in the PlayInfoResponse event,
     * send the event to all the user endpoints.
     */
    public void updateStatus(PlayStatus status, String bucketId) {
        AlbumInfo currentAlbumInfo = null;
        if (bucketId != null) {
            currentAlbumInfo = infoSlave.getAlbumsMap().get(bucketId);
        }

        infoSlave.getPlayInfo().setCurrentAlbumInfo(currentAlbumInfo);
        infoSlave.getPlayInfo().setStatus(status);

        PlayInfoResponse playInfoResponse = new PlayInfoResponse();
        playInfoResponse.setPlayInfo(infoSlave.getPlayInfo());

        mPhotoFrameEventClassFamily.sendEventToAll(playInfoResponse);
    }


    /**
     * Discover all the available remote devices (endpoints) of the user
     * by sending them the DeviceInfoRequest and PlayInfoRequest events.
     * Each operational device (endpoint) will send a reply with the DeviceInfoResponse
     * and PlayInfoResponse events to the current endpoint.
     */
    public void discoverRemoteDevices() {
        infoSlave.clearRemoteDevicesMap();

        mPhotoFrameEventClassFamily.sendEventToAll(new DeviceInfoRequest());
        mPhotoFrameEventClassFamily.sendEventToAll(new PlayInfoRequest());
    }

    /**
     * Get the information about a remote device image albums by
     * sending the AlbumListRequest event to the target endpoint using its endpointKey.
     */
    public void requestRemoteDeviceAlbums(String endpointKey) {
        AlbumListRequest albumListRequest = new AlbumListRequest();
        mPhotoFrameEventClassFamily.sendEvent(albumListRequest, endpointKey);
    }

    /**
     * Get the information about a remote device play status by
     * sending the PlayInfoRequest event to the target endpoint using its endpointKey.
     */
    public void requestRemoteDeviceStatus(String endpointKey) {
        mPhotoFrameEventClassFamily.sendEvent(new PlayInfoRequest(), endpointKey);
    }

    /**
     * Send a command to a remote device to play the image album with the specified bucketId by
     * sending the PlayAlbumRequest event to the target endpoint using its endpointKey.
     */
    public void playRemoteDeviceAlbum(String endpointKey, String bucketId) {
        PlayAlbumRequest playAlbumRequest = new PlayAlbumRequest();
        playAlbumRequest.setBucketId(bucketId);
        mPhotoFrameEventClassFamily.sendEvent(playAlbumRequest, endpointKey);
    }

    /**
     * Send a command to a remote device to stop the image album playback by
     * sending the PlayAlbumRequest event to target endpoint using its endpointKey.
     */
    public void stopPlayRemoteDeviceAlbum(String endpointKey) {
        StopRequest stopRequest = new StopRequest();
        mPhotoFrameEventClassFamily.sendEvent(stopRequest, endpointKey);
    }

    /**
     * Handle the DeviceInfoRequest event from the remote endpoint
     * identified by the endpoint key (sourceEndpoint parameter).
     * Reply with the current device info by sending the DeviceInfoResponse event.
     */
    @Override
    public void onEvent(DeviceInfoRequest deviceInfoRequest, String sourceEndpoint) {
        DeviceInfoResponse deviceInfoResponse = new DeviceInfoResponse();
        deviceInfoResponse.setDeviceInfo(infoSlave.getDeviceInfo());
        mPhotoFrameEventClassFamily.sendEvent(deviceInfoResponse, sourceEndpoint);
    }

    /**
     * Handle the DeviceInfoResponse event from the remote endpoint.
     * Store the remote device info in the local devices map.
     */
    @Override
    public void onEvent(DeviceInfoResponse deviceInfoResponse, String sourceEndpoint) {
        infoSlave.getRemoteDevicesMap().put(sourceEndpoint, deviceInfoResponse.getDeviceInfo());
        if (!infoSlave.getRemoteAlbumsMap().containsKey(sourceEndpoint)) {
            infoSlave.getRemoteAlbumsMap().put(sourceEndpoint, new ArrayList<AlbumInfo>());
        }
        mEventBus.post(new Events.DeviceInfoEvent(sourceEndpoint));
    }

    /**
     * Handle the AlbumListRequest event from a remote endpoint.
     * Reply with a list of the image albums located on the device by sending the AlbumListResponse event.
     */
    @Override
    public void onEvent(AlbumListRequest albumListRequest, String sourceEndpoint) {
        List<AlbumInfo> albums = new ArrayList<>(infoSlave.getAlbumsMap().values());
        AlbumListResponse albumListResponse = new AlbumListResponse();
        albumListResponse.setAlbumList(albums);
        mPhotoFrameEventClassFamily.sendEvent(albumListResponse, sourceEndpoint);
    }

    /**
     * Handle the AlbumListResponse event from a remote endpoint.
     * Store a remote device albums list in the local album lists map.
     */
    @Override
    public void onEvent(AlbumListResponse albumListResponse, String sourceEndpoint) {
        infoSlave.getRemoteAlbumsMap().put(sourceEndpoint, albumListResponse.getAlbumList());
        mEventBus.post(new Events.AlbumListEvent(sourceEndpoint));
    }

    /**
     * Handle the PlayAlbumRequest event from a remote endpoint.
     * Notify the application to start playback of the image album identified by bucketId.
     */
    @Override
    public void onEvent(PlayAlbumRequest playAlbumRequest, String sourceEndpoint) {
        mEventBus.post(new Events.PlayAlbumEvent(playAlbumRequest.getBucketId()));
    }

    /**
     * Handle the StopRequest event from a remote endpoint.
     * Notify the application to stop the current image album playback.
     */
    @Override
    public void onEvent(StopRequest stopRequest, String sourceEndpoint) {
        mEventBus.post(new Events.StopPlayEvent());
    }

    /**
     * Handle the PlayInfoRequest event from a remote endpoint.
     * Reply with the current device play status by sending the PlayInfoResponse event.
     */
    @Override
    public void onEvent(PlayInfoRequest playInfoRequest, String sourceEndpoint) {
        PlayInfoResponse playInfoResponse = new PlayInfoResponse();
        playInfoResponse.setPlayInfo(infoSlave.getPlayInfo());
        mPhotoFrameEventClassFamily.sendEvent(playInfoResponse, sourceEndpoint);
    }

    /**
     * Handle the PlayInfoResponse event from a remote endpoint.
     * Store a remote device play status info in the local play info map.
     */
    @Override
    public void onEvent(PlayInfoResponse playInfoResponse, String sourceEndpoint) {
        infoSlave.getRemotePlayInfoMap().put(sourceEndpoint, playInfoResponse.getPlayInfo());
        mEventBus.post(new Events.PlayInfoEvent(sourceEndpoint));
    }

}
