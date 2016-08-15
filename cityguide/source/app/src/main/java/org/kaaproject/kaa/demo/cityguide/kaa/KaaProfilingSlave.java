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

package org.kaaproject.kaa.demo.cityguide.kaa;

import org.kaaproject.kaa.demo.cityguide.profile.CityGuideProfile;

/**
 * Implementation of Kaa Profiling mechanism
 *
 * @see <a href="http://docs.kaaproject.org/display/KAA/Endpoint+profiling">Kaa Profile Docs</a>
 */
public class KaaProfilingSlave {

    private CityGuideProfile mProfile;

    /**
     * Create an empty city guide profile object based on the user-defined profile schema.
     */
    public KaaProfilingSlave() {
        this.mProfile = new CityGuideProfile();
    }

    /**
     * Save needed info in profile
     *
     * @param area
     * @param city
     */
    public void saveInfo(String area, String city) {
        mProfile.setArea(area);
        mProfile.setCity(city);
    }

    public CityGuideProfile getProfile() {
        return mProfile;
    }

    public String getArea() {
        return mProfile.getArea();
    }

    public String getCity() {
        return mProfile.getCity();
    }
}
