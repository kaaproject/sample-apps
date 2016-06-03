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

package org.kaaproject.kaa.demo.cityguide.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.kaaproject.kaa.demo.cityguide.Area;
import org.kaaproject.kaa.demo.cityguide.Category;
import org.kaaproject.kaa.demo.cityguide.City;
import org.kaaproject.kaa.demo.cityguide.CityGuideConfig;
import org.kaaproject.kaa.demo.cityguide.Place;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * A utility class that provides static functions to handle the show on map requests,
 * copy streams and query data from the city guide configuration object.
 */
public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    private static final String MAPS_PACKAGE_NAME = "com.google.android.apps.maps";
    private static final String MAPS_CLASS_NAME = "com.google.android.maps.MapsActivity";

    public static void showOnMap(Context context, double latitude,
                                 double longitude) {
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

    public static String formatLatitudeLongitude(String format,
                                                 double latitude, double longitude) {
        return String.format(Locale.ENGLISH, format, latitude, longitude);
    }

    public static void copyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Unable to copy stream!", ex);
        }
    }

    // TODO: rewrite
    public static List<City> getCities(List<Area> areas, String areaName) {
        for (Area area : areas) {
            if (area.getName().equals(areaName)) {
                return area.getCities();
            }
        }
        return new ArrayList<>();
    }

    public static City getCity(List<Area> areas, String areaName, String cityName) {
        List<City> cities = getCities(areas, areaName);
        if (cities != null) {
            for (City city : cities) {
                if (city.getName().equals(cityName)) {
                    return city;
                }
            }
        }
        return null;
    }

    public static List<Place> getPlaces(List<Area> areas, String areaName, String cityName,
                                        Category placeCategory) {

        List<City> cities = getCities(areas, areaName);
        if (cities != null) {
            for (City city : cities) {
                if (city.getName().equals(cityName)) {
                    List<Place> places = new ArrayList<>();
                    for (Place place : city.getPlaces()) {
                        if (place.getCategory() == placeCategory) {
                            places.add(place);
                        }
                    }
                    return places;
                }
            }
        }
        return null;
    }

    public static Place getPlace(List<Area> areas, String areaName, String cityName,
                                 Category placeCategory, String placeName) {

        List<Place> places = getPlaces(areas, areaName, cityName, placeCategory);
        if (places != null) {
            for (Place place : places) {
                if (place.getTitle().equals(placeName)) {
                    return place;
                }
            }
        }
        return null;
    }

}
