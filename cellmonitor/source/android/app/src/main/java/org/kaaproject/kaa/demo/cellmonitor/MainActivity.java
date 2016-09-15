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

package org.kaaproject.kaa.demo.cellmonitor;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.widget.TextView;

import org.kaaproject.kaa.demo.cellmonitor.kaa.KaaManager;
import org.kaaproject.kaa.demo.cellmonitor.manager.CellManager;
import org.kaaproject.kaa.demo.cellmonitor.manager.LocationManagerWrapper;
import org.kaaproject.kaa.demo.cellmonitor.util.CellMonitorConstants;
import org.kaaproject.kaa.demo.cellmonitor.util.LocationUtil;
import org.kaaproject.kaa.demo.cellmonitor.util.NetworkUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * The implementation of {@link AppCompatActivity} class.
 * Notifies the application of the activity lifecycle changes.
 */
public class MainActivity extends AppCompatActivity {

    private TextView mNetworkOperatorValue;
    private TextView mNetworkOperatorNameValue;
    private TextView mGsmCellIdValue;
    private TextView mGsmLacValue;
    private TextView mGsmSignalStrengthValue;
    private TextView mGpsLocationValue;
    private TextView mLastLogTimeValue;
    private TextView mSentLogCountValue;
    private TextView mCollectedLogCountValue;

    private KaaManager kaaManager;
    private CellManager cellManager;
    private LocationManagerWrapper locationManagerWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cell_monitor);

        mNetworkOperatorValue = (TextView) findViewById(R.id.networkOperatorValue);
        mNetworkOperatorNameValue = (TextView) findViewById(R.id.networkOperatorNameValue);
        mGsmCellIdValue = (TextView) findViewById(R.id.gsmCellIdValue);
        mGsmLacValue = (TextView) findViewById(R.id.gsmLacValue);
        mGsmSignalStrengthValue = (TextView) findViewById(R.id.gsmSignalStrengthValue);
        mGpsLocationValue = (TextView) findViewById(R.id.gpsLocationValue);
        mLastLogTimeValue = (TextView) findViewById(R.id.lastLogTimeValue);
        mCollectedLogCountValue = (TextView) findViewById(R.id.logsCollectedValue);
        mSentLogCountValue = (TextView) findViewById(R.id.logsSentValue);

        kaaManager = new KaaManager(cellCallback);
        cellManager = new CellManager(this, cellCallback);
        locationManagerWrapper = new LocationManagerWrapper(this, cellCallback);

        kaaManager.start(this);

        if (!LocationUtil.isLocationEnabled(this)) {
            LocationUtil.getLocationSettingDialog(this).show();
            return;
        }

        if (!NetworkUtil.isNetworkAvailable(this)) {
            NetworkUtil.getNetworkDialog(this).show();
            return;
        }

        initActionBar();
        initContent();

    }

    private void initActionBar() {
        if (getSupportActionBar() != null) {
            int options = ActionBar.DISPLAY_SHOW_TITLE;
            getSupportActionBar().setDisplayOptions(options, ActionBar.DISPLAY_HOME_AS_UP
                    | ActionBar.DISPLAY_SHOW_TITLE);
            getSupportActionBar().setTitle(getText(R.string.cell_monitor_title));
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    private void initContent() {
        updateNetworkOperator();
        updateGsmCellLocation();
        updateGsmSignalStrength();
        updateGpsLocation();
        updateSentLogs();
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*
         * Notify the application of the background state.
         */
        kaaManager.pause();
        locationManagerWrapper.pause();
        cellManager.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
         * Notify the application of the foreground state.
         */
        kaaManager.resume();
        locationManagerWrapper.resume();
        cellManager.resume();
    }

    @Override
    protected void onStop() {
        super.onStop();

        kaaManager.stop();
    }

    private void updateNetworkOperator() {
        String networkOperator = cellManager.getTelephonyManager().getNetworkOperator();
        mNetworkOperatorValue.setText(!TextUtils.isEmpty(networkOperator)
                ? networkOperator : getString(R.string.unavailable));

        String networkOperatorName = cellManager.getTelephonyManager().getNetworkOperatorName();
        mNetworkOperatorNameValue.setText(!TextUtils.isEmpty(networkOperatorName)
                ? networkOperatorName : getString(R.string.unavailable));
    }

    private void updateGsmCellLocation() {
        int cid = CellMonitorConstants.UNDEFINED;
        int lac = CellMonitorConstants.UNDEFINED;

        CellLocation cellLocation = cellManager.getCellLocation();
        if (cellLocation != null && cellLocation instanceof GsmCellLocation) {
            GsmCellLocation gsmCellLocation = (GsmCellLocation) cellLocation;
            cid = gsmCellLocation.getCid();
            lac = gsmCellLocation.getLac();
        }
        mGsmCellIdValue.setText(cid != CellMonitorConstants.UNDEFINED ? String.valueOf(cid) : getString(R.string.unavailable));
        mGsmLacValue.setText(lac != CellMonitorConstants.UNDEFINED ? String.valueOf(lac) : getString(R.string.unavailable));
    }

    private void updateGsmSignalStrength() {
        int gsmSignalStrength = CellMonitorConstants.UNDEFINED;
        if (cellManager.getSignalStrength() != null) {
            gsmSignalStrength = cellManager.getSignalStrength().getGsmSignalStrength();
        }

        mGsmSignalStrengthValue.setText(gsmSignalStrength != CellMonitorConstants.UNDEFINED
                ? "" + gsmSignalStrength : getString(R.string.unavailable));
    }

    private void updateGpsLocation() {
        if (locationManagerWrapper.getGpsLocation() != null) {
            double latitude = locationManagerWrapper.getGpsLocation().getLatitude();
            double longitude = locationManagerWrapper.getGpsLocation().getLongitude();

            mGpsLocationValue.setText(String.format(getString(R.string.location_value), String.valueOf(latitude), String.valueOf(longitude)));
        } else {
            mGpsLocationValue.setText(R.string.unavailable);
        }
    }

    private void updateSentLogs() {
        SimpleDateFormat mDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
        Calendar mCalendar = Calendar.getInstance();

        long lastLogTime = kaaManager.getLastLogTime();
        if (lastLogTime > 0) {
            mCalendar.setTimeInMillis(lastLogTime);
            mLastLogTimeValue.setText(mDateFormat.format(mCalendar.getTime()));
        } else {
            mLastLogTimeValue.setText(R.string.unavailable);
        }

        mCollectedLogCountValue.setText(String.valueOf(kaaManager.getCollectedLogCount()));
        mSentLogCountValue.setText(String.valueOf(kaaManager.getSentLogCount()));
    }

    private void sendLog() {
        if (!LocationUtil.isLocationEnabled(this) || !NetworkUtil.isNetworkAvailable(this)) {
            return;
        }

        int networkOperatorCode;
        String networkOperatorName;

        String networkOperator = cellManager.getTelephonyManager().getNetworkOperator();
        if (networkOperator == null || networkOperator.isEmpty()) {
            networkOperatorCode = CellMonitorConstants.UNDEFINED;
        } else {
            networkOperatorCode = Integer.valueOf(cellManager.getTelephonyManager().getNetworkOperator());
        }
        networkOperatorName = cellManager.getTelephonyManager().getNetworkOperatorName();

        int cid = CellMonitorConstants.UNDEFINED;
        int lac = CellMonitorConstants.UNDEFINED;

        GsmCellLocation gsmCellLocation = (GsmCellLocation) cellManager.getCellLocation();
        if (gsmCellLocation != null) {
            cid = gsmCellLocation.getCid();
            lac = gsmCellLocation.getLac();
        }
        int gsmSignalStrength = CellMonitorConstants.UNDEFINED;

        if (cellManager.getSignalStrength() != null) {
            gsmSignalStrength = cellManager.getSignalStrength().getGsmSignalStrength();
        }

        double latitude = CellMonitorConstants.UNDEFINED;
        double longitude = CellMonitorConstants.UNDEFINED;

        if (locationManagerWrapper.getGpsLocation() != null) {
            latitude = locationManagerWrapper.getGpsLocation().getLatitude();
            longitude = locationManagerWrapper.getGpsLocation().getLongitude();
        }

        kaaManager.sendLog(networkOperatorCode, networkOperatorName, cid, lac, gsmSignalStrength,
                latitude, longitude);
    }

    private Handler cellCallback = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case CellMonitorConstants.CELL_LOCATION_CHANGED:
                    sendLog();
                    updateGsmCellLocation();
                    break;
                case CellMonitorConstants.GPS_LOCATION_CHANGED:
                    sendLog();
                    updateGpsLocation();
                    break;
                case CellMonitorConstants.SIGNAL_STRENGTH_CHANGED:
                    sendLog();
                    updateGsmSignalStrength();
                    break;
                case CellMonitorConstants.LOG_SENT:
                    updateSentLogs();
                    break;
            }
        }
    };
}
