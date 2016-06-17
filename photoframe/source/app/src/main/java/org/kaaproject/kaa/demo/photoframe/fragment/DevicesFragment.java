/**
 * Copyright 2014-2016 CyberVision, Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.demo.photoframe.fragment;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.kaaproject.kaa.demo.photoframe.DeviceInfo;
import org.kaaproject.kaa.demo.photoframe.R;
import org.kaaproject.kaa.demo.photoframe.adapter.DevicesAdapter;
import org.kaaproject.kaa.demo.photoframe.communication.Events;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the {@link BaseFragment} class.
 * Represents a view with a list of remote devices.
 */
public class DevicesFragment extends BaseFragment {

    private TextView mNoData;
    private ListView mDevices;
    private DevicesAdapter adapter;
    private SwipeRefreshLayout mSwipeRefresh;

    private List<DeviceInfo> devices = new ArrayList<>();

    public DevicesFragment() {
        super();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_devices, container, false);
        setupBaseViews(rootView);

        mNoData = (TextView) rootView.findViewById(R.id.devices_no_data_text);
        mDevices = (ListView) rootView.findViewById(R.id.devices_list);
        mSwipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.devices_swiperefresh);

        mNoData.setText(getString(R.string.no_devices));

        adapter = new DevicesAdapter(getActivity(), manager, devices);
        mDevices.setAdapter(adapter);

        mDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String endpointKey = manager.getRemoteDeviceEndpoint(position);
                AlbumsFragment.newInstance(endpointKey).move(getActivity());
            }
        });

        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefresh.setRefreshing(true);
                updateInfo();

                mSwipeRefresh.setRefreshing(false);
            }
        });

        showContentView();

        return rootView;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateInfo();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        getActivity().getMenuInflater().inflate(R.menu.menu_photo_frame, menu);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.item_refresh:
                updateInfo();
                break;
            case R.id.item_logout:
                manager.logout();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateInfo();
    }

    @Subscribe
    public void onEvent(Events.DeviceInfoEvent deviceInfoEvent) {
        updateView();
    }

    @Subscribe
    public void onEvent(Events.PlayInfoEvent playInfoEvent) {
        updateView();
    }

    @Override
    public String getTitle() {
        return getString(R.string.devices);
    }

    @Override
    public String getFragmentTag() {
        return DevicesFragment.class.getSimpleName();
    }

    @Override
    protected boolean displayHomeAsUp() {
        return false;
    }

    private void notifyView() {
        if (adapter.getCount() > 0) {
            mNoData.setVisibility(View.GONE);
            mDevices.setVisibility(View.VISIBLE);
        } else {
            mDevices.setVisibility(View.GONE);
            mNoData.setVisibility(View.VISIBLE);
        }
    }

    private void updateInfo() {
        manager.discoverRemoteDevices();
    }

    private void updateView() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                devices.clear();
                devices.addAll(manager.getRemoteDevicesMap().values());

                adapter.notifyDataSetChanged();
                notifyView();
            }
        });

    }
}
