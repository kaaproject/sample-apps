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


import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import org.kaaproject.kaa.demo.notification.R;
import org.kaaproject.kaa.schema.sample.notification.AlertType;
import org.kaaproject.kaa.schema.sample.notification.SecurityAlert;

/**
 * Extends {@link DialogFragment} and show new notification, that sent from server.
 * Can be shown for multiply notification
 * Show notification message and image
 */
public class NotificationDialogFragment extends DialogFragment {

    private String topicName;
    private String notificationMessage;
    private AlertType notificationType;

    public NotificationDialogFragment() {
    }

    public static NotificationDialogFragment newInstance(String topicName, String notificationMessage, String notificationType) {
        NotificationDialogFragment frag = new NotificationDialogFragment();

        frag.topicName = topicName;
        frag.notificationMessage = notificationMessage;
        frag.notificationType = AlertType.valueOf(notificationType);

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_notification, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get field from view
        TextView mTopicName = ((TextView) view.findViewById(R.id.popup_topic));
        TextView mNotificationMessage = ((TextView) view.findViewById(R.id.popup_notification));
        TextView mNotificationLogo = ((TextView) view.findViewById(R.id.popup_type));

        Button mOkButton = (Button) view.findViewById(R.id.popup_ok);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // Set arguments to field
        mNotificationMessage.setText(notificationMessage);
        mTopicName.setText(topicName);
        mNotificationLogo.setBackgroundResource(getColor());
    }

    private int getColor() {
        switch (notificationType) {
            case CodeGreen:
                return android.R.color.holo_green_dark;
            case CodeRed:
                return android.R.color.holo_red_light;
            case CodeYellow:
                return android.R.color.background_light;
        }
        return -1;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}
