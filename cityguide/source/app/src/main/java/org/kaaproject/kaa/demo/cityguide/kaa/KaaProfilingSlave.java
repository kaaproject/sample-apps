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