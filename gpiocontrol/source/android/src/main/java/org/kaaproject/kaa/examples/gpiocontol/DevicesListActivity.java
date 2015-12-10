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

package org.kaaproject.kaa.examples.gpiocontol;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import com.melnykov.fab.FloatingActionButton;

import org.kaaproject.kaa.examples.gpiocontrol.R;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.event.EndpointAccessToken;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.registration.OnAttachEndpointOperationCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.examples.gpiocontol.adapters.DevicesAdapter;
import org.kaaproject.kaa.examples.gpiocontol.model.Device;
import org.kaaproject.kaa.examples.gpiocontol.utils.ConnectionsManager;
import org.kaaproject.kaa.examples.gpiocontol.utils.KaaProvider;
import org.kaaproject.kaa.examples.gpiocontol.utils.PreferencesManager;
import org.kaaproject.kaa.examples.gpiocontol.utils.SnackbarsManager;
import org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoRequest;
import org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoResponse;
import org.kaaproject.kaa.examples.gpiocontrol.RemoteControlECF;

import java.util.ArrayList;
import java.util.List;

public class DevicesListActivity extends AppCompatActivity {
    private final String LOG_TAG = "DevicesListActivity";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ProgressBar progressBar;


    private KaaClient kaaClient;
    private final Context context = this;

    private String endpointId;

    List<Device> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_list);

        //look up for Views
        initView();

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        devices = new ArrayList<>();
        mAdapter = new DevicesAdapter(devices);
        mRecyclerView.setAdapter(mAdapter);

        startKaa();
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        refreshActivity();
    }
    public void refreshActivity(){
        devices.clear();
        mAdapter.notifyDataSetChanged();
        kaaClient.getEventFamilyFactory().getRemoteControlECF().sendEventToAll(new DeviceInfoRequest());
    }

    protected void addItem(Device device){
        if(devices.contains(device)){
            int index = devices.indexOf(device);
            devices.set(index, device);
            mAdapter.notifyItemChanged(index);
        }else {
            devices.add(device);
            mAdapter.notifyItemInserted(devices.size() - 1);
        }
        findViewById(R.id.noEndpointsText).setVisibility(View.INVISIBLE);
    }

    protected void startKaa(){
        progressBar.setVisibility(View.VISIBLE);
        if(!ConnectionsManager.haveConnection(this)){
            SnackbarsManager.makeSnackBarNoInet(this);
        }else {
            kaaClient = KaaProvider.getClient(this);
            kaaClient.start();
            if(isFirstLaunch()) {
                initKaa();
            }
            setUpEndpointListener();
        }
        progressBar.setVisibility(View.INVISIBLE);
    }
    protected void initKaa(){
        PreferencesManager.setUserExternalId(this, "2");
        Log.d(LOG_TAG, "Attaching user...");
        KaaProvider.attachUser(context);

    }
    protected void setUpEndpointListener(){
        if(!ConnectionsManager.haveConnection(this)){
            SnackbarsManager.makeSnackBarNoInet(this);
            return;
        }
        if(kaaClient==null){
            startKaa();
        }





        KaaProvider.setUpEventListener(this, new RemoteControlECF.Listener() {
            @Override
            public void onEvent(DeviceInfoResponse deviceInfoResponse, String s) {
                Log.d(LOG_TAG, "Got DeviceInfoResponse");
                Device device = new Device(
                        deviceInfoResponse.getModel(),
                        deviceInfoResponse.getDeviceName(),
                        deviceInfoResponse.getGpioStatus(),
                        s);
                addItem(device);
            }
        });
    }

    private void showEndpointDialog(){
        LayoutInflater layoutInflater = LayoutInflater.from(DevicesListActivity.this);
        View promptView = layoutInflater.inflate(R.layout.dialog_endpoint_id, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DevicesListActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        endpointId = editText.getText().toString();
                        progressBar.setVisibility(View.VISIBLE);


                        if(endpointId == null || endpointId.isEmpty()) {
                            SnackbarsManager.makeSnackBar(context, "Endpoint ID can't be empty");
                            return;
                        }
                        kaaClient.attachEndpoint(new EndpointAccessToken(endpointId), new OnAttachEndpointOperationCallback() {
                            @Override
                            public void onAttach(SyncResponseResultType syncResponseResultType, EndpointKeyHash endpointKeyHash) {
                                Log.d(LOG_TAG, "attachEndpoint result: " + syncResponseResultType.toString());
                            }
                        });
                        KaaProvider.sendDeviceInfoRequestToAll(context);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                })
                .setNegativeButton("Cancel",
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

    protected void initView(){
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        ((TextView)findViewById(R.id.appName)).setText(getText(R.string.app_name));
        findViewById(R.id.reloadButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshActivity();
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToRecyclerView(mRecyclerView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEndpointDialog();
            }
        });
    }
}

