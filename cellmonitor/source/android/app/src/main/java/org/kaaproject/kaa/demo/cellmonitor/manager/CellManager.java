package org.kaaproject.kaa.demo.cellmonitor.manager;

import android.content.Context;
import android.os.Handler;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import org.kaaproject.kaa.demo.cellmonitor.util.CellMonitorConstants;

/**
 *
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
