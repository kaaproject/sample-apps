/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.demo.photoframe.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import org.kaaproject.kaa.demo.photoframe.R;

/**
 * A simple utility class which provides static functions for managing fragments
 */
public class Utils {
    
    private static final String TAG = Utils.class.getSimpleName();

    public static Fragment getTopFragment(FragmentActivity activity) {
        if (activity == null) {
            Log.e(TAG, "Unable to get top fragment. Invalid args.");
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

    public static void replaceFragment(FragmentActivity activity, Fragment fragment, String tag) {
        if (activity == null) {
            Log.e(TAG, "Unable to replace fragment. Invalid args.");
            return;
        }

        FragmentManager fm = activity.getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.container, fragment, tag).commit();
    }

    public static void addBackStackFragment(FragmentActivity activity, Fragment fragment, String tag) {
        if (activity == null) {
            Log.e(TAG, "Unable pop fragment. Invalid args.");
            return;
        }
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container, fragment, tag);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(tag);
        ft.commit();
    }

    public static void popBackStack(FragmentActivity activity) {
        if (activity == null) {
            Log.e(TAG, "Unable pop fragment. Invalid args.");
            return;
        }

        FragmentManager fm = activity.getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        }
    }

}
