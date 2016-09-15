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

package org.kaaproject.kaa.demo.verifiersdemo;

import android.app.Activity;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import java.io.IOException;

public class GplusSigninListener implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = GplusSigninListener.class.getSimpleName();

    private static final int SIGN_IN_SUCCESS_RESULT_CODE = 0;
    private static final String GOOGLE_SCOPE = "oauth2:https://www.googleapis.com/auth/plus.login";

    private GoogleApiClient client;
    private LoginActivity parentActivity;
    private boolean mSignInClicked;
    private boolean mIntentInProgress;
    private ConnectionResult mConnectionResult;

    public GplusSigninListener(LoginActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public void setClient(GoogleApiClient client) {
        this.client = client;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "User is connected");
        if (mSignInClicked) {
            getTokenInBackground();
        }
        mSignInClicked = false;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection was suspended");
    }

    /*
        Called when the Google+ sign-in button is clicked.
     */
    @Override
    public void onClick(View v) {
        mSignInClicked = true;
        if (!client.isConnected() || !client.isConnecting()) {
            resolveSignInError();
            client.connect();
        }

        if (client.isConnected()) {
            getTokenInBackground();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed");
        if (!mIntentInProgress) {
            /*
                Stores ConnectionResult to use it later when the user clicks 'sign-in'.
            */
            mConnectionResult = result;

            if (mSignInClicked) {
                /*
                    Attempts to resolve all errors until the user is signed in or the user cancels.
                 */
                mSignInClicked = false;
                resolveSignInError();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode) {
        if (requestCode == SIGN_IN_SUCCESS_RESULT_CODE && mSignInClicked) {
            if (resultCode != Activity.RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!client.isConnecting()) {
                client.connect();
            }
        }
    }

    public void onStop() {
        if (client.isConnected()) {
            client.disconnect();
        }
    }

    private void resolveSignInError() {
        if (mConnectionResult != null && mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                parentActivity.startIntentSenderForResult(mConnectionResult.getResolution().getIntentSender(),
                        SIGN_IN_SUCCESS_RESULT_CODE, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                Log.i(TAG, "SendIntentException exception has occurred");
                /*
                    Switch to default state and attempts to connect using ConnectionResult
                 */
                mIntentInProgress = false;
                client.connect();
            }
        }
    }

    private void getTokenInBackground() {
        GetTokenInBackground task = new GetTokenInBackground(parentActivity);
        task.execute();
    }

    private class GetTokenInBackground extends AsyncTask<Void, Void, Void> {
        private LoginActivity activity;

        public GetTokenInBackground(LoginActivity activity) {
            this.activity = activity;
        }

        @Override
        protected Void doInBackground(Void... params) {
            mSignInClicked = false;
            try {
                /*
                    Getting the user's access token, id, name and email.
                 */
                String email = Plus.AccountApi.getAccountName(client);
                String accessToken = GoogleAuthUtil.getToken(activity, email, GOOGLE_SCOPE);
                String userId = Plus.PeopleApi.getCurrentPerson(client).getId();
                String userName = Plus.PeopleApi.getCurrentPerson(client).getName().getGivenName();

                Log.i(TAG, "Token: " +  accessToken);
                Log.i(TAG, "User id: " + userId);
                Log.i(TAG, "User name: " + userName);

                parentActivity.updateUI(userName, userId, accessToken, LoginActivity.AccountType.GOOGLE);
            } catch (IOException e) {
                Log.i(TAG, "IOException exception has occurred ", e);
            } catch (GoogleAuthException e) {
                Log.i(TAG, "GoogleAuthException exception has occurred ", e);
            }
            return null;
        }
    }
}
