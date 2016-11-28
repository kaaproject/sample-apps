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

package org.kaaproject.kaa.demo.photoframe.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.kaaproject.kaa.demo.photoframe.DeviceInfo;
import org.kaaproject.kaa.demo.photoframe.PlayInfo;
import org.kaaproject.kaa.demo.photoframe.PlayStatus;
import org.kaaproject.kaa.demo.photoframe.R;
import org.kaaproject.kaa.demo.photoframe.kaa.KaaManager;

import java.util.List;

/**
 * The implementation of the {@link BaseAdapter} class. Used as an adapter class for the devices list view.
 * Provides list item views with the information about remote devices.
 */
public class DevicesAdapter extends ArrayAdapter<DeviceInfo> {

    private KaaManager mKaaManager;

    private final LayoutInflater mLayoutInflater;

    public DevicesAdapter(Context context, KaaManager controller, List<DeviceInfo> devices) {
        super(context, R.layout.device_list_item, devices);
        mKaaManager = controller;

        mLayoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final DeviceInfo deviceInfo = getItem(position);
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.device_list_item, null);

            holder = new ViewHolder();
            holder.modelNameView = (TextView) convertView.findViewById(R.id.model_name);
            holder.manufacturerNameView =
                    (TextView) convertView.findViewById(R.id.manufacturer_name);
            holder.playStatusView = (TextView) convertView.findViewById(R.id.play_status);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.bind(deviceInfo, mKaaManager.getRemoteDeviceStatus(
                mKaaManager.getRemoteDeviceEndpoint(position)
        ));

        return convertView;
    }

    private static class ViewHolder {
        TextView modelNameView;
        TextView manufacturerNameView;
        TextView playStatusView;

        void bind(DeviceInfo deviceInfo, @Nullable PlayInfo playInfo) {

            final Context context = modelNameView.getContext();

            modelNameView.setText(deviceInfo.getModel());

            manufacturerNameView.setText(context.getString(
                    R.string.view_holder_device_made_by_patter, deviceInfo.getManufacturer()
            ));

            if (playInfo == null) {
                playStatusView.setText(R.string.view_holder_device_status_unknown);
            } else {
                if (playInfo.getStatus() == PlayStatus.STOPPED) {
                    playStatusView.setText(R.string.view_holder_device_status_stopped);
                } else {
                    playStatusView.setText(
                            context.getString(R.string.view_holder_device_playing_album_text,
                                    playInfo.getCurrentAlbumInfo().getTitle()));
                }
            }
        }
    }
}
