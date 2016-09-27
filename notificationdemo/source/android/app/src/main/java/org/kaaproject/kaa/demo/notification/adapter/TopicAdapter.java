/**
 * Copyright 2014-2016 CyberVision, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.demo.notification.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.kaaproject.kaa.demo.notification.R;
import org.kaaproject.kaa.demo.notification.entity.TopicPojo;

import java.util.List;

public class TopicAdapter extends ArrayAdapter<TopicPojo> {

    private Context context;
    private OnSubscribeCallback callback;

    public TopicAdapter(Context context, List<TopicPojo> topics, OnSubscribeCallback callback) {
        super(context, R.layout.item_topic, topics);

        this.context = context;
        this.callback = callback;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_topic, null);

            viewHolder = new ViewHolder();
            viewHolder.topic = (TextView) convertView.findViewById(R.id.label);
            viewHolder.notificationCount = (TextView) convertView.findViewById(R.id.notifications);
            viewHolder.checkbox = (CheckBox) convertView.findViewById(R.id.check);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        TopicPojo model = getItem(position);

        viewHolder.topic.setText(model.getTopicName());
        viewHolder.notificationCount.setText(getNotificationCountText(model));
        viewHolder.checkbox.setChecked(model.isMandatoryTopic() || model.isSelected());

        viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TopicPojo element = getItem(position);

                element.setSelected(buttonView.isChecked());

                if (element.isSelected() && !element.isMandatoryTopic()) {
                    callback.onSubscribe(element.getTopicId());
                } else {
                    callback.onUnsubscribe(element.getTopicId());
                }

                viewHolder.notificationCount.setText(getNotificationCountText(element));
            }

        });

        if (model.isMandatoryTopic()) {
            viewHolder.checkbox.setEnabled(false);
        } else {
            viewHolder.checkbox.setEnabled(true);
        }

        return convertView;
    }

    private String getNotificationCountText(TopicPojo topicPojo) {
        return topicPojo.getNotificationsCount() != 0 ? "" + topicPojo.getNotificationsCount() : "";
    }

    public interface OnSubscribeCallback {
        void onSubscribe(long topicId);

        void onUnsubscribe(long topicId);
    }

    private class ViewHolder {
        private TextView topic;
        private TextView notificationCount;
        private CheckBox checkbox;
    }

}
