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


package org.kaaproject.kaa.demo.photoframe.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.kaaproject.kaa.demo.photoframe.PlayStatus;
import org.kaaproject.kaa.demo.photoframe.R;
import org.kaaproject.kaa.demo.photoframe.adapter.SlideshowPageAdapter;
import org.kaaproject.kaa.demo.photoframe.communication.Events;

public class SlideshowActivity extends BaseActivity {


    private static final String BUCKET_ID = "bucketId";
    private static final int SLIDESHOW_INTERVAL_MS = 5000;

    ViewPager mViewPager;

    private SlideshowPageAdapter mSlideShowPagerAdapter;

    private Handler mSlideshowHandler = new Handler();
    private String mBucketId;

    private Runnable mSlideshowAction = new Runnable() {
        @Override
        public void run() {
            final int count = mSlideShowPagerAdapter.getCount();
            int position = mViewPager.getCurrentItem();
            if (position == count - 1) {
                position = 0;
            } else {
                position++;
            }
            mViewPager.setCurrentItem(position, true);

            toastPageNumber(position + 1, count);
            mSlideshowHandler.postDelayed(this, SLIDESHOW_INTERVAL_MS);
        }

    };

    public static void start(Context context, String bucketId) {
        context.startActivity(new Intent(context, SlideshowActivity.class).putExtra(BUCKET_ID, bucketId));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_slideshow);

        mBucketId = getIntent().getStringExtra(BUCKET_ID);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mSlideShowPagerAdapter = new SlideshowPageAdapter(this, mBucketId);
        mViewPager.setAdapter(mSlideShowPagerAdapter);

        toastPageNumber(1, mSlideShowPagerAdapter.getCount());

    }

    @Subscribe
    public void onEvent(Events.StopPlayEvent stopPlayEvent) {
        finish();
    }

    @Override
    protected void loadSlideshow(Events.PlayAlbumEvent playAlbumEvent) {
        /**
         * no call for super, just update this one
         */
        updateBucketId(playAlbumEvent.getBucketId());
    }

    public void updateBucketId(String bucketId) {
        if (!mBucketId.equals(bucketId)) {
            mSlideshowHandler.removeCallbacks(mSlideshowAction);
            mBucketId = bucketId;
            mSlideShowPagerAdapter = new SlideshowPageAdapter(this, mBucketId);
            mViewPager.setAdapter(mSlideShowPagerAdapter);
            mSlideshowHandler.postDelayed(mSlideshowAction, SLIDESHOW_INTERVAL_MS);
            getKaaManager().updateStatus(PlayStatus.PLAYING, mBucketId);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        setLightsOutMode(true);
        mSlideshowHandler.postDelayed(mSlideshowAction, SLIDESHOW_INTERVAL_MS);
        getKaaManager().updateStatus(PlayStatus.PLAYING, mBucketId);
    }

    @Override
    public void onPause() {
        super.onPause();

        setLightsOutMode(false);
        mSlideshowHandler.removeCallbacks(mSlideshowAction);
        getKaaManager().updateStatus(PlayStatus.STOPPED, null);
    }

    private void toastPageNumber(int pageNum, int ofAll) {
        Toast.makeText(this, String.valueOf(pageNum) + "/" + String.valueOf(ofAll),
                Toast.LENGTH_SHORT).show();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setLightsOutMode(boolean enabled) {
        final Window window = getWindow();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            if (enabled) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            }
        } else {
            window.getDecorView().setSystemUiVisibility(enabled ? View.SYSTEM_UI_FLAG_FULLSCREEN : 0);
        }
    }
}
