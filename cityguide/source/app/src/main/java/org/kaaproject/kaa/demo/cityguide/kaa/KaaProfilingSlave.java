package org.kaaproject.kaa.demo.cityguide.kaa;

import org.kaaproject.kaa.demo.cityguide.profile.CityGuideProfile;

public class KaaProfilingSlave {

    private CityGuideProfile mProfile;

    public KaaProfilingSlave() {
        /*
         * Create an empty city guide profile object based on the user-defined profile
         * schema.
         */
        this.mProfile = new CityGuideProfile();
    }

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