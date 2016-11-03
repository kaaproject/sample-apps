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
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.util.ArrayMap;

import org.kaaproject.kaa.demo.photoframe.AlbumInfo;
import org.kaaproject.kaa.demo.photoframe.DeviceInfo;
import org.kaaproject.kaa.demo.photoframe.PlayInfo;
import org.kaaproject.kaa.demo.photoframe.PlayStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class stores all needed information from server
 * <p>
 * Tip: for this purposes you can develop local storage
 */
final class KaaInfoSlave {

    private static final String[] ALBUM_PROJECTION = {MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

    /**
     * A local device information.
     */
    private DeviceInfo mDeviceInfo = new DeviceInfo();
    private PlayInfo mPlayInfo = new PlayInfo();
    private Map<String, AlbumInfo> mAlbumsMap = new ArrayMap<>();

    /**
     * Remote devices information
     */
    private Map<String, DeviceInfo> mRemoteDevicesMap = new ArrayMap<>();
    private Map<String, PlayInfo> mRemotePlayInfoMap = new ArrayMap<>();
    private Map<String, List<AlbumInfo>> mRemoteAlbumsMap = new ArrayMap<>();

    void initDeviceInfo(final Context context, final Runnable onAlbumListUpdated) {
        mDeviceInfo.setManufacturer(android.os.Build.MANUFACTURER);
        mDeviceInfo.setModel(android.os.Build.MODEL);
        mPlayInfo.setStatus(PlayStatus.STOPPED);

        final Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        mAlbumsMap.putAll(fetchAlbums(context, uri));

        /**
         * Register content observer, to notify about updates
         */
        context.getContentResolver().registerContentObserver(uri, true,
                new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        super.onChange(selfChange);

                        mAlbumsMap.clear();
                        mAlbumsMap.putAll(fetchAlbums(context, uri));

                        onAlbumListUpdated.run();
                    }
                }
        );
    }

    List<AlbumInfo> getRemoteDeviceAlbums(String endpointKey) {
        return mRemoteAlbumsMap.get(endpointKey);
    }

    PlayInfo getRemoteDeviceStatus(String endpointKey) {
        return mRemotePlayInfoMap.get(endpointKey);
    }

    DeviceInfo getDeviceInfo() {
        return mDeviceInfo;
    }

    PlayInfo getPlayInfo() {
        return mPlayInfo;
    }

    Map<String, AlbumInfo> getAlbumsMap() {
        return mAlbumsMap;
    }

    Map<String, DeviceInfo> getRemoteDevicesMap() {
        return mRemoteDevicesMap;
    }

    Map<String, PlayInfo> getRemotePlayInfoMap() {
        return mRemotePlayInfoMap;
    }

    Map<String, List<AlbumInfo>> getRemoteAlbumsMap() {
        return mRemoteAlbumsMap;
    }

    void clearRemoteDevicesMap() {
        mRemoteDevicesMap.clear();
    }

    private Map<String, AlbumInfo> fetchAlbums(Context context, Uri uri) {

        final Map<String, AlbumInfo> albumInfoMap = new ArrayMap<>();

        final Cursor cursor = context.getContentResolver().query(uri, ALBUM_PROJECTION, null, null, null);

        if (cursor == null) {
            return Collections.emptyMap();
        }

        try {
            final int idIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
            final int titleIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            while (cursor.moveToNext()) {
                final String id = cursor.getString(idIndex);
                if (!albumInfoMap.containsKey(id)) {
                    final AlbumInfo album = new AlbumInfo();
                    album.setBucketId(id);
                    album.setTitle(cursor.getString(titleIndex));
                    album.setImageCount(1);
                    albumInfoMap.put(id, album);
                } else {
                    final AlbumInfo album = albumInfoMap.get(id);
                    int imageCount = album.getImageCount();
                    album.setImageCount(++imageCount);
                }
            }
        } finally {
            cursor.close();
        }

        return albumInfoMap;
    }
}
