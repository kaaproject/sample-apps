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
import org.kaaproject.kaa.demo.photoframe.AlbumInfo;
import org.kaaproject.kaa.demo.photoframe.PlayInfo;
import org.kaaproject.kaa.demo.photoframe.PlayStatus;
import org.kaaproject.kaa.demo.photoframe.R;
import org.kaaproject.kaa.demo.photoframe.adapter.AlbumsAdapter;
import org.kaaproject.kaa.demo.photoframe.communication.Events;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the {@link BaseFragment} class.
 * Represents a view with a list of remote device albums.
 */
public class AlbumsFragment extends BaseFragment {

    private static final String ENDPOINT_KEY = "endpointKey";

    TextView mNoDataTextView;
    ListView mAlbumsListView;
    SwipeRefreshLayout mSwipeRefresh;

    private final List<AlbumInfo> mAlbums = new ArrayList<>();

    private AlbumsAdapter mAdapter;
    private String mEndpointKey;

    private boolean mIsPlaying;

    public static AlbumsFragment newInstance(String endpointKey) {
        final AlbumsFragment fragment = new AlbumsFragment();

        final Bundle bundle = new Bundle();
        bundle.putString(ENDPOINT_KEY, endpointKey);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mEndpointKey = getArguments().getString(ENDPOINT_KEY);
        }

        setHasOptionsMenu(true);

        requestInfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_list_with_refresh_and_empty, container, false);

        mNoDataTextView = (TextView) rootView.findViewById(R.id.no_data_text);
        mAlbumsListView = (ListView) rootView.findViewById(R.id.list);
        mSwipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);

        mNoDataTextView.setText(getString(R.string.fragment_albums_no_data_text));

        final PlayInfo playInfo = getKaaManager().getRemoteDeviceStatus(mEndpointKey);
        mAdapter = new AlbumsAdapter(getActivity(), playInfo, mAlbums);

        mAlbumsListView.setAdapter(mAdapter);

        mAlbumsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlbumInfo album = getKaaManager().getRemoteDeviceAlbums(mEndpointKey).get(position);
                getKaaManager().playRemoteDeviceAlbum(mEndpointKey, album.getBucketId());

                mIsPlaying = true;
                getActivity().invalidateOptionsMenu();

                updateAdapter();
            }
        });

        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefresh.setRefreshing(true);
                updateAdapter();

                mSwipeRefresh.setRefreshing(false);
            }
        });

        updateAdapter();

        mIsPlaying = getKaaManager().getRemoteDeviceStatus(mEndpointKey).getStatus() == PlayStatus.PLAYING;

        return rootView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        getActivity().getMenuInflater().inflate(R.menu.menu_photo_frame, menu);
        menu.findItem(R.id.item_stop).setVisible(mIsPlaying);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                popBackStack(getActivity());
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
                getKaaManager().logout();
                break;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public String getTitle() {
        return getKaaManager().getRemoteDeviceModel(mEndpointKey);
    }

    @Override
    public String getFragmentTag() {
        return AlbumsFragment.class.getSimpleName() + mEndpointKey;
    }

    @Override
    protected boolean displayHomeAsUp() {
        return true;
    }

    private void requestInfo() {
        getKaaManager().requestRemoteDeviceInfo(mEndpointKey);
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
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAlbums.clear();
                mAlbums.addAll(getKaaManager().getRemoteDeviceAlbums(mEndpointKey));

                mAdapter.notifyDataSetChanged();

                notifyView();
            }
        });
    }
}
