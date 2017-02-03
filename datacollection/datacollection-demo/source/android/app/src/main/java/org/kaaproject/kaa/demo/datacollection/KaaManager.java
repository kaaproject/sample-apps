/*
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

package org.kaaproject.kaa.demo.datacollection;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.provider.Settings;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.configuration.base.ConfigurationListener;
import org.kaaproject.kaa.client.logging.BucketInfo;
import org.kaaproject.kaa.client.logging.RecordInfo;
import org.kaaproject.kaa.client.logging.future.RecordFuture;
import org.kaaproject.kaa.client.logging.strategies.RecordCountLogUploadStrategy;
import org.kaaproject.kaa.schema.sample.Configuration;
import org.kaaproject.kaa.schema.sample.DataCollection;

final class KaaManager {

  private static final int DEFAULT_COUNT_THRESHOLD = 1;

  private final KaaClient mKaaClient;
  private final Context mContext;

  private final Logger mLogger;

  private String mAndroidId;

  private int mPercentThreshold;

  KaaManager(Context context, final ConfigurationListener configurationListener, final Logger logger) throws IOException {
    mContext = context;
    mLogger = logger;

    // Create the Kaa desktop context for the application
    final AndroidKaaPlatformContext androidKaaPlatformContext =
        new AndroidKaaPlatformContext(mContext);

    // Create a Kaa client and add a listener which creates a log record
    // as soon as the Kaa client is started.
    mKaaClient = Kaa.newClient(androidKaaPlatformContext, new SimpleKaaClientStateListener() {
      @Override public void onStarted() {
        super.onStarted();

        final Configuration configuration = mKaaClient.getConfiguration();

        mLogger.log("Config default: samplePeriod="
            + configuration.getSamplePeriod()
            + "ms", Color.GREEN);

        configurationListener.onConfigurationUpdate(configuration);
      }
    }, true);

    mKaaClient.setLogUploadStrategy(new RecordCountLogUploadStrategy(DEFAULT_COUNT_THRESHOLD));

    mKaaClient.addConfigurationListener(new ConfigurationListener() {
      @Override public void onConfigurationUpdate(Configuration configuration) {

        mLogger.log("Config update: samplePeriod="
            + configuration.getSamplePeriod()
            + "ms", Color.GREEN);

        configurationListener.onConfigurationUpdate(configuration);
      }
    });
  }

  void start() {
    mKaaClient.start();
  }

  void postDatacollection(int temp, long timeStamp) {

    final RecordFuture future = mKaaClient.addLogRecord(new DataCollection(temp, timeStamp));

    AsyncTask.execute(new Runnable() {
      @Override public void run() {
        try {
          final RecordInfo recordInfo = future.get(); // wait for log record delivery error
          final BucketInfo bucketInfo = recordInfo.getBucketInfo();

          mLogger.log("Received log record delivery info. Bucket Id [" + String.valueOf(
              bucketInfo.getBucketId()) + "]. Record delivery time [" + String.valueOf(
              recordInfo.getRecordDeliveryTimeMs()) + " ms].", Logger.DEFAULT_COLOR);
        } catch (InterruptedException | ExecutionException e) {

          mLogger.log(
              "Exception was caught while waiting for log's delivery report: " + e.getMessage(),
              Color.RED);
        }
      }
    });
  }

  void stop() {
    mKaaClient.stop();
  }

  private String getAndroidId() {

    if (mAndroidId == null) {

      mAndroidId = "ANDROID_" + Settings.Secure.getString(mContext.getContentResolver(),
          Settings.Secure.ANDROID_ID);

      mLogger.log("Android id: " + mAndroidId, Color.GREEN);
    }

    return mAndroidId;
  }
}
