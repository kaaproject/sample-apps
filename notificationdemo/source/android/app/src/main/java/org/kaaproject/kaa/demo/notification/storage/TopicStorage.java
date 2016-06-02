package org.kaaproject.kaa.demo.notification.storage;

import android.content.Context;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.kaaproject.kaa.demo.notification.entity.TopicPojo;
import org.kaaproject.kaa.demo.notification.util.NotificationConstants;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Only for example
 */
public class TopicStorage {

    private Map<Long, TopicPojo> topics;

    private Gson gson;
    private static TopicStorage instance;

    private TopicStorage() {
        topics = new HashMap<>();
        gson = new GsonBuilder().enableComplexMapKeySerialization()
                .setPrettyPrinting().create();
    }

    public static synchronized TopicStorage get() {
        if (instance == null) {
            instance = new TopicStorage();
        }
        return instance;
    }

    public TopicStorage save(Context context) {
        Map<Long, TopicPojo> buffMap = new HashMap<>();
        buffMap.putAll(topics);

        load(context);

        if (topics == null)
            topics = new HashMap<>();
        topics.putAll(buffMap);

        String buff = gson.toJson(topics);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(NotificationConstants.PREFERENCES_TOPICS, buff).apply();

        // FOR TEST
        // Toast.makeText(context, buff, Toast.LENGTH_SHORT).show();

        return this;
    }

    public TopicStorage load(Context context) {
        Type typeOfHashMap = new TypeToken<Map<Long, TopicPojo>>() {
        }.getType();

        String json = PreferenceManager.getDefaultSharedPreferences(context).getString(NotificationConstants.PREFERENCES_TOPICS, "");
        topics = gson.fromJson(json, typeOfHashMap);

        // FOR TEST
        // Toast.makeText(context, topics.toString(), Toast.LENGTH_SHORT).show();

        return this;
    }

    public Map<Long, TopicPojo> getTopicMap() {
        return topics;
    }

    public List<TopicPojo> getTopics() {
        return new ArrayList<>(topics.values());
    }

    public TopicStorage setTopics(Map<Long, TopicPojo> topics) {
        this.topics = topics;

        return this;
    }
}
