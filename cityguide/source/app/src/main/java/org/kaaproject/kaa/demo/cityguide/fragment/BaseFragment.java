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

package org.kaaproject.kaa.demo.cityguide.fragment;

import org.kaaproject.kaa.demo.cityguide.MainActivity;
import org.kaaproject.kaa.demo.cityguide.kaa.KaaManager;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;

/**
 * The implementation of the {@link Fragment} class. Used as a superclass for all the application fragments.
 * Implements common fragment lifecycle functions. Stores references to common application resources.
 */
public abstract class BaseFragment extends Fragment {

    protected KaaManager manager;
    protected ActionBar mActionBar;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        manager = ((MainActivity) activity).getManager();
        mActionBar = ((MainActivity) activity).getSupportActionBar();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mActionBar != null) {
            int options = ActionBar.DISPLAY_SHOW_TITLE;
            if (displayHomeAsUp())
                options |= ActionBar.DISPLAY_HOME_AS_UP;

            mActionBar.setDisplayOptions(options, ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
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

    public abstract String getTitle();

    protected abstract boolean saveInfo();

    protected abstract boolean displayHomeAsUp();


}
