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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.greenrobot.eventbus.Subscribe;
import org.kaaproject.kaa.demo.cityguide.Category;
import org.kaaproject.kaa.demo.cityguide.Place;
import org.kaaproject.kaa.demo.cityguide.R;
import org.kaaproject.kaa.demo.cityguide.adapter.PlacesAdapter;
import org.kaaproject.kaa.demo.cityguide.event.Events;
import org.kaaproject.kaa.demo.cityguide.util.GuideConstants;
import org.kaaproject.kaa.demo.cityguide.util.KaaUtils;

import java.util.List;

/**
 * The implementation of the {@link BaseFragment} class.
 * Represents a view with a list of places.
 */
public class PlacesFragment extends BaseFragment {

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
        args.putString(GuideConstants.AREA_NAME, areaName);
        args.putString(GuideConstants.CITY_NAME, cityName);
        args.putInt(GuideConstants.PLACE_CATEGORY, placeCategory.ordinal());

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mAreaName = getArguments().getString(GuideConstants.AREA_NAME);
            mCityName = getArguments().getString(GuideConstants.CITY_NAME);
            mPlaceCategory = Category.values()[getArguments().getInt(GuideConstants.PLACE_CATEGORY)];
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

        List<Place> places = KaaUtils.getPlaces(manager.getAreas(), mAreaName, mCityName, mPlaceCategory);

        if (places != null) {
            mPlacesAdapter = new PlacesAdapter(getActivity(), places);
            mPlacesListView.setAdapter(mPlacesAdapter);
        }
        return rootView;
    }

    private void onPlaceClicked(int position) {
        Place place = mPlacesAdapter.getItem(position);
        PlaceDetailFragment placeDetailFragment = PlaceDetailFragment.newInstance(mAreaName, mCityName,
                mPlaceCategory, place.getTitle());

        move(getActivity(), placeDetailFragment, placeDetailFragment.getTitle());
    }

    @Subscribe
    public void onEvent(Events.ConfigurationUpdated configurationUpdated) {
        List<Place> places = KaaUtils.getPlaces(manager.getAreas(), mAreaName, mCityName, mPlaceCategory);

        if (places != null) {
            mPlacesAdapter = new PlacesAdapter(getContext(), places);
            mPlacesListView.setAdapter(mPlacesAdapter);
        }
    }

    @Override
    public String getTitle() {
        return mAreaName + "_" + mCityName;
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
