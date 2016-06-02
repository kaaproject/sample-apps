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

import java.util.List;

import org.greenrobot.eventbus.Subscribe;
import org.kaaproject.kaa.demo.cityguide.Category;
import org.kaaproject.kaa.demo.cityguide.Place;
import org.kaaproject.kaa.demo.cityguide.R;
import org.kaaproject.kaa.demo.cityguide.adapter.PlacesAdapter;
import org.kaaproject.kaa.demo.cityguide.event.Events;
import org.kaaproject.kaa.demo.cityguide.util.FragmentUtils;
import org.kaaproject.kaa.demo.cityguide.util.Utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The implementation of the {@link CityGuideFragment} class.
 * Represents a view with a list of places.
 */
public class PlacesFragment extends CityGuideFragment {

    private static final String ARG_AREA_NAME = "arg-area";
    private static final String ARG_CITY_NAME = "arg-city";
    private static final String ARG_CATEGORY_JSON = "arg-category";

    private String mAreaName;
    private String mCityName;
    private Category mPlaceCategory;

    private ListView mPlacesListView;
    private PlacesAdapter mPlacesAdapter;

    public PlacesFragment() {
        super();
    }

    public static Fragment newInstance(String areaName, String cityName, Category placeCategory) {
        PlacesFragment fragment = new PlacesFragment();

        Bundle args = new Bundle();
        args.putString(ARG_AREA_NAME, areaName);
        args.putString(ARG_CITY_NAME, cityName);
        args.putString(ARG_CATEGORY_JSON, new Gson().toJson(placeCategory));

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mAreaName = getArguments().getString(ARG_AREA_NAME);
            mCityName = getArguments().getString(ARG_CITY_NAME);
            mPlaceCategory = new Gson().fromJson(getArguments().getString(ARG_CATEGORY_JSON), Category.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_places, container,
                false);

        mPlacesListView = (ListView) rootView.findViewById(R.id.placesList);
        mPlacesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                onPlaceClicked(position);
            }
        });

        List<Place> places = Utils.getPlaces(mApplication.getCityGuideConfiguration(), mAreaName,
                mCityName, mPlaceCategory);

        if (places != null) {
            mPlacesAdapter = new PlacesAdapter(mActivity, mApplication.getImageLoader(), places);
            mPlacesListView.setAdapter(mPlacesAdapter);
        }
        return rootView;
    }

    private void onPlaceClicked(int position) {
        Place place = mPlacesAdapter.getItem(position);
        PlaceFragment placeFragment = new PlaceFragment(mAreaName, mCityName,
                mPlaceCategory, place.getTitle());

        FragmentUtils.addBackStackFragment(mActivity, placeFragment);
    }

    @Subscribe
    public void onEventMainThread(Events.ConfigurationUpdated configurationUpdated) {
        List<Place> places = Utils.getPlaces(
                mApplication.getCityGuideConfiguration(), mAreaName, mCityName,
                mPlaceCategory);
        if (places != null) {
            mPlacesAdapter = new PlacesAdapter(mActivity,
                    mApplication.getImageLoader(), places);
            mPlacesListView.setAdapter(mPlacesAdapter);
        }
    }

    @Override
    protected boolean updateActionBar() {
        return false;
    }

    @Override
    protected boolean useEventBus() {
        return true;
    }

    @Override
    protected boolean displayHomeAsUp() {
        return true;
    }

}
