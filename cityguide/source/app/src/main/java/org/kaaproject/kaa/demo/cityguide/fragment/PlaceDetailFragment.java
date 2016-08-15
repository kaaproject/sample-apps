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

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.kaaproject.kaa.demo.cityguide.Category;
import org.kaaproject.kaa.demo.cityguide.Place;
import org.kaaproject.kaa.demo.cityguide.R;
import org.kaaproject.kaa.demo.cityguide.event.Events;
import org.kaaproject.kaa.demo.cityguide.ui.ProgressImageView;
import org.kaaproject.kaa.demo.cityguide.util.GuideConstants;
import org.kaaproject.kaa.demo.cityguide.util.KaaUtils;

import java.util.Locale;

/**
 * The implementation of the {@link BaseFragment} class.
 * Represents a view with the information about the place including its photo, name and description.
 * Provides the 'Shown on map' button to show the place location on a map via an external activity.
 */
public class PlaceDetailFragment extends BaseFragment {

    private static final String MAPS_PACKAGE_NAME = "com.google.android.apps.maps";
    private static final String MAPS_CLASS_NAME = "com.google.android.maps.MapsActivity";

    private String mAreaName;
    private String mCityName;
    private Category mPlaceCategory;
    private String mPlaceTitle;

    private ProgressImageView mPlacePhotoView;
    private Button mShowOnMapButton;
    private TextView mPlaceTitleView;
    private TextView mPlaceDescView;

    public PlaceDetailFragment() {
        super();
    }

    public static PlaceDetailFragment newInstance(String areaName, String cityName,
                                                  Category placeCategory, String placeTitle) {
        PlaceDetailFragment fragment = new PlaceDetailFragment();

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
        if (getArguments() != null) {
            mAreaName = getArguments().getString(GuideConstants.AREA_NAME);
            mCityName = getArguments().getString(GuideConstants.CITY_NAME);
            mPlaceCategory = Category.values()[getArguments().getInt(GuideConstants.PLACE_CATEGORY)];
            mPlaceTitle = getArguments().getString(GuideConstants.PLACE_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_place, container, false);

        mPlacePhotoView = (ProgressImageView) rootView.findViewById(R.id.placePhoto);
        mShowOnMapButton = (Button) rootView.findViewById(R.id.showOnMap);
        mPlaceTitleView = (TextView) rootView.findViewById(R.id.placeName);
        mPlaceDescView = (TextView) rootView.findViewById(R.id.placeDesc);

        showPlace();
        return rootView;
    }

    private void showPlace() {
        final Place place = KaaUtils.getPlace(manager.getAreas(), mAreaName, mCityName,
                mPlaceCategory, mPlaceTitle);

        if (place != null) {
            mPlacePhotoView.setImage(place.getPhotoUrl());

            mPlaceTitleView.setText(place.getTitle());
            mPlaceDescView.setText(place.getDescription());
            mShowOnMapButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showOnMap(getActivity(), place.getLocation().getLatitude(),
                            place.getLocation().getLongitude());
                }
            });
        } else {
            popBackStack(getActivity());
        }
    }

    public static void showOnMap(Context context, double latitude, double longitude) {
        String uri = formatLatitudeLongitude(
                "http://maps.google.com/maps?f=q&q=(%f,%f)", latitude,
                longitude);
        try {
            ComponentName compName = new ComponentName(MAPS_PACKAGE_NAME,
                    MAPS_CLASS_NAME);
            Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    .setComponent(compName);
            context.startActivity(mapsIntent);
        } catch (ActivityNotFoundException exeption) {
            String url = formatLatitudeLongitude("geo:%f,%f", latitude,
                    longitude);
            Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            try {
                context.startActivity(mapsIntent);
            } catch (ActivityNotFoundException notFoundException) {
                mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                context.startActivity(mapsIntent);
            }
        }
    }

    public static String formatLatitudeLongitude(String format, double latitude, double longitude) {
        return String.format(Locale.ENGLISH, format, latitude, longitude);
    }


    @Subscribe
    public void onEvent(Events.ConfigurationUpdated configurationUpdated) {
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
