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

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import org.kaaproject.kaa.demo.photoframe.AlbumInfo;
import org.kaaproject.kaa.demo.photoframe.DeviceInfo;
import org.kaaproject.kaa.demo.photoframe.PlayInfo;
import org.kaaproject.kaa.demo.photoframe.PlayStatus;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class stores all needed information from server
 *
 * Tip: for this purposes you can develop local storage
 */
public class KaaInfoSlave {

    /**
     * A local device information.
     */
    private DeviceInfo mDeviceInfo = new DeviceInfo();
    private PlayInfo mPlayInfo = new PlayInfo();
    private Map<String, AlbumInfo> mAlbumsMap = new HashMap<>();

    /**
     * Remote devices information
     */
    private Map<String, DeviceInfo> mRemoteDevicesMap = new LinkedHashMap<>();
    private Map<String, PlayInfo> mRemotePlayInfoMap = new HashMap<>();
    private Map<String, List<AlbumInfo>> mRemoteAlbumsMap = new HashMap<>();


    public void initDeviceInfo(Context context) {
        mDeviceInfo.setManufacturer(android.os.Build.MANUFACTURER);
        mDeviceInfo.setModel(android.os.Build.MODEL);
        mPlayInfo.setStatus(PlayStatus.STOPPED);

        fetchAlbums(context);
    }

    public List<AlbumInfo> getRemoteDeviceAlbums(String endpointKey) {
        return mRemoteAlbumsMap.get(endpointKey);
    }

    public PlayInfo getRemoteDeviceStatus(String endpointKey) {
        return mRemotePlayInfoMap.get(endpointKey);
    }

    public DeviceInfo getDeviceInfo() {
        return mDeviceInfo;
    }

    public PlayInfo getPlayInfo() {
        return mPlayInfo;
    }

    public Map<String, AlbumInfo> getAlbumsMap() {
        return mAlbumsMap;
    }

    public Map<String, DeviceInfo> getRemoteDevicesMap() {
        return mRemoteDevicesMap;
    }

    public Map<String, PlayInfo> getRemotePlayInfoMap() {
        return mRemotePlayInfoMap;
    }

    public Map<String, List<AlbumInfo>> getRemoteAlbumsMap() {
        return mRemoteAlbumsMap;
    }

    public void clearRemoteDevicesMap() {
        mRemoteDevicesMap.clear();
    }

    private void fetchAlbums(Context context) {
        mAlbumsMap.clear();

        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            try {
                int idIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                int titleIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                while (cursor.moveToNext()) {
                    String id = cursor.getString(idIndex);
                    if (!mAlbumsMap.containsKey(id)) {
                        AlbumInfo album = new AlbumInfo();
                        album.setBucketId(id);
                        album.setTitle(cursor.getString(titleIndex));
                        album.setImageCount(1);
                        mAlbumsMap.put(id, album);
                    } else {
                        AlbumInfo album = mAlbumsMap.get(id);
                        int imageCount = album.getImageCount();
                        imageCount++;
                        album.setImageCount(imageCount);
                    }
                }
            } finally {
                cursor.close();
            }
        }
    }

}
