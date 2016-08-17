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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;
import org.kaaproject.kaa.demo.photoframe.R;
import org.kaaproject.kaa.demo.photoframe.communication.Events;

/**
 * The implementation of the {@link BaseFragment} class.
 * Used to display the busy progress view or errors.
 */
public class WaitFragment extends BaseFragment {

    public WaitFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_wait, container, false);
        setupBaseViews(rootView);


        return rootView;
    }

    @Subscribe
    public void onEvent(Events.KaaStartedEvent kaaStarted) {
        if (isSuccessEvent(kaaStarted)) {
            new LoginFragment().move(getActivity());
        }
    }

    public String getTitle() {
        return getString(R.string.app_name);
    }

    @Override
    protected boolean displayHomeAsUp() {
        return false;
    }

    @Override
    public String getFragmentTag() {
        return WaitFragment.class.getSimpleName();
    }

}
