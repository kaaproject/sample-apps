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
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.kaaproject.kaa.demo.cityguide.Category;
import org.kaaproject.kaa.demo.cityguide.City;
import org.kaaproject.kaa.demo.cityguide.R;
import org.kaaproject.kaa.demo.cityguide.adapter.CityPagerAdapter;
import org.kaaproject.kaa.demo.cityguide.event.Events;
import org.kaaproject.kaa.demo.cityguide.util.GuideConstants;
import org.kaaproject.kaa.demo.cityguide.util.KaaUtils;

/**
 * The implementation of the {@link BaseFragment} class.
 * Represents tabs with list views of city places separated by the place {@link Category}.
 */
public class CityFragment extends BaseFragment {

    private View mWaitView;
    private View mCityPages;
    // TODO: move out
//    private TabPageIndicator mCityPageIndicator;
    private ViewPager mCityPager;
    private String mAreaName;
    private String mCityName;

    public static CityFragment newInstance(String areaName, String cityName) {
        CityFragment fragment = new CityFragment();

        Bundle bundle = new Bundle();
        bundle.putString(GuideConstants.AREA_NAME, areaName);
        bundle.putString(GuideConstants.CITY_NAME, cityName);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAreaName = getArguments().getString(GuideConstants.AREA_NAME);
            mCityName = getArguments().getString(GuideConstants.CITY_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_city, container, false);

        mWaitView = rootView.findViewById(R.id.waitProgress);
        mCityPages = rootView.findViewById(R.id.cityPages);

//        mCityPageIndicator = (TabPageIndicator) rootView.findViewById(R.id.cityPageIndicator);
        mCityPager = (ViewPager) rootView.findViewById(R.id.cityPager);

        if (manager.isKaaStarted()) {
            showCity();
        } else {
            Toast.makeText(getContext(), R.string.no_city, Toast.LENGTH_SHORT).show();
        }

        return rootView;
    }

    private void showCity() {
        mWaitView.setVisibility(View.GONE);
        City city = KaaUtils.getCity(manager.getAreas(), mAreaName, mCityName);

        if (city != null) {
            CityPagerAdapter mCityPagerAdapter = new CityPagerAdapter(getActivity(), mAreaName, mCityName);

            mCityPages.setVisibility(View.VISIBLE);
            mCityPager.setAdapter(mCityPagerAdapter);
//            mCityPageIndicator.setViewPager(mCityPager);
        } else {
            popBackStack(getActivity());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Subscribe
    public void onEvent(Events.KaaStarted kaaStarted) {
        showCity();
    }

    @Subscribe
    public void onEvent(Events.ConfigurationUpdated configurationUpdated) {
        City city = KaaUtils.getCity(manager.getAreas(), mAreaName, mCityName);

        if (city == null) {
            popBackStack(getActivity());
        }
    }

    @Override
    public String getTitle() {
        return mCityName;
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
