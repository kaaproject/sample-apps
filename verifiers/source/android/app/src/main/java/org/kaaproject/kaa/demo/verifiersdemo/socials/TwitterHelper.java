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

package org.kaaproject.kaa.demo.verifiersdemo.socials;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.kaaproject.kaa.demo.verifiersdemo.LoginActivity;
import org.kaaproject.kaa.demo.verifiersdemo.UserVerifierApp;
import org.kaaproject.kaa.demo.verifiersdemo.entity.User;
import org.kaaproject.kaa.demo.verifiersdemo.R;

import io.fabric.sdk.android.Fabric;

/**
 * Extends {@see SocialNetworkHelper}.
 * Helper class for Twitter Sign In functionality.
 *
 * @see <a href="https://fabric.io/downloads/android">Twitter Sign In Manual</a>
 */
public class TwitterHelper extends SocialNetworkHelper {

    private TwitterLoginButton mTwitterLoginButton;

    public TwitterHelper(Handler callback, AppCompatActivity activity) {
        super(callback, activity);
    }

    /**
     * Creating a Twitter authConfig for Twitter credentials verification.
     */
    @Override
    public void init() {
        final TwitterAuthConfig authConfig = new TwitterAuthConfig(
                mActivity.getString(R.string.twitter_key),
                mActivity.getString(R.string.twitter_secret));
        Fabric.with(mActivity, new Twitter(authConfig));
    }

    @Override
    public void initSignInButton(View button) {
        mTwitterLoginButton = (TwitterLoginButton) button;

        mTwitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // The TwitterSession is also available through:
                // Twitter.getInstance().core.getSessionManager().getActiveSession()
                final TwitterSession session = result.data;

                final TwitterAuthToken authToken = session.getAuthToken();
                final User user = new User(UserVerifierApp.AccountType.TWITTER,
                        String.valueOf(session.getUserId()),
                        session.getUserName(),
                        authToken.token + " " + authToken.secret);

                mEventBus.obtainMessage(
                        LoginActivity.EVENT_ATTACH_USER, user).sendToTarget();
            }

            @Override
            public void failure(TwitterException exception) {
                mEventBus.obtainMessage(
                        LoginActivity.EVENT_ERROR,
                        mActivity.getString(R.string.twitter_error, exception.getMessage()))
                        .sendToTarget();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mTwitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void logout() {
        final TwitterSession twitterSession = TwitterCore.getInstance()
                .getSessionManager()
                .getActiveSession();
        if (twitterSession != null) {
            Twitter.getSessionManager().clearActiveSession();
            Twitter.logOut();
        }
    }
}
