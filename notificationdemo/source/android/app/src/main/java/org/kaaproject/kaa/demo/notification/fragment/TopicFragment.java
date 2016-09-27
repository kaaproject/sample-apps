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

package org.kaaproject.kaa.demo.notification.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.kaaproject.kaa.demo.notification.MainActivity;
import org.kaaproject.kaa.demo.notification.R;
import org.kaaproject.kaa.demo.notification.adapter.TopicAdapter;
import org.kaaproject.kaa.demo.notification.entity.TopicPojo;
import org.kaaproject.kaa.demo.notification.kaa.KaaManager;
import org.kaaproject.kaa.demo.notification.storage.TopicStorage;
import org.kaaproject.kaa.demo.notification.util.TopicHelper;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Show all topics. You can subscribe on topic and click to see notifications.
 * Provide timer for updating fragment.
 * https://developer.android.com/training/basics/fragments/communicating.html
 */
public class TopicFragment extends BaseListFragment implements OnFragmentUpdateEvent,
        TopicAdapter.OnSubscribeCallback {

    private OnTopicClickedListener mCallback;
    private Timer timer;
    private Snackbar snackbar;

    public TopicFragment() {
        super();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.topic_title);
        updateAdapter();
        scheduleKaaUpdates();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        setEmptyViewText(view, R.string.no_notifications);
        snackbar = Snackbar.make(view, R.string.refreshing_topics, Snackbar.LENGTH_SHORT);
        super.onViewCreated(view, savedInstanceState);
    }

    private void scheduleKaaUpdates() {
        timer = new Timer(true);
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        snackbar.show();
                        updateAdapter();
                    }
                });
            }
        }, 5000, 20000);
    }

    private void updateAdapter() {
        final List<TopicPojo> updatedTopics = getSyncedTopics();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setListAdapter(new TopicAdapter(getActivity(), updatedTopics, TopicFragment.this));
            }
        });
    }

    private List<TopicPojo> getSyncedTopics() {
        KaaManager manager = ((MainActivity) getActivity()).getManager();
        TopicStorage.get().loadTopics(getActivity());
        List<TopicPojo> updatedTopics = TopicHelper.syncTopics(TopicStorage.get().getTopics(), manager.getTopics());
        TopicStorage.get().setTopics(updatedTopics).save(getActivity());
        return updatedTopics;
    }

    @Override
    public void onSubscribe(long topicId) {
        subscribeToTopic(topicId);
        updateAdapter();
    }

    private void subscribeToTopic(long topicId) {
        ((MainActivity) getActivity()).getManager().subscribeTopic(topicId);
        TopicStorage.get().subscribe(topicId).save(getActivity());
    }

    @Override
    public void onUnsubscribe(long topicId) {
        unsubscribeFromTopic(topicId);
        updateAdapter();
    }

    private void unsubscribeFromTopic(long topicId) {
        ((MainActivity) getActivity()).getManager().unsubscribeTopic(topicId);
        TopicStorage.get().unsubsccribe(topicId).save(getActivity());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnTopicClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    // http://stackoverflow.com/questions/32083053/android-fragment-onattach-deprecated
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnTopicClickedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        timer.cancel();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mCallback.onTopicClicked(position);
        timer.cancel();
    }

    @Override
    public void onRefresh() {
        updateAdapter();
    }

    public interface OnTopicClickedListener {
        void onTopicClicked(int position);
    }

}