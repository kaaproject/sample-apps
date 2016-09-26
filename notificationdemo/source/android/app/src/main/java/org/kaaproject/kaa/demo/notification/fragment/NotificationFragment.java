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

package org.kaaproject.kaa.demo.notification.fragment;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kaaproject.kaa.demo.notification.NotificationDemoActivity;
import org.kaaproject.kaa.demo.notification.TopicInfoHolder;
import org.kaaproject.kaa.demo.notification.TopicModel;
import org.kaaproject.kaa.demo.notification.adapter.NotificationArrayAdapter;
import org.kaaproject.kaa.schema.sample.notification.SecurityAlert;

import java.util.LinkedList;
import java.util.List;

public class NotificationFragment extends ListFragment {

    public NotificationFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setListAdapter(new NotificationArrayAdapter(inflater, getNotificationList()));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private List<SecurityAlert> getNotificationList() {
        Bundle bundle = ((NotificationDemoActivity) getActivity()).getFragmentData();
        if (bundle != null) {
            List<TopicModel> list = TopicInfoHolder.holder.getTopicModelList();
            if (list != null) {
                Integer position = bundle.getInt("position");
                TopicModel model = list.get(position);
                return model != null ? model.getNotifications() : new LinkedList<SecurityAlert>();
            }
        }
        return new LinkedList<>();
    }

    public String getFragmentTag() {
        return NotificationFragment.class.getSimpleName();
    }

}
