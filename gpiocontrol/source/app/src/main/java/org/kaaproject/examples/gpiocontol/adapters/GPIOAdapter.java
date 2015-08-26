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

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.kaaproject.demo.remotecontrol.R;
import org.kaaproject.kaa.examples.gpiocontol.model.Device;
import org.kaaproject.demo.remotecontrol.RemoteControlECF;
import org.kaaproject.kaa.examples.gpiocontol.utils.ConnectionsManager;
import org.kaaproject.kaa.examples.gpiocontol.utils.KaaProvider;
import org.kaaproject.kaa.examples.gpiocontol.utils.SnackbarsManager;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.examples.gpiocontrol.GPIOToggleRequest;

public class GPIOAdapter extends RecyclerView.Adapter<GPIOAdapter.ViewHolder> {
    private Boolean[] gpioDataset;
    private Device device;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public CardView cardView;
        public TextView gpioId;
        public SwitchCompat switcher;

        public ViewHolder(CardView v) {
            super(v);
            cardView = v;
            gpioId = (TextView)cardView.findViewById(R.id.gpioId);
            switcher = (SwitchCompat)cardView.findViewById(R.id.switcher);
        }
    }

    public GPIOAdapter(Device device) {
        this.device = device;
        this.gpioDataset = device.getGpioStatus();
    }

    @Override
    public GPIOAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gpio_status, parent, false);

        ViewHolder vh = new ViewHolder((CardView)v);
        return vh;
    }
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.switcher.setChecked(gpioDataset[position]);
        holder.gpioId.setText(position + "");

        final int teaColor = Color.parseColor("#009688");

        if(holder.switcher.isChecked()){
            holder.gpioId.setTextColor(teaColor);
        }else{
            holder.gpioId.setTextColor(Color.RED);
        }
        holder.switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!ConnectionsManager.haveConnection(holder.cardView.getContext())) {
                    SnackbarsManager.makeSnackBarNoInet(holder.cardView.getContext());
                    buttonView.setChecked(!isChecked);
                    return;
                }

                KaaClient kaaClient = KaaProvider.getClient(holder.cardView.getContext());

                EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
                final RemoteControlECF ecf = eventFamilyFactory.getRemoteControlECF();
                if(buttonView.isChecked()){
                    holder.gpioId.setTextColor(teaColor);
                }else{
                    holder.gpioId.setTextColor(Color.RED);
                }
                ecf.sendEvent(new GPIOToggleRequest(position, isChecked), device.getKaaEndpointId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return gpioDataset.length;
    }
}
