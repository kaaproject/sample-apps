/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.examples.gpiocontol;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.event.EndpointAccessToken;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.registration.OnAttachEndpointOperationCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.examples.gpiocontol.adapters.DevicesAdapter;
import org.kaaproject.kaa.examples.gpiocontol.model.Device;
import org.kaaproject.kaa.examples.gpiocontol.utils.KaaProvider;
import org.kaaproject.kaa.examples.gpiocontol.utils.NetworkUtil;
import org.kaaproject.kaa.examples.gpiocontol.utils.PreferencesManager;
import org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoRequest;
import org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoResponse;
import org.kaaproject.kaa.examples.gpiocontrol.R;
import org.kaaproject.kaa.examples.gpiocontrol.RemoteControlECF;

import java.util.ArrayList;
import java.util.List;

public class DevicesListActivity extends AppCompatActivity {

    private final String TAG = DevicesListActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;

    private ProgressBar progressBar;

    private KaaClient kaaClient;

    private String endpointId;
    private List<Device> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_list);

        initViews();

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        devices = new ArrayList<>();
        mAdapter = new DevicesAdapter(devices, this);
        mRecyclerView.setAdapter(mAdapter);

        startKaa();
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        refreshActivity();
    }

    private void refreshActivity() {
        if (isFirstLaunch()) {
            devices.clear();
            mAdapter.notifyDataSetChanged();
            kaaClient.getEventFamilyFactory().getRemoteControlECF().sendEventToAll(new DeviceInfoRequest());
        }
    }

    private void addDevice(Device device) {
        if (devices.contains(device)) {
            int index = devices.indexOf(device);
            devices.set(index, device);
            mAdapter.notifyItemChanged(index);
        } else {
            devices.add(device);
            mAdapter.notifyItemInserted(devices.size() - 1);
        }
        findViewById(R.id.noEndpointsText).setVisibility(View.INVISIBLE);
    }

    private void startKaa() {
        progressBar.setVisibility(View.VISIBLE);
        if (!NetworkUtil.isNetworkAvailable(this)) {
            NetworkUtil.showNetworkDialog(this);
        }else {
            kaaClient = KaaProvider.getClient(this);
            kaaClient.start();
            if (isFirstLaunch()) {
                PreferencesManager.setUserExternalId(this, "2");
                Log.d(TAG, "Attaching user...");
                KaaProvider.attachUser(DevicesListActivity.this);
            }
            setUpEndpointListener();
        }
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void setUpEndpointListener(){
        if (!NetworkUtil.isNetworkAvailable(this)) {
            NetworkUtil.showNetworkDialog(this);
            return;
        }
        if (kaaClient == null) {
            startKaa();
        }

        KaaProvider.setUpEventListener(this, new RemoteControlECF.Listener() {
            @Override
            public void onEvent(DeviceInfoResponse deviceInfoResponse, String endpointId) {
                Log.d(TAG, "Got DeviceInfoResponse");
                Device device = new Device(
                        deviceInfoResponse.getModel(),
                        deviceInfoResponse.getDeviceName(),
                        deviceInfoResponse.getGpioStatus(),
                        endpointId);
                addDevice(device);
            }
        });
    }

    private void showEndpointDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(DevicesListActivity.this);
        View promptView = layoutInflater.inflate(R.layout.dialog_endpoint_id, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DevicesListActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (kaaClient == null) {
                            startKaa();
                        }

                        endpointId = editText.getText().toString();
                        progressBar.setVisibility(View.VISIBLE);

                        if (endpointId == null || endpointId.isEmpty()) {
                            Snackbar.make(mRecyclerView, "Endpoint ID can't be empty", Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        kaaClient.attachEndpoint(new EndpointAccessToken(endpointId), new OnAttachEndpointOperationCallback() {
                            @Override
                            public void onAttach(SyncResponseResultType syncResponseResultType, EndpointKeyHash endpointKeyHash) {
                                Log.d(TAG, "attachEndpoint result: " + syncResponseResultType.toString());
                            }
                        });
                        KaaProvider.sendDeviceInfoRequestToAll(DevicesListActivity.this);
                        progressBar.setVisibility(View.INVISIBLE);
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

    private boolean isFirstLaunch(){
        return PreferencesManager.getUserExternalId(this).isEmpty();
    }

    private void initViews() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        ((TextView) findViewById(R.id.appName)).setText(getText(R.string.app_name));
        findViewById(R.id.reloadButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshActivity();
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.attachToRecyclerView(mRecyclerView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEndpointDialog();
            }
        });
    }
}

