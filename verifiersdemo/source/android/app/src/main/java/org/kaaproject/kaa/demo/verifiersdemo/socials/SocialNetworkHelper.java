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

package org.kaaproject.kaa.demo.verifiersdemo.socials;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Abstract class for all social network verifier' helpers.
 */
abstract class SocialNetworkHelper {

    final Handler mEventBus;
    final AppCompatActivity mActivity;

    SocialNetworkHelper(Handler eventBus, AppCompatActivity context) {
        mEventBus = eventBus;
        mActivity = context;
    }

    /**
     * Initial functionality, what must be loaded before user sign in
     */
    public abstract void init();

    /**
     * All social networks work with their buttons
     */
    public abstract void initSignInButton(View button);

    /**
     * Method, that delegates onActivityResult to social network sdk
     */
    public abstract void onActivityResult(int requestCode, int resultCode, Intent data);

    /**
     * Logout from the social network
     */
    public abstract void logout();
}
