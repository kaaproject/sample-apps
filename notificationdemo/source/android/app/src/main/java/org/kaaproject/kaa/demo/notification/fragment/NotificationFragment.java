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

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.kaaproject.kaa.demo.notification.R;
import org.kaaproject.kaa.demo.notification.adapter.NotificationAdapter;
import org.kaaproject.kaa.demo.notification.entity.TopicPojo;
import org.kaaproject.kaa.demo.notification.storage.TopicStorage;
import org.kaaproject.kaa.demo.notification.util.NotificationConstants;
import org.kaaproject.kaa.schema.example.Notification;

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
        getActivity().getActionBar().setTitle(R.string.notification_title);
        setListAdapter(new NotificationAdapter(getActivity(), getNotificationList()));

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private List<Notification> getNotificationList() {
        int topicPosition = getArguments().getInt(NotificationConstants.BUNDLE_TOPIC_ID);

        TopicPojo model = TopicStorage.get()
                .load(getContext())
                .getTopics()
                .get(topicPosition);

        if (model != null) {
            if (!model.getNotifications().isEmpty()) {
                return model.getNotifications();
            } else {
                Toast.makeText(getActivity(), R.string.no_notifications, Toast.LENGTH_SHORT).show();
            }
        }
        return new LinkedList<>();
    }

}
