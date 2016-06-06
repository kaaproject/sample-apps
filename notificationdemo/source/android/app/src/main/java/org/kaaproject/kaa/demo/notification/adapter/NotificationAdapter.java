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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.kaaproject.kaa.demo.notification.R;
import org.kaaproject.kaa.demo.notification.util.ImageCache;
import org.kaaproject.kaa.schema.example.Notification;

import java.util.List;

public class NotificationAdapter extends ArrayAdapter<Notification> {

    private Context context;

    public NotificationAdapter(Context context, List<Notification> notifications) {
        super(context, R.layout.item_notification, notifications);

        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        Notification notification = getItem(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_notification, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.message = (TextView) convertView.findViewById(R.id.notification_message);
            viewHolder.image = (ImageView) convertView.findViewById(R.id.notification_image);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.message.setText(notification.getMessage());
        viewHolder.image.setImageBitmap(ImageCache.loadBitmap(context, notification.getImage()));

        return convertView;
    }

    private class ViewHolder {
        private TextView message;
        private ImageView image;
    }


}
