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

package org.kaaproject.kaa.demo.cellmonitor.kaa;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaClientPlatformContext;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.logging.BucketInfo;
import org.kaaproject.kaa.client.logging.LogDeliveryListener;
import org.kaaproject.kaa.client.logging.LogFailoverCommand;
import org.kaaproject.kaa.client.logging.LogStorageStatus;
import org.kaaproject.kaa.client.logging.LogUploadStrategy;
import org.kaaproject.kaa.client.logging.LogUploadStrategyDecision;
import org.kaaproject.kaa.common.endpoint.gen.LogDeliveryErrorCode;
import org.kaaproject.kaa.demo.cellmonitor.CellMonitorLog;
import org.kaaproject.kaa.demo.cellmonitor.Location;
import org.kaaproject.kaa.demo.cellmonitor.util.CellMonitorConstants;

/**
 * The implementation of the base {@link Application} class. Performs initialization of the
 * application resources including initialization of the Kaa client. Handles the Kaa client lifecycle.
 * Implements and registers a listener to monitor a mobile cell location and the signal strength.
 * Implements and registers a listener to monitor a phone gps location.
 * Sends cell monitor log records to the Kaa cluster via the Kaa client.
 */
public class KaaManager implements LogUploadStrategy, LogDeliveryListener {

    private static final int MAX_PARALLEL_UPLOADS = 10;
    private static final int TIMEOUT_PERIOD = 100;
    private static final int UPLOAD_CHECK_PERIOD = 30;

    private KaaClient mClient;
    private boolean mKaaStarted;

    private int mSentLogCount;
    private int mCollectedLogCount = 1;
    private long mLastLogTime;

    private Handler cellCallback;

    public KaaManager(Handler cellCallback) {
        this.cellCallback = cellCallback;
    }

    /**
     * Initialize the Kaa client using the Android context.
     * Implement the onStarted callback to get notified as soon as
     * the Kaa client is operational.
     * Define a log upload strategy used by the Kaa client for logs delivery.
     * Setting callback for logs delivery.
     * Start the Kaa client workflow.
     */
    public void start(Context context) {


        KaaClientPlatformContext kaaClientContext = new AndroidKaaPlatformContext(context);
        mClient = Kaa.newClient(kaaClientContext, new SimpleKaaClientStateListener() {

            @Override
            public void onStarted() {
                CellMonitorConstants.LOG.info("Kaa client started");

                mKaaStarted = true;
            }
        }, true);

        mClient.setLogUploadStrategy(this);
        mClient.setLogDeliveryListener(this);

        mClient.start();
    }


    /**
     * Create an instance of a cell monitor log record and populate it with the latest values.
     * Pass a cell monitor log record to the Kaa client. The Kaa client will upload
     * the log record according to the defined log upload strategy.
     */
    public void sendLog(int networkOperatorCode, String networkOperator, int gsmCellId,
                        int gsmAreaLocationCode, int gsmSignalStrength,
                        double longitude, double latitude) {

        if (mKaaStarted) {

            mCollectedLogCount++;
            mLastLogTime = System.currentTimeMillis();

            CellMonitorLog cellMonitorLog = new CellMonitorLog();
            cellMonitorLog.setLogTime(mLastLogTime);

            cellMonitorLog.setNetworkOperatorCode(networkOperatorCode);
            cellMonitorLog.setNetworkOperatorName(networkOperator);

            cellMonitorLog.setGsmCellId(gsmCellId);
            cellMonitorLog.setGsmLac(gsmAreaLocationCode);

            cellMonitorLog.setSignalStrength(gsmSignalStrength);

            Location phoneLocation = new Location();
            phoneLocation.setLatitude(latitude);
            phoneLocation.setLongitude(longitude);
            cellMonitorLog.setPhoneGpsLocation(phoneLocation);

            mClient.addLogRecord(cellMonitorLog);

            cellCallback.sendEmptyMessage(CellMonitorConstants.LOG_SENT);
        }
    }

    /**
     * Stop the Kaa client. Release all network connections and application
     * resources. Shut down all the Kaa client tasks.
     */
    public void stop() {
        mClient.stop();
        mKaaStarted = false;
    }

    public boolean isKaaStarted() {
        return mKaaStarted;
    }

    public int getSentLogCount() {
        return mSentLogCount;
    }

    public long getLastLogTime() {
        return mLastLogTime;
    }

    public int getCollectedLogCount() {
        return mCollectedLogCount;
    }

    @Override
    public void onTimeout(LogFailoverCommand logFailoverCommand) {
        CellMonitorConstants.LOG.error("Unable to send logs within defined timeout!");
    }

    @Override
    public void onFailure(LogFailoverCommand logFailoverCommand, LogDeliveryErrorCode logDeliveryErrorCode) {
        CellMonitorConstants.LOG.error("Unable to send logs, error code: " + logDeliveryErrorCode);
        logFailoverCommand.retryLogUpload(10);
    }

    @Override
    public LogUploadStrategyDecision isUploadNeeded(LogStorageStatus logStorageStatus) {
        return logStorageStatus.getRecordCount() > 0 ? LogUploadStrategyDecision.UPLOAD :
                LogUploadStrategyDecision.NOOP;
    }

    @Override
    public int getMaxParallelUploads() {
        return MAX_PARALLEL_UPLOADS;
    }

    @Override
    public int getTimeout() {
        return TIMEOUT_PERIOD;
    }

    @Override
    public int getUploadCheckPeriod() {
        return UPLOAD_CHECK_PERIOD;
    }

    @Override
    public void onLogDeliverySuccess(BucketInfo bucketInfo) {
        CellMonitorConstants.LOG.error("Log with bucketId: " + bucketInfo.getBucketId() + " was successfully uploaded");
        mSentLogCount = bucketInfo.getBucketId() + 1;
    }

    @Override
    public void onLogDeliveryFailure(BucketInfo bucketInfo) {
        CellMonitorConstants.LOG.error("Unable to send log with bucketId " + bucketInfo.getBucketId() + " because failure");
    }

    @Override
    public void onLogDeliveryTimeout(BucketInfo bucketInfo) {
        CellMonitorConstants.LOG.error("Unable to send log with bucketId " + bucketInfo.getBucketId() + " within defined timeout");
    }
}
