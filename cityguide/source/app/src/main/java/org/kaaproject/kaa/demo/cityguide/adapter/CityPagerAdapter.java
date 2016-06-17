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

package org.kaaproject.kaa.demo.cityguide.adapter;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.kaaproject.kaa.demo.cityguide.Category;
import org.kaaproject.kaa.demo.cityguide.R;
import org.kaaproject.kaa.demo.cityguide.fragment.PlacesFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the {@link FragmentStatePagerAdapter} class.
 * Used as an adapter class for the city view pager.
 * Provides fragments with places separated by the place {@link Category}.
 */
public class CityPagerAdapter extends FragmentStatePagerAdapter {

    // TODO: from server
    private static final int[] pageTitles = new int[]{R.string.hotels,
            R.string.shops, R.string.museums, R.string.restaurants};

    private FragmentActivity activity;
    private List<Fragment> fragments;

    public CityPagerAdapter(FragmentActivity activity, String areaName, String cityName) {
        super(activity.getSupportFragmentManager());

        this.activity = activity;
        fragments = new ArrayList<>(Category.values().length);

        for (int i = 0; i < Category.values().length; i++) {
            fragments.add(PlacesFragment.newInstance(areaName, cityName, Category.values()[i]));
        }
    }

    @Override
    public int getCount() {
        return Category.values().length;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return activity.getString(pageTitles[position]);
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

}
