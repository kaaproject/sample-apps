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

package org.kaaproject.kaa.demo.notification;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import org.kaaproject.kaa.client.notification.NotificationListener;
import org.kaaproject.kaa.client.notification.NotificationTopicListListener;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.demo.notification.entity.TopicPojo;
import org.kaaproject.kaa.demo.notification.fragment.NotificationDialogFragment;
import org.kaaproject.kaa.demo.notification.fragment.NotificationFragment;
import org.kaaproject.kaa.demo.notification.fragment.OnFragmentUpdateEvent;
import org.kaaproject.kaa.demo.notification.fragment.TopicFragment;
import org.kaaproject.kaa.demo.notification.kaa.KaaManager;
import org.kaaproject.kaa.demo.notification.storage.TopicStorage;
import org.kaaproject.kaa.demo.notification.util.NotificationConstants;
import org.kaaproject.kaa.demo.notification.util.TopicHelper;
import org.kaaproject.kaa.schema.sample.notification.SecurityAlert;

import java.util.List;

/**
 * The implementation of the {@link FragmentActivity} class.
 * Use multithreading for starting Kaa client
 * Init Notification and Topic listener, which can get information from server
 * Manage all views.
 *
 * Tip: you can use Service with this AsyncTask and send all Kaa Notification in mobile notification area
 */
public class MainActivity extends FragmentActivity implements TopicFragment.OnTopicClickedListener {

    private KaaManager manager;
    private NotificationListener notificationListener;
    private NotificationTopicListListener topicListener;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_main);

        kaaTask.execute();
        permitPolicy();
    }

    private void permitPolicy() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public KaaManager getManager() {
        return manager;
    }

    @Override
    public void onTopicClicked(int position) {
        showNotificationsFragment(position);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //  Notify the application of the background state.
        manager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Notify the application of the foreground state.
        if (manager != null)
            manager.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();

        manager.onTerminate();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentByTag(NotificationConstants.NOTIFICATION_FRAGMENT_TAG) != null) {
            showProgress();
            showTopicsFragment();
            hideProgress();
        } else {
            super.onBackPressed();
        }
    }

    private void initNotificationListener() {
        notificationListener = new NotificationListener() {
            public void onNotification(final long topicId, final SecurityAlert alert) {
                Log.i(NotificationConstants.TAG, "Notification received: " + alert.toString());

                List<TopicPojo> updatedTopics = TopicHelper.addNotification(TopicStorage.get().getTopics(),
                        topicId, alert);
                TopicStorage.get().setTopics(updatedTopics).save(MainActivity.this);

                switch (getCurrentFragmentTag()) {
                    case NotificationConstants.TOPIC_FRAGMENT_TAG:
                        showNotificationDialog(topicId, alert);
                    case NotificationConstants.NOTIFICATION_FRAGMENT_TAG:
                        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(getCurrentFragmentTag());

                        ((OnFragmentUpdateEvent) currentFragment).onRefresh();
                        break;
                    default:
                        Log.i(NotificationConstants.TAG, "Wrong fragment!" + getCurrentFragmentTag());

                }
            }
        };
    }

    private String getCurrentFragmentTag() {
        return getSupportFragmentManager().findFragmentById(R.id.container).getTag();
    }

    private void initTopicListener() {
        topicListener = new NotificationTopicListListener() {
            public void onListUpdated(List<Topic> topics) {
                Log.i(NotificationConstants.TAG, "Topic list updated with topics: ");
                for (Topic topic : topics) {
                    Log.i(NotificationConstants.TAG, topic.toString());
                }

                TopicFragment topicFragment = (TopicFragment) getSupportFragmentManager().findFragmentByTag(NotificationConstants.TOPIC_FRAGMENT_TAG);
                if (topicFragment != null && topicFragment.isVisible()) {
                    ((OnFragmentUpdateEvent) topicFragment).onRefresh();
                }
            }
        };
    }

    private void showNotificationDialog(long topicId, SecurityAlert notification) {

        NotificationDialogFragment dialog = NotificationDialogFragment.newInstance(TopicHelper.getTopicName(TopicStorage.get().getTopics(), topicId),
                notification.getAlertMessage(), null);
        dialog.show(getSupportFragmentManager(), "fragment_alert");
    }

    private void showNotificationsFragment(int position) {
        NotificationFragment notificationFragment = new NotificationFragment();

        Bundle args = new Bundle();
        args.putInt(NotificationConstants.BUNDLE_TOPIC_ID, position);
        notificationFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction().replace(R.id.container, notificationFragment,
                NotificationConstants.NOTIFICATION_FRAGMENT_TAG).commit();
    }

    private void showTopicsFragment() {
        TopicFragment topicFragment = new TopicFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.container, topicFragment,
                NotificationConstants.TOPIC_FRAGMENT_TAG).commit();
    }

    private void showProgress() {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.container).setVisibility(View.GONE);
    }

    private void hideProgress() {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        findViewById(R.id.container).setVisibility(View.VISIBLE);
    }

    // Tip: you can use it from Service
    private AsyncTask<Void, Void, Void> kaaTask = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
            // Initialize a notification listener and add it to the Kaa client.
            initNotificationListener();

            // Initialize a topicList listener and add it to the Kaa client.
            initTopicListener();

            manager = new KaaManager(MainActivity.this);
            manager.start(notificationListener, topicListener);

            List<TopicPojo> buff = TopicHelper.sync(TopicStorage.get().getTopics(), manager.getTopics());

            TopicStorage.get()
                    .setTopics(buff)
                    .save(MainActivity.this);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            hideProgress();
            showTopicsFragment();
        }
    };

}