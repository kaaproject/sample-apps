package org.kaaproject.kaa.demo.cellmonitor.manager;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;

import org.kaaproject.kaa.demo.cellmonitor.util.CellMonitorConstants;

/**
 */
public class LocationManager {

    private android.location.LocationManager mLocationManager;
    private GpsLocationListener mGpsLocationListener;
    private Location mGpsLocation;
    private Handler cellCallback;

    public LocationManager(Context context, Handler cellCalback) {
        this.cellCallback = cellCalback;

        mGpsLocationListener = new GpsLocationListener();
        mGpsLocation = getLocation(context);
    }

    public Location getLocation(Context context) {
        mLocationManager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location bestLocation = null;
        Location lastKnownLocation = null;

        for (String provider : mLocationManager.getAllProviders()) {
            Location location = mLocationManager.getLastKnownLocation(provider);
            if (location == null) {
                continue;
            }
            if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = location;
            }
        }

        if (bestLocation != null)
            lastKnownLocation = bestLocation;

        return lastKnownLocation;
    }

    public void pause() {
        mLocationManager.removeUpdates(mGpsLocationListener);
    }

    public void resume() {
        Criteria criteria = new Criteria();
        String bestProvider = mLocationManager.getBestProvider(criteria, false);
        mLocationManager.requestLocationUpdates(bestProvider, 0, 0, mGpsLocationListener);
    }

    public Location getGpsLocation() {
        return mGpsLocation;
    }

    private class GpsLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            mGpsLocation = location;

            cellCallback.sendEmptyMessage(CellMonitorConstants.GPS_LOCATION_CHANGED);
            CellMonitorConstants.LOG.info("GPS location changed!");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }
}
