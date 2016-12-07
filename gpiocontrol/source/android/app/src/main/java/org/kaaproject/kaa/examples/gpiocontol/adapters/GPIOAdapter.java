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
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.kaaproject.kaa.examples.gpiocontol.model.Device;
import org.kaaproject.kaa.examples.gpiocontol.utils.KaaManager;
import org.kaaproject.kaa.examples.gpiocontrol.GpioStatus;
import org.kaaproject.kaa.examples.gpiocontrol.GpioToggleRequest;
import org.kaaproject.kaa.examples.gpiocontrol.R;

import java.util.List;

public class GPIOAdapter extends RecyclerView.Adapter<GPIOAdapter.ViewHolder> {

    private final KaaManager mKaaManager;
    private final Context mContext;

    private List<GpioStatus> mGpioStatuses;
    private Device mDevice;

    public GPIOAdapter(Context context, KaaManager kaaManager, Device device) {
        mContext = context;
        mKaaManager = kaaManager;
        mDevice = device;
        mGpioStatuses = device.getGpioStatuses();
    }

    @Override
    public GPIOAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder((CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gpio_status, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final GpioStatus gpioStatus = mGpioStatuses.get(position);

        holder.switcher.setChecked(gpioStatus.getStatus());
        holder.gpioId.setText(String.valueOf(gpioStatus.getId()) /*+ " [" + gpioStatus.getType() + "]"*/);

        final int teaColor = ContextCompat.getColor(mContext, R.color.tea_color);
        holder.gpioId.setTextColor(holder.switcher.isChecked() ? teaColor : Color.RED);

        holder.switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Skips system recycler invoking
                if (!buttonView.isPressed()) return;

                gpioStatus.setStatus(isChecked);

                holder.gpioId.setTextColor(buttonView.isChecked() ? teaColor : Color.RED);
                mKaaManager.sendGpioToggleRequest(new GpioToggleRequest(gpioStatus),
                        mDevice.getKaaEndpointId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDevice.getGpioStatuses().size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private TextView gpioId;
        private SwitchCompat switcher;

        private ViewHolder(CardView holderView) {
            super(holderView);
            cardView = holderView;
            gpioId = (TextView) cardView.findViewById(R.id.gpio_id);
            switcher = (SwitchCompat) cardView.findViewById(R.id.switcher);
        }
    }
}
