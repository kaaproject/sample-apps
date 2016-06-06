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

import org.greenrobot.eventbus.Subscribe;
import org.kaaproject.kaa.demo.cityguide.Category;
import org.kaaproject.kaa.demo.cityguide.CityGuideApplication;
import org.kaaproject.kaa.demo.cityguide.MainActivity;
import org.kaaproject.kaa.demo.cityguide.Place;
import org.kaaproject.kaa.demo.cityguide.R;
import org.kaaproject.kaa.demo.cityguide.event.Events;
import org.kaaproject.kaa.demo.cityguide.image.ImageLoader.ImageType;
import org.kaaproject.kaa.demo.cityguide.image.LoadingImageView;
import org.kaaproject.kaa.demo.cityguide.util.FragmentUtils;
import org.kaaproject.kaa.demo.cityguide.util.GuideConstants;
import org.kaaproject.kaa.demo.cityguide.util.Utils;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * The implementation of the {@link BaseFragment} class.
 * Represents a view with the information about the place including its photo, name and description.
 * Provides the 'Shown on map' button to show the place location on a map via an external activity.
 */
public class PlaceFragment extends BaseFragment {

    private String mAreaName;
    private String mCityName;
    private Category mPlaceCategory;
    private String mPlaceTitle;

    private LoadingImageView mPlacePhotoView;
    private Button mShowOnMapButton;
    private TextView mPlaceTitleView;
    private TextView mPlaceDescView;

    public PlaceFragment() {
        super();
    }

    public static PlaceFragment newInstance(String areaName, String cityName,
                                            Category placeCategory, String placeTitle) {
        PlaceFragment fragment = new PlaceFragment();

        Bundle args = new Bundle();
        args.putString(GuideConstants.AREA_NAME, areaName);
        args.putString(GuideConstants.CITY_NAME, cityName);
        args.putInt(GuideConstants.PLACE_CATEGORY, placeCategory.ordinal());
        args.putString(GuideConstants.PLACE_TITLE, placeTitle);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            mAreaName = getArguments().getString(GuideConstants.AREA_NAME);
            mCityName = getArguments().getString(GuideConstants.CITY_NAME);
            mPlaceCategory = Category.values()[getArguments().getInt(GuideConstants.PLACE_CATEGORY)];
            mPlaceTitle = getArguments().getString(GuideConstants.PLACE_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_place, container,
                false);

        mPlacePhotoView = (LoadingImageView) rootView.findViewById(R.id.placePhoto);
        mShowOnMapButton = (Button) rootView.findViewById(R.id.showOnMap);
        mPlaceTitleView = (TextView) rootView.findViewById(R.id.placeName);
        mPlaceDescView = (TextView) rootView.findViewById(R.id.placeDesc);

        showPlace();
        return rootView;
    }

    private void showPlace() {
        final Place place = Utils.getPlace(manager.getAreas(), mAreaName, mCityName,
                mPlaceCategory, mPlaceTitle);

        if (place != null) {
            //TODO remove
            ((CityGuideApplication) ((MainActivity) getActivity()).getApplication()).getImageLoader()
                    .loadImage(place.getPhotoUrl(), mPlacePhotoView, ImageType.SCREENAIL);

            mPlaceTitleView.setText(place.getTitle());
            mPlaceDescView.setText(place.getDescription());
            mShowOnMapButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.showOnMap(getActivity(), place.getLocation()
                            .getLatitude(), place.getLocation().getLongitude());
                }
            });
        } else {
            FragmentUtils.popBackStack(getActivity());
        }
    }

    @Subscribe
    public void onEventMainThread(Events.ConfigurationUpdated configurationUpdated) {
        showPlace();
    }

    @Override
    public String getTitle() {
        return mPlaceTitle;
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
