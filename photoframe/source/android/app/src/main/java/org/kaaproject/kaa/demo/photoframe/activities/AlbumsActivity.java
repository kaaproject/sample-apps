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


package org.kaaproject.kaa.demo.photoframe.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.kaaproject.kaa.demo.photoframe.AlbumInfo;
import org.kaaproject.kaa.demo.photoframe.PlayInfo;
import org.kaaproject.kaa.demo.photoframe.PlayStatus;
import org.kaaproject.kaa.demo.photoframe.R;
import org.kaaproject.kaa.demo.photoframe.adapter.AlbumsAdapter;
import org.kaaproject.kaa.demo.photoframe.communication.Events;

import java.util.ArrayList;
import java.util.List;

public class AlbumsActivity extends BaseActivity {

    private static final String ENDPOINT_KEY = "endpointKey";

    TextView mNoDataTextView;
    ListView mAlbumsListView;

    private final List<AlbumInfo> mAlbums = new ArrayList<>();

    private AlbumsAdapter mAdapter;
    private String mEndpointKey;

    private boolean mIsPlaying;

    public static void start(Activity activity, String endpointKey) {
        activity.startActivity(new Intent(activity, AlbumsActivity.class).putExtra(ENDPOINT_KEY, endpointKey));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_list_with_empty);

        mEndpointKey = getIntent().getStringExtra(ENDPOINT_KEY);

        final ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        try {
            actionBar.setTitle(getKaaManager().getRemoteDeviceModel(mEndpointKey));
        } catch (IllegalStateException e) {
            Toast.makeText(this, "Cannot open this device. App was closed.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        mNoDataTextView = (TextView) findViewById(R.id.no_data_text);
        mAlbumsListView = (ListView) findViewById(R.id.list);

        mNoDataTextView.setText(getString(R.string.fragment_albums_no_data_text));

        mAdapter = new AlbumsAdapter(this, mAlbums);

        mAlbumsListView.setAdapter(mAdapter);

        mAlbumsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlbumInfo album = getKaaManager().getRemoteDeviceAlbums(mEndpointKey).get(position);
                getKaaManager().playRemoteDeviceAlbum(mEndpointKey, album.getBucketId());

                mIsPlaying = true;
                invalidateOptionsMenu();

                updateAdapter();
            }
        });

        updateAdapter();

        mIsPlaying = getKaaManager().getRemoteDeviceStatus(mEndpointKey).getStatus() == PlayStatus.PLAYING;

        requestInfo();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_photo_frame, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.item_stop).setVisible(mIsPlaying);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.item_stop:
                mIsPlaying = false;
                getKaaManager().stopPlayRemoteDeviceAlbum(mEndpointKey);
                item.setVisible(mIsPlaying);
                break;
            case R.id.item_refresh:
                requestInfo();
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

    @Subscribe
    public void onEvent(Events.AlbumListEvent albumListEvent) {
        if (albumListEvent.getEndpointKey().equals(mEndpointKey)) {
            updateAdapter();
        }
    }

    @Subscribe
    public void onEvent(Events.PlayInfoEvent playInfoEvent) {
        if (playInfoEvent.getEndpointKey().equals(mEndpointKey)) {
            updateAdapter();
        }
    }

    private void notifyView() {
        if (mAdapter.getCount() > 0) {
            mNoDataTextView.setVisibility(View.GONE);
            mAlbumsListView.setVisibility(View.VISIBLE);
        } else {
            mAlbumsListView.setVisibility(View.GONE);
            mNoDataTextView.setVisibility(View.VISIBLE);
        }
    }

    private void updateAdapter() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                final PlayInfo playInfo = getKaaManager().getRemoteDeviceStatus(mEndpointKey);
                mAdapter.setPlayInfo(playInfo);

                mAlbums.clear();
                mAlbums.addAll(getKaaManager().getRemoteDeviceAlbums(mEndpointKey));

                mAdapter.notifyDataSetChanged();

                notifyView();
            }
        });
    }

    private void requestInfo() {
        getKaaManager().requestRemoteDeviceInfo(mEndpointKey);
    }
}
