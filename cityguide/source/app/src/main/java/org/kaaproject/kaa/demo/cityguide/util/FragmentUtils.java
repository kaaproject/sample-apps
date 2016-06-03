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

package org.kaaproject.kaa.demo.cityguide.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import org.kaaproject.kaa.demo.cityguide.R;
import org.kaaproject.kaa.demo.cityguide.fragment.BaseFragment;

/**
 * A simple utility class which provides static functions for managing fragments
 */
public class FragmentUtils {


    public static void addBackStackFragment(FragmentActivity activity, Fragment fragment, String tag) {
        if (activity == null) {
            GuideConstants.LOGGER.error("Unable pop fragment. Invalid args.");
            return;
        }

        activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, tag)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(tag)
                .commit();
    }

    public static BaseFragment getActiveFragment(FragmentActivity activity) {
        if (activity.getSupportFragmentManager().getBackStackEntryCount() == 0) {
            return null;
        }
        String tag = activity.getSupportFragmentManager().getBackStackEntryAt(
                activity.getSupportFragmentManager().getBackStackEntryCount() - 1).getName();

        return (BaseFragment) activity.getSupportFragmentManager().findFragmentByTag(tag);
    }

    public static void popBackStack(FragmentActivity activity) {
        if (activity == null) {
            GuideConstants.LOGGER.error("Unable pop fragment. Invalid args.");
            return;
        }

        FragmentManager fm = activity.getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack(getActiveFragment(activity).getTitle(), 0);
        }
    }

}
