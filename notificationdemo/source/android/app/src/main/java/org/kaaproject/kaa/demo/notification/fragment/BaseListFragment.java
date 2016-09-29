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
package org.kaaproject.kaa.demo.notification.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.kaaproject.kaa.demo.notification.R;

public class BaseListFragment extends ListFragment {

    private static final int LIST_VIEW_PADDING_DP = 16;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getListView().setDivider(null);
        getListView().setBackgroundColor(getResources().getColor(R.color.clouds));
        getListView().setClipToPadding(false);
        int padding = convertDpToPixel(LIST_VIEW_PADDING_DP, getActivity());
        getListView().setPadding(padding, padding, padding, padding);
        super.onViewCreated(view, savedInstanceState);
    }

    public void setEmptyViewText(View rootView, int stringId) {
        TextView emptyView = new TextView(getActivity());
        emptyView.setText(stringId);
        emptyView.setGravity(Gravity.CENTER);
        ((ViewGroup) rootView).addView(emptyView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        getListView().setEmptyView(emptyView);
    }

    public static int convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return (int) px;
    }

}
