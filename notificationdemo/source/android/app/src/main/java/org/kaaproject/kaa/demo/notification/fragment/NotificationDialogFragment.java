package org.kaaproject.kaa.demo.notification.fragment;


import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.kaaproject.kaa.demo.notification.R;
import org.kaaproject.kaa.demo.notification.util.ImageCache;

/**
 */
public class NotificationDialogFragment extends DialogFragment {

    private String topicName;
    private String notificationMessage;
    private String notificationImageUrl;

    public NotificationDialogFragment() {
    }

    public static NotificationDialogFragment newInstance(String topicName, String notificationMessage, String notificationImageUrl) {
        NotificationDialogFragment frag = new NotificationDialogFragment();

        frag.topicName = topicName;
        frag.notificationMessage = notificationMessage;
        frag.notificationImageUrl = notificationImageUrl;

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
        ImageView mNotificationLogo = ((ImageView) view.findViewById(R.id.popup_image));

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

        Bitmap bitmap = ImageCache.loadBitmap(view.getContext(), notificationImageUrl);
        if (bitmap != null)
            mNotificationLogo.setImageBitmap(bitmap);

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
