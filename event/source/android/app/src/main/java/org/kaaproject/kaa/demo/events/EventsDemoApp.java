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

package org.kaaproject.kaa.demo.events;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.kaaproject.kaa.demo.events.utils.KaaChatManager;

import java.io.IOException;

public class EventsDemoApp extends Application {

    // Credentials for attaching an endpoint to the user.
    private static final String USER_EXTERNAL_ID = "userid";
    private static final String USER_ACCESS_TOKEN = "token";

    private static final String PREFS_USERNAME = "prefs_username";

    private KaaChatManager mKaaChatManager;

    public static EventsDemoApp app(Context ctx) {
        return ((EventsDemoApp) ctx.getApplicationContext());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mKaaChatManager = new KaaChatManager(this);

        try {
            mKaaChatManager.start(new Runnable() {
                @Override
                public void run() {
                    // attach endpoint to user - only endpoints attached to the same user
                    // can do events exchange among themselves
                    mKaaChatManager.attachToUser(USER_EXTERNAL_ID, USER_ACCESS_TOKEN, null);
                }
            });
        } catch (IOException e) {
            Toast.makeText(
                    this,
                    getString(R.string.events_demo_app_kaa_manager_start_error, e.getMessage()),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public KaaChatManager getKaaChatManager() {
        return mKaaChatManager;
    }

    /**
     * @return returns global username for this app, stored in the SharedPrefs.
     * If null, creates default
     */
    public String username() {

        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        final String defaultUsername = "User" + String.valueOf(System.currentTimeMillis());
        if (!preferences.contains(PREFS_USERNAME)) {
            newUsername(defaultUsername);
        }

        return preferences.getString(PREFS_USERNAME, defaultUsername);
    }

    /**
     * Set new global username
     *
     * @param username
     */
    public void newUsername(String username) {
        PreferenceManager
                .getDefaultSharedPreferences(this).edit()
                .putString(PREFS_USERNAME, username).apply();
    }
}
