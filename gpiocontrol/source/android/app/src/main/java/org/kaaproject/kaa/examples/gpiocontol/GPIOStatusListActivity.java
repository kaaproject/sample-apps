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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import org.kaaproject.kaa.examples.gpiocontol.adapters.GPIOAdapter;
import org.kaaproject.kaa.examples.gpiocontol.model.Device;
import org.kaaproject.kaa.examples.gpiocontol.utils.NetworkUtil;
import org.kaaproject.kaa.examples.gpiocontrol.R;

public class GPIOStatusListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;

    private Device device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpio_list);

        if (!NetworkUtil.isNetworkAvailable(this)) {
            NetworkUtil.showNetworkDialog(this);
        }

        initViews();

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        device = (Device) getIntent().getSerializableExtra("device");

        RecyclerView.Adapter mAdapter = new GPIOAdapter(this, device);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initViews() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        ((TextView) findViewById(R.id.appName)).setText(getText(R.string.app_name));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
