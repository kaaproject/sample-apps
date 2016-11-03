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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class DevicesAdapter extends BaseAdapter {

    private Context mContext;
    private KaaManager manager;
    private List<DeviceInfo> devices;

    public DevicesAdapter(Context context, KaaManager controller, List<DeviceInfo> devices) {
        mContext = context;
        manager = controller;
        this.devices = devices;
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DeviceInfo deviceInfo = (DeviceInfo) getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.device_list_item, null);

            holder = new ViewHolder();
            holder.modelNameView = (TextView) convertView.findViewById(R.id.modelName);
            holder.manufacturerNameView = (TextView) convertView.findViewById(R.id.manufacturerName);
            holder.playStatusView = (TextView) convertView.findViewById(R.id.playStatus);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.modelNameView.setText(deviceInfo.getModel());

        String byManufacturer = mContext.getString(R.string.by_pattern, deviceInfo.getManufacturer());
        holder.manufacturerNameView.setText(byManufacturer);

        String endpointKey = manager.getRemoteDeviceEndpoint(position);
        PlayInfo playInfo = manager.getRemoteDeviceStatus(endpointKey);

        if (playInfo == null) {
            holder.playStatusView.setText(R.string.unknown);
            return convertView;
        }

        if (playInfo.getStatus() == PlayStatus.STOPPED) {
            holder.playStatusView.setText(R.string.stopped);
        } else {
            holder.playStatusView.setText(mContext.getString(R.string.playing,
                    playInfo.getCurrentAlbumInfo().getTitle()));
        }

        return convertView;
    }

    private class ViewHolder {
        TextView modelNameView;
        TextView manufacturerNameView;
        TextView playStatusView;
    }
}
