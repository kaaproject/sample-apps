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

import org.kaaproject.kaa.demo.cityguide.Area;
import org.kaaproject.kaa.demo.cityguide.Category;
import org.kaaproject.kaa.demo.cityguide.City;
import org.kaaproject.kaa.demo.cityguide.Place;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class that provides static functions to query data from the city guide configuration object.
 */
public class KaaUtils {

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
        if (cities == null) {
            return null;
        }
        for (City city : cities) {
            if (city.getName().equals(cityName)) {
                return city;
            }
        }

        return null;
    }

    public static List<Place> getPlaces(List<Area> areas, String areaName, String cityName,
                                        Category placeCategory) {
        City city = getCity(areas, areaName, cityName);

        List<Place> places = new ArrayList<>();
        for (Place place : city.getPlaces()) {
            if (place.getCategory() == placeCategory) {
                places.add(place);
            }
        }
        return places;
    }

    public static Place getPlace(List<Area> areas, String areaName, String cityName,
                                 Category placeCategory, String placeName) {
        List<Place> places = getPlaces(areas, areaName, cityName, placeCategory);
        if (places == null) {
            return null;
        }

        for (Place place : places) {
            if (place.getTitle().equals(placeName)) {
                return place;
            }
        }

        return null;
    }

}
