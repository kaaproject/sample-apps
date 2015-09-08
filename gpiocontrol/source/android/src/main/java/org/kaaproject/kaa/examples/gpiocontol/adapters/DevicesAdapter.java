/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.examples.gpiocontol.adapters;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.kaaproject.kaa.examples.gpiocontrol.R;
import org.kaaproject.kaa.examples.gpiocontol.GPIOStatusListActivity;
import org.kaaproject.kaa.examples.gpiocontol.model.Device;

import java.util.List;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.ViewHolder>{

    private List<Device> devicesDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public TextView modelName;
        public TextView deviceName;
        public TextView gpioCount;

        public ViewHolder(CardView v) {
            super(v);
            cardView = v;
            modelName = (TextView)cardView.findViewById(R.id.model);
            deviceName = (TextView)cardView.findViewById(R.id.deviceName);
            gpioCount = (TextView)cardView.findViewById(R.id.gpioCount);
        }
    }

    public DevicesAdapter(List<Device> devicesDataset) {
        this.devicesDataset = devicesDataset;
    }

    @Override
    public DevicesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_view, parent, false);

        return new ViewHolder((CardView)v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.modelName.setText(devicesDataset.get(position).getModel());
        holder.deviceName.setText(devicesDataset.get(position).getDeviceName());
        holder.gpioCount.setText(devicesDataset.get(position).getGpioStatus().length+" GPIO");

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), GPIOStatusListActivity.class);
                intent.putExtra("device", devicesDataset.get(position));
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return devicesDataset.size();
    }

}
