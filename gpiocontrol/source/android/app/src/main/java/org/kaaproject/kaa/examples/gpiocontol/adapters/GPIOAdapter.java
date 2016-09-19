/**
 * Copyright 2014-2016 CyberVision, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.examples.gpiocontol.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.examples.gpiocontol.model.Device;
import org.kaaproject.kaa.examples.gpiocontol.utils.KaaProvider;
import org.kaaproject.kaa.examples.gpiocontol.utils.NetworkUtil;
import org.kaaproject.kaa.examples.gpiocontrol.GpioStatus;
import org.kaaproject.kaa.examples.gpiocontrol.GpioToggleRequest;
import org.kaaproject.kaa.examples.gpiocontrol.R;
import org.kaaproject.kaa.examples.gpiocontrol.RemoteControlECF;

import java.util.List;

public class GPIOAdapter extends RecyclerView.Adapter<GPIOAdapter.ViewHolder> {

    private List<GpioStatus> gpioStatusList;
    private Device device;

    private Context context;

    static class ViewHolder extends RecyclerView.ViewHolder {

        private boolean state;
        private CardView cardView;
        private TextView gpioId;
        private SwitchCompat switcher;

        private ViewHolder(CardView holderView) {
            super(holderView);
            cardView = holderView;
            gpioId = (TextView) cardView.findViewById(R.id.gpioId);
            switcher = (SwitchCompat) cardView.findViewById(R.id.switcher);
            state = false;
        }
    }

    public GPIOAdapter(Context context, Device device) {
        this.device = device;
        this.context = context;
        gpioStatusList = device.getGpioStatuses();
    }

    @Override
    public GPIOAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gpio_status, parent, false);

        return new ViewHolder((CardView) view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.switcher.setChecked(gpioStatusList.get(position).getStatus());
        holder.gpioId.setText(String.valueOf(gpioStatusList.get(position).getId()));

        final int teaColor = ContextCompat.getColor(context, R.color.tea_color);
        holder.gpioId.setTextColor(holder.switcher.isChecked() ? teaColor : Color.RED);

        holder.switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!NetworkUtil.isNetworkAvailable(context)) {
                    NetworkUtil.showNetworkDialog(context);
                    buttonView.setChecked(!isChecked);
                    return;
                }

                //Skips system recycler invoking
                if (!buttonView.isPressed()) return;

                gpioStatusList.get(position).setStatus(isChecked);

                KaaClient kaaClient = KaaProvider.getClient(holder.cardView.getContext());

                EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
                final RemoteControlECF ecf = eventFamilyFactory.getRemoteControlECF();
                holder.gpioId.setTextColor(buttonView.isChecked() ? teaColor : Color.RED);
                ecf.sendEvent(new GpioToggleRequest(gpioStatusList.get(position)), device.getKaaEndpointId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return device.getGpioStatuses().size();
    }
}
