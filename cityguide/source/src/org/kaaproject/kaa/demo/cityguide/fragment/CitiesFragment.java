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

package org.kaaproject.kaa.demo.cityguide.fragment;

import java.util.List;

import org.kaaproject.kaa.demo.cityguide.City;
import org.kaaproject.kaa.demo.cityguide.R;
import org.kaaproject.kaa.demo.cityguide.adapter.CitiesAdapter;
import org.kaaproject.kaa.demo.cityguide.event.ConfigurationUpdated;
import org.kaaproject.kaa.demo.cityguide.event.KaaStarted;
import org.kaaproject.kaa.demo.cityguide.util.FragmentUtils;
import org.kaaproject.kaa.demo.cityguide.util.Utils;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * The implementation of the {@link CityGuideFragment} class. 
 * Represents a view with a list of cities.
 */
public class CitiesFragment extends CityGuideFragment {

    private View mWaitView;
    private ListView mCitiesListView;
    private String mAreaName;
    private CitiesAdapter mCitiesAdapter;

    public static CitiesFragment createInstance(String areaName) {
        CitiesFragment fragment = new CitiesFragment();
        Bundle bundle = new Bundle();
        bundle.putString(AREA_NAME, areaName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mAreaName = arguments.getString(AREA_NAME);
        }
        if (mAreaName == null) {
            mAreaName = savedInstanceState.getString(AREA_NAME);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAreaName != null) {
            outState.putString(AREA_NAME, mAreaName);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cities, container,
                false);

        mWaitView = rootView.findViewById(R.id.waitProgress);
        mCitiesListView = (ListView) rootView.findViewById(R.id.citiesList);
        mCitiesListView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        onCityClicked(position);
                    }
                });
        if (mApplication.isKaaStarted()) {
            showCities();
        }
        return rootView;
    }

    private void showCities() {
        mWaitView.setVisibility(View.GONE);
        List<City> cities = Utils.getCities(
                mApplication.getCityGuideConfiguration(), mAreaName);
        if (cities != null) {
            mCitiesAdapter = new CitiesAdapter(mActivity, cities);
            mCitiesListView.setVisibility(View.VISIBLE);
            mCitiesListView.setAdapter(mCitiesAdapter);
        } else {
            FragmentUtils.popBackStack(mActivity);
        }
    }

    public void onEventMainThread(KaaStarted kaaStarted) {
        showCities();
    }

    public void onEventMainThread(ConfigurationUpdated configurationUpdated) {
        showCities();
    }

    private void onCityClicked(int position) {
        City city = mCitiesAdapter.getItem(position);
        CityFragment cityFragment = CityFragment.createInstance(mAreaName, city.getName());
        FragmentUtils.addBackStackFragment(mActivity, cityFragment);
    }

    @Override
    protected String getTitle() {
        return mAreaName;
    }

    @Override
    protected boolean displayHomeAsUp() {
        return true;
    }

}
