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

package org.kaaproject.kaa.demo.cityguide.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.kaaproject.kaa.demo.cityguide.Area;
import org.kaaproject.kaa.demo.cityguide.R;
import org.kaaproject.kaa.demo.cityguide.adapter.AreasAdapter;
import org.kaaproject.kaa.demo.cityguide.event.Events;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the {@link BaseFragment} class.
 * Represents a view with a list of areas.
 */
public class AreasFragment extends BaseFragment {

    private View mWaitView;
    private ListView mAreasListView;

    private List<Area> areas = new ArrayList<>();
    private AreasAdapter mAreasAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_areas, container, false);

        mWaitView = rootView.findViewById(R.id.waitProgress);
        mAreasListView = (ListView) rootView.findViewById(R.id.areasList);

        mAreasAdapter = new AreasAdapter(getActivity(), areas);
        mAreasListView.setAdapter(mAreasAdapter);

        mAreasListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                onAreaClicked(position);
            }
        });

        if (manager.isKaaStarted()) {
            updateAreas();
        } else {
            Toast.makeText(getContext(), R.string.no_areas, Toast.LENGTH_SHORT).show();
        }

        return rootView;
    }

    private void updateAreas() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWaitView.setVisibility(View.GONE);
                mAreasListView.setVisibility(View.VISIBLE);

                areas.clear();
                areas.addAll(manager.getAreas());

                mAreasAdapter.notifyDataSetChanged();
            }
        });
    }

    private void onAreaClicked(int position) {
        Area area = mAreasAdapter.getItem(position);

        CitiesFragment citiesFragment = CitiesFragment.newInstance(area.getName());
        move(getActivity(), citiesFragment, citiesFragment.getTitle());
    }


    @Subscribe
    public void onEvent(Events.KaaStarted kaaStarted) {
        updateAreas();
    }

    @Subscribe
    public void onEvent(Events.ConfigurationUpdated configurationUpdated) {
        updateAreas();
    }

    @Override
    public String getTitle() {
        return getString(R.string.areas_title);
    }

    @Override
    protected boolean saveInfo() {
        return false;
    }

    @Override
    protected boolean displayHomeAsUp() {
        return false;
    }

}
