package org.kaaproject.kaa.demo.notification.kaa;

import android.app.Activity;
import android.util.Log;

import org.kaaproject.kaa.demo.notification.util.NotificationConstants;
import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.notification.NotificationListener;
import org.kaaproject.kaa.client.notification.NotificationTopicListListener;
import org.kaaproject.kaa.client.notification.UnavailableTopicException;
import org.kaaproject.kaa.common.endpoint.gen.Topic;

import java.util.List;

/**
 * Performs initialization of
 * application resources including initialization of the Kaa client.
 * Handles the Kaa client lifecycle.
 */
public class KaaManager {

    private KaaClient mClient;
    private Activity activity;

    public KaaManager(Activity activity) {
        this.activity = activity;
    }

    /**
     * Initialise the Kaa client with all needed information.
     */
    public void start(NotificationListener notificationListener, NotificationTopicListListener topicListListener) {
        // Initialize the Kaa client using the Android context.
        mClient = Kaa.newClient(new AndroidKaaPlatformContext(activity));

        mClient.addNotificationListener(notificationListener);
        mClient.addTopicListListener(topicListListener);

        // Start the Kaa client workflow.
        mClient.start();
    }

    public List<Topic> getTopics() {
        return mClient.getTopics();
    }

    /**
     * Subscribes the Kaa client to an optional notification topic.
     */
    public void subscribeTopic(long topicId) {
        try {
            mClient.subscribeToTopic(topicId, true);
            Log.i(NotificationConstants.TAG, "Subscribing to topic with id: " + topicId);
        } catch (UnavailableTopicException e) {
            Log.e(NotificationConstants.TAG, e.getMessage());
        }
    }

    /**
     * Unsubscribes the Kaa client from an optional notification topic.
     */
    public void unsubscribeTopic(long topicId) {
        try {
            mClient.unsubscribeFromTopic(topicId, true);
            Log.i(NotificationConstants.TAG, "Unsubscribing from topic with id: " + topicId);
        } catch (UnavailableTopicException e) {
            Log.e(NotificationConstants.TAG, e.getMessage());
        }
    }

    /**
     * Suspend the Kaa client. Release all network connections and application
     * resources. Suspend all the Kaa client tasks.
     */
    public void onPause() {
        if (mClient != null)
            mClient.pause();
    }

    /**
     * Resume the Kaa client. Restore the Kaa client workflow. Resume all the Kaa client
     * tasks.
     */
    public void onResume() {
        if (mClient != null)
            mClient.resume();
    }

    /**
     * Stop the Kaa client. Release all network connections and application
     * resources. Shut down all the Kaa client tasks.
     */
    public void onTerminate() {
        if (mClient != null)
            mClient.stop();
    }

}
