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

package org.kaaproject.kaa.examples.gpiocontol.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.registration.OnAttachEndpointOperationCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.examples.gpiocontol.adapters.DevicesAdapter;
import org.kaaproject.kaa.examples.gpiocontol.model.Device;
import org.kaaproject.kaa.examples.gpiocontol.utils.KaaManager;
import org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoResponse;
import org.kaaproject.kaa.examples.gpiocontrol.R;
import org.kaaproject.kaa.examples.gpiocontrol.RemoteControlECF;

public class DevicesListActivity extends AppCompatActivity {

    private final String TAG = DevicesListActivity.class.getSimpleName();

    TextView mNoEndpointsText;
    TextView mEndpointsTextView;

    private DevicesAdapter mAdapter;
    private KaaManager mKaaManager;

    private final RemoteControlECF.Listener mRemoteControlECFListener = new RemoteControlECF.Listener() {
        @Override
        public void onEvent(DeviceInfoResponse deviceInfoResponse, String endpointId) {
            Log.d(TAG, "Got DeviceInfoResponse");

            mAdapter.addDevice(new Device(
                    deviceInfoResponse.getModel(),
                    deviceInfoResponse.getDeviceName(),
                    deviceInfoResponse.getGpioStatus(),
                    endpointId));

            mNoEndpointsText.setVisibility(View.INVISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_list);

        mKaaManager = KaaManager.getInstance();

        mNoEndpointsText = (TextView) findViewById(R.id.no_endpoints_text);

        mAdapter = new DevicesAdapter(this, mKaaManager);
        initViews(mAdapter);

        startKaa();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mKaaManager.addEventListener(mRemoteControlECFListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mKaaManager.removeEventListener(mRemoteControlECFListener);
    }

    private void refreshActivity() {
        mAdapter.reset();
        mKaaManager.sendDeviceInfoRequestToAll();

        Toast.makeText(this, "Send device info request to all", Toast.LENGTH_SHORT).show();
    }

    private void startKaa() {
        mKaaManager.start(new Runnable() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DevicesListActivity.this, "Kaa started", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showEndpointDialog() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        alertDialogBuilder
                .setView(input)
                .setMessage(getString(R.string.endpoint_id))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        final String endpointId = input.getText().toString();
                        if (TextUtils.isEmpty(endpointId)) {
                            Toast.makeText(DevicesListActivity.this, "Endpoint ID can't be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mKaaManager.attachEndpoint(endpointId, new OnAttachEndpointOperationCallback() {
                            @Override
                            public void onAttach(SyncResponseResultType result, final EndpointKeyHash resultContext) {
                                switch (result) {
                                    case SUCCESS:
                                        mKaaManager.sendDeviceInfoRequestToAll();

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mEndpointsTextView.append("\nId: " + endpointId + ", hash: " + resultContext.getKeyHash());
                                            }
                                        });
                                        break;
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

    private void initViews(RecyclerView.Adapter adapter) {
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEndpointDialog();
            }
        });

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitle(R.string.app_name);
        toolbar.inflateMenu(R.menu.menu_device_list);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.action_update:
                        refreshActivity();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });

        mEndpointsTextView = (TextView) findViewById(R.id.endpoints_text_view);
    }
}

