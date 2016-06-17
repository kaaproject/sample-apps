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

package org.kaaproject.kaa.demo.notification.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import org.kaaproject.kaa.demo.notification.R;
import org.kaaproject.kaa.demo.notification.adapter.TopicAdapter;
import org.kaaproject.kaa.demo.notification.entity.TopicPojo;
import org.kaaproject.kaa.demo.notification.fragment.NotificationDialogFragment;
import org.kaaproject.kaa.demo.notification.fragment.NotificationFragment;
import org.kaaproject.kaa.demo.notification.fragment.TopicFragment;
import org.kaaproject.kaa.demo.notification.kaa.KaaManager;
import org.kaaproject.kaa.demo.notification.storage.TopicStorage;
import org.kaaproject.kaa.demo.notification.util.NotificationConstants;
import org.kaaproject.kaa.demo.notification.util.TopicHelper;
import org.kaaproject.kaa.client.notification.NotificationListener;
import org.kaaproject.kaa.client.notification.NotificationTopicListListener;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.schema.example.Notification;

import java.util.List;
import java.util.Map;

public class MainActivity extends FragmentActivity implements TopicFragment.OnTopicClickedListener, TopicAdapter.OnSubscribeCallback {

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


    @Override
    public void onSubscribe(long topicId) {
        manager.subscribeTopic(topicId);
    }

    @Override
    public void onUnsubscribe(long topicId) {
        manager.unsubscribeTopic(topicId);
    }

    public KaaManager getManager() {
        return manager;
    }

    private void initNotificationListener() {
        notificationListener = new NotificationListener() {
            public void onNotification(final long topicId, final Notification notification) {
                Log.i(NotificationConstants.TAG, "Notification received: " + notification.toString());

                TopicHelper.addNotification(TopicStorage.get().getTopicMap(), topicId, notification);
                TopicStorage.get().save(MainActivity.this);

                ListFragment fragment = (ListFragment) getSupportFragmentManager().findFragmentByTag(NotificationConstants.NOTIFICATION_FRAGMENT_TAG);
                if (fragment != null && fragment.isVisible()) {
                    updateAdapter((ArrayAdapter) fragment.getListAdapter());
                    return;
                }

                fragment = (ListFragment) getSupportFragmentManager().findFragmentByTag(NotificationConstants.TOPIC_FRAGMENT_TAG);
                if (fragment != null && fragment.isVisible()) {
                    showNotificationDialog(topicId, notification);
                    updateAdapter((ArrayAdapter) fragment.getListAdapter());
                }
            }
        };
    }

    private void updateAdapter(final ArrayAdapter adapter) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
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
                    updateAdapter((ArrayAdapter) topicFragment.getListAdapter());
                }
            }
        };
    }

    private void showNotificationDialog(long topicId, Notification notification) {

        NotificationDialogFragment dialog = NotificationDialogFragment.newInstance(TopicHelper.getTopicName(TopicStorage.get().getTopicMap(), topicId),
                notification.getMessage(), notification.getImage());
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


    private AsyncTask<Void, Void, Void> kaaTask = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
            // Initialize a notification listener and add it to the Kaa client.
            initNotificationListener();

            // Initialize a topicList listener and add it to the Kaa client.
            initTopicListener();

            manager = new KaaManager(MainActivity.this);
            manager.start(notificationListener, topicListener);

            Map<Long, TopicPojo> buff = TopicHelper.initTopics(TopicStorage.get().getTopicMap(), manager.getTopics());

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