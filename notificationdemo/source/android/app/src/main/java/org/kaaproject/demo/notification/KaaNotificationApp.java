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

package org.kaaproject.demo.notification;

import android.app.Application;

import org.kaaproject.demo.notification.entity.TopicPojo;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the base {@link Application} class.
 */
public class KaaNotificationApp extends Application {


    private static Map<Long, TopicPojo> topics = new HashMap<>();

    public static Map<Long, TopicPojo> getTopics() {
        return topics;
    }

    public static void setTopics(Map<Long, TopicPojo> topics) {
        KaaNotificationApp.topics = topics;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

}
