package org.kaaproject.kaa.demo.photoframe.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.kaaproject.kaa.demo.photoframe.DeviceInfo;
import org.kaaproject.kaa.demo.photoframe.R;
import org.kaaproject.kaa.demo.photoframe.adapter.DevicesAdapter;
import org.kaaproject.kaa.demo.photoframe.communication.Events;

import java.util.ArrayList;
import java.util.List;

public class DevicesActivity extends BaseActivity {


    private TextView mNoData;
    private ListView mDevices;
    private DevicesAdapter mAdapter;

    private List<DeviceInfo> mDeviceInfos = new ArrayList<>();

    public static void start(Context context) {
        context.startActivity(new Intent(context, DevicesActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_list_with_empty);

        setTitle(R.string.fragment_devices_title);

        mNoData = (TextView) findViewById(R.id.no_data_text);
        mDevices = (ListView) findViewById(R.id.list);

        mNoData.setText(getString(R.string.fragment_devices_no_data_text));

        mAdapter = new DevicesAdapter(this, getKaaManager(), mDeviceInfos);
        mDevices.setAdapter(mAdapter);

        mDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String endpointKey = getKaaManager().getRemoteDeviceEndpoint(position);
                AlbumsActivity.start(DevicesActivity.this, endpointKey);
            }
        });

        updateInfo();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_photo_frame, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_refresh:
                updateInfo();
                break;
            case R.id.item_logout:
                LoginActivity.logout(this);
                getKaaManager().logout();
                break;
            default:
                return false;
        }

        return true;
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


    private void notifyView() {
        if (mAdapter.getCount() > 0) {
            mNoData.setVisibility(View.GONE);
            mDevices.setVisibility(View.VISIBLE);
        } else {
            mDevices.setVisibility(View.GONE);
            mNoData.setVisibility(View.VISIBLE);
        }
    }

    private void updateInfo() {
        getKaaManager().discoverRemoteDevices();
    }

    private void updateView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeviceInfos.clear();
                mDeviceInfos.addAll(getKaaManager().getRemoteDevicesMap().values());

                mAdapter.notifyDataSetChanged();
                notifyView();
            }
        });
    }
}
