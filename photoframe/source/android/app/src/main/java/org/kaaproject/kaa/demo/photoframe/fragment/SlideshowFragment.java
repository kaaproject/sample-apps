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

package org.kaaproject.kaa.demo.photoframe.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.kaaproject.kaa.demo.photoframe.MainActivity;
import org.kaaproject.kaa.demo.photoframe.PlayStatus;
import org.kaaproject.kaa.demo.photoframe.R;
import org.kaaproject.kaa.demo.photoframe.adapter.SlideshowPageAdapter;
import org.kaaproject.kaa.demo.photoframe.communication.Events;

/**
 * The implementation of the {@link Fragment} class.
 * Represents a view pager displaying views with images from the album identified by the bucketId.
 * Handles the image view switching with the constant {@link #SLIDESHOW_INTERVAL_MS} interval.
 */
public class SlideshowFragment extends BaseFragment {

    private static final String BUCKET_ID = "bucketId";
    private static final int SLIDESHOW_INTERVAL_MS = 5000;

    private MainActivity mActivity;

    private ViewPager mViewPager;
    private SlideshowPageAdapter mSlideShowPagerAdapter;

    private Handler mSlideshowHandler = new Handler();
    private String mBucketId;


    public static SlideshowFragment newInstance(String bucketId) {
        SlideshowFragment fragment = new SlideshowFragment();

        Bundle bundle = new Bundle();
        bundle.putString(BUCKET_ID, bucketId);

        fragment.setArguments(bundle);
        return fragment;
    }

    public SlideshowFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mBucketId = getArguments().getString(BUCKET_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_slideshow, container, false);
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mSlideShowPagerAdapter = new SlideshowPageAdapter(getActivity(), mBucketId);
        mViewPager.setAdapter(mSlideShowPagerAdapter);

        Toast.makeText(getActivity(), 1 + "/" + mSlideShowPagerAdapter.getCount(), Toast.LENGTH_SHORT).show();

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = (MainActivity) activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mActivity = (MainActivity) context;
    }

    @Subscribe
    public void onEvent(Events.StopPlayEvent stopPlayEvent) {
        popBackStack(getActivity());
    }

    public void updateBucketId(String bucketId) {
        if (!mBucketId.equals(bucketId)) {
            mSlideshowHandler.removeCallbacks(mSlideshowAction);
            mBucketId = bucketId;
            mSlideShowPagerAdapter = new SlideshowPageAdapter(getActivity(), mBucketId);
            mViewPager.setAdapter(mSlideShowPagerAdapter);
            mSlideshowHandler.postDelayed(mSlideshowAction, SLIDESHOW_INTERVAL_MS);
            manager.updateStatus(PlayStatus.PLAYING, mBucketId);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mActivity.getSupportActionBar() != null) {
            mActivity.getSupportActionBar().hide();
        }

        mActivity.setLightsOutMode(true);
        mSlideshowHandler.postDelayed(mSlideshowAction, SLIDESHOW_INTERVAL_MS);
        manager.updateStatus(PlayStatus.PLAYING, mBucketId);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mActivity.getSupportActionBar() != null) {
            mActivity.getSupportActionBar().show();
        }

        mActivity.setLightsOutMode(false);
        mSlideshowHandler.removeCallbacks(mSlideshowAction);
        manager.updateStatus(PlayStatus.STOPPED, null);
    }

    @Override
    public String getTitle() {
        return "";
    }

    public String getFragmentTag() {
        return SlideshowFragment.class.getSimpleName() + mBucketId;
    }

    @Override
    protected boolean displayHomeAsUp() {
        return false;
    }


    private Runnable mSlideshowAction = new Runnable() {
        @Override
        public void run() {
            int count = mSlideShowPagerAdapter.getCount();
            int position = mViewPager.getCurrentItem();
            if (position == count - 1) {
                position = 0;
            } else {
                position++;
            }
            mViewPager.setCurrentItem(position, true);

            Toast.makeText(getActivity(), (position + 1) + "/" + count, Toast.LENGTH_SHORT).show();
            mSlideshowHandler.postDelayed(this, SLIDESHOW_INTERVAL_MS);
        }

    };
}
