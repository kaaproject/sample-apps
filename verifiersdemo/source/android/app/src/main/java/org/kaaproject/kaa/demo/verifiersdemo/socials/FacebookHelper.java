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
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.kaaproject.kaa.demo.verifiersdemo.LoginActivity;
import org.kaaproject.kaa.demo.verifiersdemo.R;
import org.kaaproject.kaa.demo.verifiersdemo.UserVerifierApp;
import org.kaaproject.kaa.demo.verifiersdemo.entity.User;

/**
 * Extends {@see SocialNetworkHelper}.
 * Helper class for Facebook Sign In functionality.
 *
 * @see <a href="https://developers.facebook.com/docs/facebook-login/android/">Facebook Sign In Manual</a>
 */
public class FacebookHelper extends SocialNetworkHelper {

    private static final String READ_PERMISSIONS_PUBLIC_PROFILE = "public_profile";

    /**
     * Facebook UI helper class used for managing the login UI.
     */
    private CallbackManager mFacebookCallback;

    public FacebookHelper(Handler callback, AppCompatActivity activity) {
        super(callback, activity);
    }

    @Override
    public void init() {
        logout(); // Facebook SDK keeps logged in user inside, so logout him in init method
    }

    @Override
    public void initSignInButton(View button) {
        final LoginButton loginButton = (LoginButton) button;
        loginButton.setReadPermissions(READ_PERMISSIONS_PUBLIC_PROFILE);

        mFacebookCallback = CallbackManager.Factory.create();

        loginButton.registerCallback(mFacebookCallback, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                final AccessToken accessToken = loginResult.getAccessToken();

                final GraphRequest request = GraphRequest.newMeRequest(
                        accessToken,
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {

                                final String id;
                                final String name;
                                try {
                                    id = object.getString("id");
                                    name = object.getString("name");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    return;
                                }

                                final User user =
                                        new User(UserVerifierApp.AccountType.FACEBOOK,
                                                id,
                                                name,
                                                accessToken.getToken());

                                mEventBus.obtainMessage(
                                        LoginActivity.EVENT_ATTACH_USER, user)
                                        .sendToTarget();
                            }
                        });

                final Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                mEventBus.obtainMessage(
                        LoginActivity.EVENT_ERROR,
                        mActivity.getString(R.string.facebook_cancelled))
                        .sendToTarget();
            }

            @Override
            public void onError(FacebookException exception) {
                mEventBus.obtainMessage(
                        LoginActivity.EVENT_ERROR,
                        mActivity.getString(R.string.facebook_error, exception.getMessage()))
                        .sendToTarget();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mFacebookCallback.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void logout() {
        LoginManager.getInstance().logOut();
    }
}

