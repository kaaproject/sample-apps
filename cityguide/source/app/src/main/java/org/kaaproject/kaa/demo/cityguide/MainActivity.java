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

package org.kaaproject.kaa.demo.cityguide;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.kaaproject.kaa.demo.cityguide.fragment.AreasFragment;
import org.kaaproject.kaa.demo.cityguide.fragment.BaseFragment;
import org.kaaproject.kaa.demo.cityguide.kaa.KaaManager;
import org.kaaproject.kaa.demo.cityguide.ui.SetLocationDialog;
import org.kaaproject.kaa.demo.cityguide.ui.SetLocationDialog.SetLocationCallback;

import java.util.List;

/**
 * The implementation of the {@link AppCompatActivity} class.
 * Notifies the application of the activity lifecycle changes.
 * Implements the 'Set location' menu command to display {@link SetLocationDialog} and notify the
 * application of the new location.
 */
public class MainActivity extends AppCompatActivity implements SetLocationCallback {

    /*
     * Create wrapper on Kaa functionality.
     * In different cases you can create manager in one activity or create it in Application class
     * for accessing in different activities.
     */
    private KaaManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_guide);

        // Create global configuration and initialize ImageLoader with this config
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

        manager = new KaaManager();
        manager.start(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new AreasFragment()).commit();
        }
    }

    public KaaManager getManager() {
        return manager;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.city_guide, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getCurrentFragment().popBackStack(this);
                break;
            case R.id.action_set_location:
                setLocation();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setLocation() {
        SetLocationDialog dialog = new SetLocationDialog(manager, this, this);
        dialog.show();
    }

    private BaseFragment getCurrentFragment() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null || !fragments.isEmpty()) {
            for (Fragment fragment : fragments) {
                if (fragment != null && fragment.isVisible())
                    return (BaseFragment) fragment;
            }
        }
        return null;
    }

    @Override
    public void onLocationSelected(String area, String city) {
        manager.updateLocation(area, city);
    }

    @Override
    protected void onPause() {
        super.onPause();

        /*
         * Notify the application of the background state.
         */
        manager.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*
         * Notify the application of the foreground state.
         */
        manager.resume();
    }

    @Override
    protected void onStop() {
        super.onStop();

        manager.stop();
    }
}
