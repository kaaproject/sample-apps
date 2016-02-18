/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.demo.photoframe;

import org.kaaproject.kaa.demo.photoframe.event.KaaStartedEvent;
import org.kaaproject.kaa.demo.photoframe.event.PlayAlbumEvent;
import org.kaaproject.kaa.demo.photoframe.event.UserAttachEvent;
import org.kaaproject.kaa.demo.photoframe.event.UserDetachEvent;
import org.kaaproject.kaa.demo.photoframe.fragment.DevicesFragment;
import org.kaaproject.kaa.demo.photoframe.fragment.LoginFragment;
import org.kaaproject.kaa.demo.photoframe.fragment.SlideshowFragment;
import org.kaaproject.kaa.demo.photoframe.fragment.WaitFragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import org.kaaproject.kaa.demo.photoframe.util.Utils;

/**
 * The implementation of the {@link ActionBarActivity} class. 
 * Manages fragments transition depending on the current application state.
 */
public class PhotoFrameActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_frame);

        if (savedInstanceState == null) {
            if (!getPhotoFrameApplication().isKaaStarted()) {
                showWait();
            } else if (!getController().isUserAttached()) {
                showLogin();
            } else {
                showDevices();
            }
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        if (getPhotoFrameApplication().getEventBus().isRegistered(this)) {
            getPhotoFrameApplication().getEventBus().unregister(this);
        }

        /*
         * Notify the application about the background state.
         */

        getPhotoFrameApplication().pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        if (!getPhotoFrameApplication().getEventBus().isRegistered(this)) {
            getPhotoFrameApplication().getEventBus().register(this);
        }

        /*
         * Notify the application about the foreground state.
         */

        getPhotoFrameApplication().resume();
    }
    
    private void showWait() {
        WaitFragment waitFragment = new WaitFragment();
        Utils.replaceFragment(this, waitFragment, waitFragment.getFragmentTag());
    }
    
    private void showLogin() {
        LoginFragment loginFragment = new LoginFragment();
        Utils.replaceFragment(this, loginFragment, loginFragment.getFragmentTag());
    }
    
    private void showDevices() {
        DevicesFragment devicesFragment = new DevicesFragment();
        Utils.replaceFragment(this, devicesFragment, devicesFragment.getFragmentTag());
    }
    
    public void onEventMainThread(PlayAlbumEvent playAlbumEvent) {
        Fragment fragment = Utils.getTopFragment(this);
        if (fragment != null && fragment instanceof SlideshowFragment) {
            ((SlideshowFragment) fragment).updateBucketId(playAlbumEvent.getBucketId());
        } else {
            SlideshowFragment slideshowFragment = SlideshowFragment.createInstance(playAlbumEvent.getBucketId());
            Utils.addBackStackFragment(this, slideshowFragment, slideshowFragment.getFragmentTag());
        }
    }
    
    public void onEventMainThread(KaaStartedEvent kaaStarted) {
        if (kaaStarted.getErrorMessage() == null) {
            if (!getController().isUserAttached()) {
                showLogin();
            } else {
                showDevices();
            }
        }
    }
    
    public void onEventMainThread(UserDetachEvent userDetachEvent) {
        if (userDetachEvent.getErrorMessage() != null) {
            Toast.makeText(this, userDetachEvent.getErrorMessage(), Toast.LENGTH_LONG).show();
        }
        showLogin();
    }
    
    public void onEventMainThread(UserAttachEvent userAttachEvent) {
        if (userAttachEvent.getErrorMessage() != null) {
            Toast.makeText(this, userAttachEvent.getErrorMessage(), Toast.LENGTH_LONG).show();
        } else {
            showDevices();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photo_frame, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Utils.popBackStack(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public PhotoFrameApplication getPhotoFrameApplication() {
        return (PhotoFrameApplication) getApplication();
    }
    
    public PhotoFrameController getController() {
        return getPhotoFrameApplication().getController();
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
