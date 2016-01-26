package org.kaaproject.kaa.demo.cityguide.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import org.kaaproject.kaa.demo.cityguide.R;

/**
 * A simple utility class which provides static functions for managing fragments
 */
public class FragmentUtils {

    private static final String TAG = Utils.class.getSimpleName();

    public static void addBackStackFragment(FragmentActivity activity, Fragment fragment) {
        if (activity == null) {
            Log.e(TAG, "Unable pop fragment. Invalid args.");
            return;
        }
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
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
