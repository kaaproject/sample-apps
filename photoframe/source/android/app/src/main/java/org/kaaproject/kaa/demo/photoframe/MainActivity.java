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

package org.kaaproject.kaa.demo.photoframe;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import org.kaaproject.kaa.demo.photoframe.fragment.DevicesFragment;
import org.kaaproject.kaa.demo.photoframe.fragment.LoginFragment;
import org.kaaproject.kaa.demo.photoframe.fragment.WaitFragment;
import org.kaaproject.kaa.demo.photoframe.kaa.KaaManager;

/**
 * The implementation of the {@link AppCompatActivity} class.
 * Manages fragments transition depending on the current application state.
 */
public class MainActivity extends AppCompatActivity {

    private KaaManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_frame);

        manager = new KaaManager();
        manager.start(this);

        if (!manager.isKaaStarted()) {
            new WaitFragment().move(this);
        } else if (!manager.isUserAttached()) {
            new LoginFragment().move(this);
        } else {
            new DevicesFragment().move(this);
        }
    }

    public KaaManager getManager() {
        return manager;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        manager.stop();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setLightsOutMode(boolean enabled) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            if (enabled) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(enabled ? View.SYSTEM_UI_FLAG_FULLSCREEN : 0);
        }
    }

}
