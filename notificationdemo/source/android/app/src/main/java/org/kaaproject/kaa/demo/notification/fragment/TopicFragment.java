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

import org.kaaproject.kaa.demo.notification.R;
import org.kaaproject.kaa.demo.notification.activity.MainActivity;
import org.kaaproject.kaa.demo.notification.entity.TopicPojo;
import org.kaaproject.kaa.demo.notification.kaa.KaaManager;
import org.kaaproject.kaa.demo.notification.storage.TopicStorage;
import org.kaaproject.kaa.demo.notification.adapter.TopicAdapter;
import org.kaaproject.kaa.demo.notification.util.TopicHelper;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * https://developer.android.com/training/basics/fragments/communicating.html
 */
public class TopicFragment extends ListFragment {

    private OnTopicClickedListener mCallback;
    private Timer timer;

    public TopicFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().getActionBar().setTitle(R.string.topic_title);

        initTopicViews();

        timer = new Timer(true);
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initTopicViews();
                        Toast.makeText(getActivity(), "Refresh", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, 5000, 10000);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void initTopicViews() {
        KaaManager manager = ((MainActivity) getActivity()).getManager();
        List<TopicPojo> buff = TopicHelper.getTopicModelList(TopicHelper.initTopics(TopicStorage.get().getTopicMap(), manager.getTopics()));
        List<TopicPojo> topics = TopicStorage.get().load(getActivity()).getTopics();

        topics.addAll(buff);

        if (topics.isEmpty()) {
            Toast.makeText(getContext(), R.string.no_topics, Toast.LENGTH_SHORT).show();
        }

        setListAdapter(new TopicAdapter(getActivity(), topics, (MainActivity) getActivity()));
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // http://stackoverflow.com/questions/32083053/android-fragment-onattach-deprecated
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

    public interface OnTopicClickedListener {
        void onTopicClicked(int position);
    }
}