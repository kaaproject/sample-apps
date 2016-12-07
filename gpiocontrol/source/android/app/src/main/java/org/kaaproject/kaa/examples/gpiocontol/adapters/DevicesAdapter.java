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
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.registration.OnDetachEndpointOperationCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.examples.gpiocontol.activity.GPIOStatusListActivity;
import org.kaaproject.kaa.examples.gpiocontol.model.Device;
import org.kaaproject.kaa.examples.gpiocontol.utils.KaaManager;
import org.kaaproject.kaa.examples.gpiocontrol.R;

import java.util.List;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.ViewHolder> {

    private static final String TAG = DevicesAdapter.class.getSimpleName();

    private final KaaManager mKaaManager;

    private final Context mContext;

    public DevicesAdapter(Context context, KaaManager kaaManager) {
        mKaaManager = kaaManager;
        mContext = context;
    }

    @Override
    public DevicesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_view, parent, false);

        return new ViewHolder((CardView) view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Device device = mKaaManager.getDevices().get(position);
        final int gpioStatusesSize = device.getGpioStatuses().size();

        holder.modelName.setText(device.getModel());
        holder.deviceName.setText(device.getDeviceName());
        holder.gpioCount.setText(mContext.getString(R.string.gpio_count_device_adapter, gpioStatusesSize));

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showDeleteEndpointDialog(device.getKaaEndpointId(), holder.getAdapterPosition());
                return true;
            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GPIOStatusListActivity.start(mContext, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mKaaManager.getDevices().size();
    }

    public void reset() {
        mKaaManager.getDevices().clear();
        notifyDataSetChanged();
    }

    private void showDeleteEndpointDialog(final String endpointKey, final int position) {
        new AlertDialog.Builder(mContext)
                .setMessage(mContext.getString(R.string.endpoint_delete))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d(TAG, "Going to detach....");
                        mKaaManager.detachEndpoint(new EndpointKeyHash(endpointKey), new OnDetachEndpointOperationCallback() {
                            @Override
                            public void onDetach(SyncResponseResultType syncResponseResultType) {
                                Log.d(TAG, syncResponseResultType.name());
                                if (syncResponseResultType == SyncResponseResultType.SUCCESS) {
                                    final List<Device> devices = mKaaManager.getDevices();
                                    devices.remove(position);
                                    notifyDataSetChanged();
                                }
                            }

                        });
                    }
                })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .show();
    }

    public void addDevice(Device device) {

        final List<Device> devices = mKaaManager.getDevices();

        if (devices.contains(device)) {
            final int index = devices.indexOf(device);
            devices.set(index, device);
            notifyItemChanged(index);
        } else {
            devices.add(device);
            notifyItemInserted(getItemCount() - 1);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView modelName;
        private TextView deviceName;
        private TextView gpioCount;

        private ViewHolder(CardView viewHolder) {
            super(viewHolder);
            cardView = viewHolder;
            modelName = (TextView) cardView.findViewById(R.id.model);
            deviceName = (TextView) cardView.findViewById(R.id.device_name);
            gpioCount = (TextView) cardView.findViewById(R.id.gpio_count);
        }
    }
}
