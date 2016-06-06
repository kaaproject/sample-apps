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
import org.kaaproject.kaa.demo.cityguide.City;
import org.kaaproject.kaa.demo.cityguide.R;
import org.kaaproject.kaa.demo.cityguide.adapter.CitiesAdapter;
import org.kaaproject.kaa.demo.cityguide.event.Events;
import org.kaaproject.kaa.demo.cityguide.util.GuideConstants;
import org.kaaproject.kaa.demo.cityguide.util.KaaUtils;

import java.util.List;

/**
 * The implementation of the {@link BaseFragment} class.
 * Represents a view with a list of cities.
 */
public class CitiesFragment extends BaseFragment {

    private View mWaitView;
    private ListView mCitiesListView;
    private String mAreaName;
    private CitiesAdapter mCitiesAdapter;

    public static CitiesFragment newInstance(String areaName) {
        CitiesFragment fragment = new CitiesFragment();
        Bundle bundle = new Bundle();
        bundle.putString(GuideConstants.AREA_NAME, areaName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mAreaName = getArguments().getString(GuideConstants.AREA_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cities, container, false);

        mWaitView = rootView.findViewById(R.id.waitProgress);
        mCitiesListView = (ListView) rootView.findViewById(R.id.citiesList);

        mCitiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                onCityClicked(position);
            }
        });

        if (manager.isKaaStarted()) {
            showCities();
        } else {
            Toast.makeText(getContext(), R.string.no_cities, Toast.LENGTH_SHORT).show();
        }

        return rootView;
    }

    private void showCities() {
        mWaitView.setVisibility(View.GONE);
        List<City> cities = KaaUtils.getCities(manager.getAreas(), mAreaName);

        if (!cities.isEmpty()) {
            mCitiesAdapter = new CitiesAdapter(getContext(), cities);
            mCitiesListView.setVisibility(View.VISIBLE);
            mCitiesListView.setAdapter(mCitiesAdapter);
        } else {
            popBackStack(getActivity());
        }
    }

    @Subscribe
    public void onEvent(Events.KaaStarted kaaStarted) {
        showCities();
    }

    @Subscribe
    public void onEvent(Events.ConfigurationUpdated configurationUpdated) {
        showCities();
    }

    private void onCityClicked(int position) {
        City city = mCitiesAdapter.getItem(position);
        CityFragment cityFragment = CityFragment.newInstance(mAreaName, city.getName());

        move(getActivity(), cityFragment, cityFragment.getTitle());
    }

    @Override
    public String getTitle() {
        return mAreaName;
    }

    @Override
    protected boolean saveInfo() {
        return false;
    }

    @Override
    protected boolean displayHomeAsUp() {
        return true;
    }

}
