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

package org.kaaproject.demo.notification.fragment;

import org.kaaproject.demo.notification.KaaNotificationApp;
import org.kaaproject.demo.notification.R;
import org.kaaproject.demo.notification.activity.MainActivity;
import org.kaaproject.demo.notification.entity.TopicPojo;
import org.kaaproject.demo.notification.util.TopicHelper;
import org.kaaproject.demo.notification.adapter.TopicAdapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

/**
 * https://developer.android.com/training/basics/fragments/communicating.html
 */
public class TopicFragment extends ListFragment {

    private OnTopicClickedListener mCallback;

    public TopicFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState);
        setListAdapter(new TopicAdapter(getActivity(), getTopics(), (MainActivity) getActivity()));

        return view;
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
    public void onListItemClick(ListView l, View v, int position, long id) {
        mCallback.onTopicClicked(position);
    }

    private List<TopicPojo> getTopics() {
        Map<Long, TopicPojo> topicMap = ((KaaNotificationApp) getActivity().getApplication()).getTopics();
        List<TopicPojo> topics = TopicHelper.getTopicModelList(topicMap);

        if (topics.isEmpty()) {
            Toast.makeText(getContext(), R.string.no_topics, Toast.LENGTH_SHORT).show();
        }

        return topics;
    }

    public interface OnTopicClickedListener {
        void onTopicClicked(int position);
    }
}