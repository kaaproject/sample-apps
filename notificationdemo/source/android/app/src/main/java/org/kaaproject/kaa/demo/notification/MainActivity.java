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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import static org.kaaproject.kaa.demo.notification.util.TopicHelper.addNotificationToTopic;

/**
 * The implementation of the {@link FragmentActivity} class.
 * Use multithreading for starting Kaa client
 * Init Notification and Topic listener, which can get information from server
 * Manage all views.
 * <p>
 * Tip: you can use Service with this AsyncTask and send all Kaa Notification in mobile notification area
 */
public class MainActivity extends AppCompatActivity implements TopicFragment.OnTopicClickedListener {

    private KaaManager manager;
    private NotificationListener notificationListener;
    private NotificationTopicListListener topicListener;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_main);

        setUpToolbar();
        showProgress();
        startKaa();
    }

    private void setUpToolbar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    }

    public void startKaa() {
        kaaTask.execute();
    }

    // Tip: you can use it from Service
    private AsyncTask<Void, Void, Void> kaaTask = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
            initNotificationListener();
            initTopicListener();

            manager = new KaaManager(MainActivity.this);
            manager.start(notificationListener, topicListener);

            syncTopics();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            hideProgress();
            addTopicsFragment();
        }
    };

    private void syncTopics() {
        List<TopicPojo> syncedTopics = TopicHelper.syncTopics(TopicStorage.get().getTopics(), manager.getTopics());
        TopicStorage.get().setTopics(syncedTopics);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.onTerminate();
    }

    @Override
    public void onTopicClicked(int position) {
        showNotificationsFragment(position);
    }

    private void initNotificationListener() {
        notificationListener = new NotificationListener() {
            public void onNotification(final long topicId, final SecurityAlert alert) {
                Log.i(NotificationConstants.TAG, "Notification received: " + alert.toString());
                addNotification(topicId, alert);
                refreshCurrentFragment();

                if (isTopicFragment()) {
                    showNotificationDialog(topicId, alert);
                }
            }
        };
    }

    private boolean isTopicFragment() {
        return getCurrentFragmentTag().equals(NotificationConstants.TOPIC_FRAGMENT_TAG);
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentByTag(getCurrentFragmentTag());
    }

    private void refreshCurrentFragment() {
        ((OnFragmentUpdateEvent) getCurrentFragment()).onRefresh();
    }

    private void addNotification(long topicId, SecurityAlert notification) {
        List<TopicPojo> updatedTopics = addNotificationToTopic(
                topicId, TopicStorage.get().getTopics(), notification);
        TopicStorage.get().setTopics(updatedTopics);
    }

    private void initTopicListener() {
        topicListener = new NotificationTopicListListener() {
            public void onListUpdated(List<Topic> topics) {
                Log.i(NotificationConstants.TAG, "Topic list updated with topics: ");
                for (Topic topic : topics) {
                    Log.i(NotificationConstants.TAG, topic.toString());
                }

                refreshTopicFragment();
            }
        };
    }

    private void refreshTopicFragment() {
        Fragment currentFragment = getCurrentFragment();
        if (isTopicFragment() && currentFragment.isVisible()) {
            ((OnFragmentUpdateEvent) currentFragment).onRefresh();
        }
    }

    private void showNotificationDialog(long topicId, SecurityAlert notification) {
        NotificationDialogFragment dialog = NotificationDialogFragment.newInstance(
                TopicHelper.getTopicName(TopicStorage.get().getTopics(), topicId),
                notification.getAlertMessage(), notification.getAlertType().name());
        dialog.show(getSupportFragmentManager(), NotificationDialogFragment.class.getSimpleName());
    }

    private void showNotificationsFragment(int position) {
        NotificationFragment notificationFragment = new NotificationFragment();

        Bundle args = new Bundle();
        args.putInt(NotificationConstants.BUNDLE_TOPIC_ID, position);
        notificationFragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(NotificationConstants.NOTIFICATION_FRAGMENT_TAG);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        fragmentTransaction.replace(R.id.container, notificationFragment,
                NotificationConstants.NOTIFICATION_FRAGMENT_TAG).commit();
    }

    private void addTopicsFragment() {
        TopicFragment topicFragment = new TopicFragment();

        getSupportFragmentManager().beginTransaction().add(R.id.container, topicFragment,
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

    private String getCurrentFragmentTag() {
        return getSupportFragmentManager().findFragmentById(R.id.container).getTag();
    }

    public KaaManager getManager() {
        return manager;
    }

}