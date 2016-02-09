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

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import org.kaaproject.kaa.examples.gpiocontrol.R;
import org.kaaproject.kaa.examples.gpiocontol.model.Device;
import org.kaaproject.kaa.examples.gpiocontol.utils.SnackbarsManager;
import org.kaaproject.kaa.examples.gpiocontol.adapters.GPIOAdapter;
import org.kaaproject.kaa.examples.gpiocontol.utils.ConnectionsManager;

public class GPIOStatusListActivity extends AppCompatActivity {

    private static final String LOG_TAG = "GPIOStatusListActivity";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Device device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpio_list);

        if(!ConnectionsManager.haveConnection(this)){
            SnackbarsManager.makeSnackBarNoInet(this);
        }

        initView();

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //Get intent data from DevicesAdapter
        device = (Device)getIntent().getSerializableExtra("device");


        mAdapter = new GPIOAdapter(device);
        mRecyclerView.setAdapter(mAdapter);

    }

    protected void initView(){
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        ((TextView)findViewById(R.id.appName)).setText(getText(R.string.app_name));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
