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
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.registration.OnDetachEndpointOperationCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.examples.gpiocontol.GPIOStatusListActivity;
import org.kaaproject.kaa.examples.gpiocontol.model.Device;
import org.kaaproject.kaa.examples.gpiocontol.utils.KaaProvider;
import org.kaaproject.kaa.examples.gpiocontrol.R;

import java.util.List;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.ViewHolder> {

    private static final String TAG = DevicesAdapter.class.getSimpleName();
    private static final String DEVICE = "device";

    private List<Device> devicesDataset;
    private Context context;

    public DevicesAdapter(List<Device> devicesDataset, Context context) {
        this.devicesDataset = devicesDataset;
        this.context = context;
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
            deviceName = (TextView) cardView.findViewById(R.id.deviceName);
            gpioCount = (TextView) cardView.findViewById(R.id.gpioCount);
        }

    }

    @Override
    public DevicesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_view, parent, false);

        return new ViewHolder((CardView) view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Device device = devicesDataset.get(position);
        int gpioStatusesSize = devicesDataset.get(position).getGpioStatuses().size();

        holder.modelName.setText(device.getModel());
        holder.deviceName.setText(device.getDeviceName());
        holder.gpioCount.setText(context.getString(R.string.gpio_count_device_adapter, gpioStatusesSize));

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showDeleteEndpointDialog(holder.cardView, device.getKaaEndpointId(), holder.getAdapterPosition());
                return true;
            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), GPIOStatusListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(DEVICE, device);
                intent.putExtras(bundle);
                view.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return devicesDataset.size();
    }

    private void showDeleteEndpointDialog(final View view, final String endpointKey, final int position) {
//        View promptView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_delete_endpoint, null);
        View promptView = View.inflate(context, R.layout.dialog_delete_endpoint, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
        alertDialogBuilder.setView(promptView);

        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        KaaClient kaaClient = KaaProvider.getClient(view.getContext());
                        Log.d(TAG, "Going to detach....");
                        kaaClient.detachEndpoint(new EndpointKeyHash(endpointKey), new OnDetachEndpointOperationCallback() {
                            @Override
                            public void onDetach(SyncResponseResultType syncResponseResultType) {
                                Log.d(TAG, syncResponseResultType.name());
                                if (syncResponseResultType == SyncResponseResultType.SUCCESS) {
                                    devicesDataset.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, devicesDataset.size());
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
                        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}
