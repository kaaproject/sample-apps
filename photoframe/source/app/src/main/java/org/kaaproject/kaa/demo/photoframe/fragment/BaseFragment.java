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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.kaaproject.kaa.demo.photoframe.MainActivity;
import org.kaaproject.kaa.demo.photoframe.R;
import org.kaaproject.kaa.demo.photoframe.communication.Events;
import org.kaaproject.kaa.demo.photoframe.kaa.KaaManager;
import org.kaaproject.kaa.demo.photoframe.util.PhotoFrameConstants;

/**
 * The implementation of the {@link Fragment} class. Used as a superclass for most application fragments.
 * Implements common fragment lifecycle functions. Stores references to common application resources.
 * Provides functions for switching between views representing busy progress, an error message, and content.
 */
public abstract class BaseFragment extends Fragment {

    protected KaaManager manager;

    private ActionBar mActionBar;

    private View mWaitLayout;
    private View mContentLayout;
    private View mErrorLayout;
    private TextView mErrorText;

    public BaseFragment() {
        super();
    }

    public static Fragment getCurrentFragment(MainActivity activity) {
        if (activity == null) {
            Log.e(PhotoFrameConstants.LOG_TAG, "Unable to get top fragment. Invalid args.");
            return null;
        }

        FragmentManager fm = activity.getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry entry = fm.
                    getBackStackEntryAt(fm.getBackStackEntryCount() - 1);
            return fm.findFragmentByTag(entry.getName());
        }
        return null;
    }

    public void move(MainActivity mActivity, BaseFragment fragment) {
        if (mActivity == null) {
            Log.e(PhotoFrameConstants.LOG_TAG, "Unable pop fragment. Invalid args.");
            return;
        }

        FragmentManager fm = mActivity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container, fragment, fragment.getFragmentTag());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(fragment.getFragmentTag());
        ft.commit();
    }

    public void move(Activity mActivity) {
        move((MainActivity) mActivity, this);
    }

    public static void popBackStack(FragmentActivity activity) {
        if (activity == null) {
            Log.e(PhotoFrameConstants.LOG_TAG, "Unable pop fragment. Invalid args.");
            return;
        }

        FragmentManager fm = activity.getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity != null) {
            mActionBar = ((MainActivity) activity).getSupportActionBar();
            manager = ((MainActivity) activity).getManager();
        }

        manager.registerEventBus(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context == null) {
            mActionBar = ((MainActivity) context).getSupportActionBar();
            manager = ((MainActivity) context).getManager();
        }

        manager.registerEventBus(this);
    }


    @Override
    public void onResume() {
        super.onResume();

        if (updateActionBar() && mActionBar != null) {
            int options = ActionBar.DISPLAY_SHOW_TITLE;
            if (displayHomeAsUp())
                options |= ActionBar.DISPLAY_HOME_AS_UP;

            mActionBar.setDisplayOptions(options, ActionBar.DISPLAY_HOME_AS_UP
                    | ActionBar.DISPLAY_SHOW_TITLE);
            mActionBar.setTitle(getTitle());

            mActionBar.setDisplayShowTitleEnabled(true);
            mActionBar.setHomeButtonEnabled(displayHomeAsUp());
        }

        manager.registerEventBus(this);
    }


    @Override
    public void onPause() {
        super.onPause();

        manager.unregisterEventBus(this);
    }


    @Subscribe
    public void onEvent(final Events.PlayAlbumEvent playAlbumEvent) {
        final Fragment fragment = BaseFragment.getCurrentFragment((MainActivity) getActivity());

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (manager.isUserAttached()) {
                    loadSlideshow(playAlbumEvent, fragment);
                } else {
                    // If you logout, but get this event
                    Toast.makeText(getActivity(), R.string.logout_interaction_event, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadSlideshow(Events.PlayAlbumEvent playAlbumEvent, Fragment fragment) {
        if (fragment != null && fragment instanceof SlideshowFragment) {
            ((SlideshowFragment) fragment).updateBucketId(playAlbumEvent.getBucketId());
        } else {
            SlideshowFragment.newInstance(playAlbumEvent.getBucketId()).move(getActivity());
        }
    }

    @Subscribe
    public void onEvent(Events.UserDetachEvent userDetachEvent) {
        if (userDetachEvent.getErrorMessage() != null) {
            Toast.makeText(getActivity(), userDetachEvent.getErrorMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        new LoginFragment().move(getActivity());
    }

    protected void setupBaseViews(View rootView) {
        mWaitLayout = rootView.findViewById(R.id.waitLayout);
        mContentLayout = rootView.findViewById(R.id.contentLayout);
        mErrorLayout = rootView.findViewById(R.id.errorLayout);
        mErrorText = (TextView) rootView.findViewById(R.id.errorText);
    }

    protected boolean isSuccessEvent(Events.BasicEvent event) {
        if (event.getErrorMessage() != null) {
            showErrorView(event.getErrorMessage());
            return false;
        }
        return true;
    }

    protected void showWaitView() {
        if (mContentLayout != null) {
            mContentLayout.setVisibility(View.GONE);
        }

        mErrorLayout.setVisibility(View.GONE);
        mWaitLayout.setVisibility(View.VISIBLE);
    }

    protected void showContentView() {
        mWaitLayout.setVisibility(View.GONE);
        mErrorLayout.setVisibility(View.GONE);

        if (mContentLayout != null) {
            mContentLayout.setVisibility(View.VISIBLE);
        }
    }

    protected void showErrorView(String error) {
        mWaitLayout.setVisibility(View.GONE);

        if (mContentLayout != null) {
            mContentLayout.setVisibility(View.GONE);
        }

        mErrorLayout.setVisibility(View.VISIBLE);
        mErrorText.setText(error);
    }

    public abstract String getTitle();

    public abstract String getFragmentTag();

    protected abstract boolean displayHomeAsUp();


    protected boolean updateActionBar() {
        return true;
    }

}
