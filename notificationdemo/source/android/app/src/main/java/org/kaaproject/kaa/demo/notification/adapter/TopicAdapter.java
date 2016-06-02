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

package org.kaaproject.kaa.demo.notification.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.kaaproject.kaa.demo.notification.R;
import org.kaaproject.kaa.demo.notification.entity.TopicPojo;

public class TopicAdapter extends ArrayAdapter<TopicPojo> {

    private Context context;
    private OnSubscribeCallback callback;

    public TopicAdapter(Context context, List<TopicPojo> topics, OnSubscribeCallback callback) {
        super(context, R.layout.item_topic, topics);

        this.context = context;
        this.callback = callback;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        TopicPojo model = getItem(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_topic, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.topic = (TextView) convertView.findViewById(R.id.label);
            viewHolder.notificationCount = (TextView) convertView.findViewById(R.id.notifications);
            viewHolder.checkbox = (CheckBox) convertView.findViewById(R.id.check);
            viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    TopicPojo element = getItem(position);
                    element.setSelected(buttonView.isChecked());
                    if (element.isSelected()) {
                        viewHolder.notificationCount.setText(String.valueOf(element.getNotificationsCount()));
                        if (!element.isMandatoryTopic()) {
                            if (!element.isSubscribedTo()) {
                                element.setSubscribedTo(true);
                                callback.onSubscribe(element.getTopicId());
                            }
                        }
                    } else {
                        if (!element.isMandatoryTopic()) {
                            if (element.isSubscribedTo()) {
                                element.setSubscribedTo(false);
                                callback.onUnsubscribe(element.getTopicId());
                            }
                            viewHolder.notificationCount.setText("");
                        }
                    }
                }

            });
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.topic.setText(model.getTopicName());
        viewHolder.notificationCount.setText(model.getNotificationsCount() != 0 ? "" + model.getNotificationsCount() : "");
        viewHolder.checkbox.setChecked(model.isSelected());

        if (model.isMandatoryTopic()) {
            viewHolder.checkbox.setEnabled(false);
        } else {
            viewHolder.checkbox.setEnabled(true);
        }

        return convertView;
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
