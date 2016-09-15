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

package org.kaaproject.kaa.demo.cellmonitor.manager;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import org.kaaproject.kaa.demo.cellmonitor.R;
import org.kaaproject.kaa.demo.cellmonitor.util.CellMonitorConstants;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Manager for working with user location.
 * Can get user location and check user location change.
 */
public class LocationManagerWrapper {

    private LocationManager mLocationManager;
    private GpsLocationListener mGpsLocationListener;
    private Location mGpsLocation;
    private Handler cellCallback;

    public LocationManagerWrapper(Context context, Handler cellCallback) {
        this.cellCallback = cellCallback;

        mGpsLocationListener = new GpsLocationListener();
        mGpsLocation = getLocation(context);
    }

    private Location getLocation(Context context) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location bestLocation = null;
        Location lastKnownLocation = null;

        try {
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
        } catch (SecurityException securityException) {
            CellMonitorConstants.LOG.error("Can't receive last known location", securityException);
            Toast.makeText(context, R.string.accept_location_permission, Toast.LENGTH_LONG).show();
        }

        return lastKnownLocation;
    }

    public void pause() {
        try {
            mLocationManager.removeUpdates(mGpsLocationListener);
        } catch (SecurityException securityException) {
            CellMonitorConstants.LOG.error("Can't pause location update");
            throw securityException;
        }
    }

    public void resume() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String bestProvider = mLocationManager.getBestProvider(criteria, true);
        try {
            mLocationManager.requestLocationUpdates(bestProvider, 5, 1000, mGpsLocationListener);
        } catch (SecurityException securityException) {
            CellMonitorConstants.LOG.error("Can't resume location update", securityException);
            throw securityException;
        }
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
