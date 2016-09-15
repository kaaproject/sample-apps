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

import android.util.Log;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

public class FacebookSigninListener implements LoginButton.UserInfoChangedCallback, Session.StatusCallback {

    private static final String TAG = FacebookSigninListener.class.getSimpleName();

    private LoginActivity parentActivity;
    private boolean mSignInClicked;

    public FacebookSigninListener(LoginActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    /*
        Specifies a callback interface that will be called when the button's notion of the current user changes
    */
    @Override
    public void onUserInfoFetched(GraphUser user) {
        if (user != null && mSignInClicked) {
             /*
                Getting the user's access token, id and user name.
             */
            String accessToken = Session.getActiveSession().getAccessToken();
            String userId = user.getId();
            String userName = user.getFirstName();

            Log.i(TAG, "Token: " +  accessToken);
            Log.i(TAG, "User id: " + userId);
            Log.i(TAG, "User name: " + user.getFirstName());

            parentActivity.updateUI(userName, userId, accessToken, LoginActivity.AccountType.FACEBOOK);

            /*
                Disconnect the user from Facebook(to make Log out button disappear)
             */
            Session.getActiveSession().closeAndClearTokenInformation();
        }

        mSignInClicked = false;
    }

    @Override
    public void call(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            Log.i(TAG, "Logged in...");
            mSignInClicked = true;
        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
        }
    }
}
