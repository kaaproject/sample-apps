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

package org.kaaproject.kaa.demo.datacollection;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.kaaproject.kaa.client.configuration.base.ConfigurationListener;
import org.kaaproject.kaa.schema.sample.Configuration;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  TextView mLogsTextView;

  private final Handler mRepeatHandler = new Handler();

  private final Random mRandom = new Random();
  private final Runnable mPeriodicalLogRunnable = new Runnable() {
    @Override public void run() {
      final long timeStamp = System.currentTimeMillis();
      final double result = (0.6 * (Math.random() - 0.5) + Math.sin((2 * Math.PI * timeStamp) / 90_000)) * 25 + 15;
      final int temp = Long.valueOf(Math.round(result)).intValue();
      appendLog("Log sent with temperature: " + temp + " timestamp: " + timeStamp, Logger.DEFAULT_COLOR);
      mKaaCanManager.postDatacollection(temp, timeStamp);
    }
  };

  private final ConfigurationListener mConfigurationListener = new ConfigurationListener() {

    Runnable mCurrentTask;

    @Override public void onConfigurationUpdate(final Configuration sensorConfig) {

      if (mCurrentTask != null) {
        cancelRepeatTask(mCurrentTask);
      }

      mCurrentTask = initRepeatTask(mPeriodicalLogRunnable, TimeUnit.SECONDS.toMillis(sensorConfig.getSamplePeriod()));
    }
  };

  private final Logger mLogger = new Logger() {
    @Override public void log(final String text, final int color) {
      appendLog(text, color);
    }
  };

  private KaaManager mKaaCanManager;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mLogsTextView = (TextView) findViewById(R.id.logs);

    try {
      mKaaCanManager =
          new KaaManager(this, mConfigurationListener, mLogger);
    } catch (IOException e) {
      e.printStackTrace();

      appendLog(e.getMessage(), Color.RED);
    }

    mKaaCanManager.start();
  }

  @Override protected void onDestroy() {
    super.onDestroy();

    mKaaCanManager.stop();
  }

  Runnable initRepeatTask(final Runnable task, final long delay) {

    final Runnable repeatTask = new Runnable() {
      @Override public void run() {
        task.run();
        mRepeatHandler.postDelayed(this, delay);
      }
    };
    mRepeatHandler.postDelayed(repeatTask, delay);

    return repeatTask;
  }

  void cancelRepeatTask(final Runnable task) {
    mRepeatHandler.removeCallbacks(task);
  }

  void appendLog(final String text, @ColorInt final int color) {

    runOnUiThread(new Runnable() {
      @Override public void run() {
        Log.d(TAG, text);

        final SpannableStringBuilder builder = new SpannableStringBuilder();

        if (mLogsTextView.getText().length() != 0) {
          builder.append('\n');
        }

        builder.append('>');
        builder.append(text);

        if (color != Logger.DEFAULT_COLOR) {
          final int builderLength = builder.length();
          builder.setSpan(new ForegroundColorSpan(color), builderLength - text.length() - 1,
              builderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        mLogsTextView.append(builder);
      }
    });
  }
}
