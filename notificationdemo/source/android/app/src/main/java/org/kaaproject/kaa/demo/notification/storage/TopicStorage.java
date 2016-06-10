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

package org.kaaproject.kaa.demo.notification.storage;

import android.content.Context;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.kaaproject.kaa.demo.notification.entity.TopicPojo;
import org.kaaproject.kaa.demo.notification.util.NotificationConstants;
import org.kaaproject.kaa.demo.notification.util.TopicHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Save and load topic list from {@link android.content.SharedPreferences}.
 * Use {@link Gson} for format topic list as json string.
 * Change topics for subscribing/unsubscribing events.
 * For more information use {@see <a href="https://github.com/google/gson/blob/master/GsonDesignDocument.md">Gson docs</a>}
 * <p>
 * Tip: don't use Shared Preferences for storing such information. This class used only for example
 */
public class TopicStorage {

    private Set<TopicPojo> topics = new HashSet<>();

    private Gson gson;
    private static TopicStorage instance;

    private TopicStorage() {
        gson = new GsonBuilder().enableComplexMapKeySerialization()
                .setPrettyPrinting().create();
    }

    public static synchronized TopicStorage get() {
        if (instance == null) {
            instance = new TopicStorage();
        }
        return instance;
    }

    public TopicStorage subsccribe(long topicId) {
        for (TopicPojo t : topics) {
            if (t.getTopicId() == topicId) {
                t.setSelected(true);
            }
        }
        return this;
    }

    public TopicStorage unsubsccribe(long topicId) {
        for (TopicPojo t : topics) {
            if (t.getTopicId() == topicId) {
                t.setSelected(false);
            }
        }
        return this;
    }

    public TopicStorage save(Context context) {
        String buffStr = gson.toJson(topics);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(NotificationConstants.PREFERENCES_TOPICS, buffStr).apply();

        // FOR TEST
        // Toast.makeText(context, buff, Toast.LENGTH_SHORT).show();

        return this;
    }

    public TopicStorage load(Context context) {
        Type typeOfHashMap = new TypeToken<Set<TopicPojo>>() {
        }.getType();

        String json = PreferenceManager.getDefaultSharedPreferences(context).getString(NotificationConstants.PREFERENCES_TOPICS, "");
        topics = gson.fromJson(json, typeOfHashMap);

        // FOR TEST
        // Toast.makeText(context, topics.toString(), Toast.LENGTH_SHORT).show();

        return this;
    }

    public List<TopicPojo> getTopics() {
        return new ArrayList<TopicPojo>() {{
            addAll(topics);
        }};
    }

    public TopicStorage setTopics(List<TopicPojo> newTopics) {
        topics.clear();
        topics.addAll(newTopics);

        return this;
    }
}
