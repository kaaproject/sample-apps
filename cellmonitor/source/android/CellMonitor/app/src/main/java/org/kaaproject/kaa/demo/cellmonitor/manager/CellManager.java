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
import android.os.Handler;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import org.kaaproject.kaa.demo.cellmonitor.util.CellMonitorConstants;

/**
 * Manager for user mobile cell operations.
 * Can get information about telephone status and check for cell location and
 * signal strength changes.
 */
public class CellManager {

    private TelephonyManager mTelephonyManager;
    private CellMonitorPhoneStateListener mCellMonitorPhoneStateListener;
    private CellLocation mCellLocation;
    private SignalStrength mSignalStrength;
    private Handler cellCallback;

    public CellManager(Context context, Handler cellCallback) {
        this.cellCallback = cellCallback;
        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        mCellMonitorPhoneStateListener = new CellMonitorPhoneStateListener();
    }

    public void pause() {
        mTelephonyManager.listen(mCellMonitorPhoneStateListener,
                PhoneStateListener.LISTEN_NONE);
    }

    public void resume() {
        mTelephonyManager.listen(mCellMonitorPhoneStateListener,
                PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    public CellLocation getCellLocation() {
        return mCellLocation;
    }

    public SignalStrength getSignalStrength() {
        return mSignalStrength;
    }

    public TelephonyManager getTelephonyManager() {
        return mTelephonyManager;
    }

    private class CellMonitorPhoneStateListener extends PhoneStateListener {

        @Override
        public void onCellLocationChanged(CellLocation location) {
            mCellLocation = location;

            cellCallback.sendEmptyMessage(CellMonitorConstants.CELL_LOCATION_CHANGED);
            CellMonitorConstants.LOG.info("Cell location changed!");
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            mSignalStrength = signalStrength;

            cellCallback.sendEmptyMessage(CellMonitorConstants.SIGNAL_STRENGTH_CHANGED);
            CellMonitorConstants.LOG.info("Signal strength changed!");
        }
    }

}
