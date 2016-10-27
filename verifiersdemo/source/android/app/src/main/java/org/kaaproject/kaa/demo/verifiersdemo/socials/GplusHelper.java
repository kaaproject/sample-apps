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
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;

import org.kaaproject.kaa.demo.verifiersdemo.LoginActivity;
import org.kaaproject.kaa.demo.verifiersdemo.R;
import org.kaaproject.kaa.demo.verifiersdemo.UserVerifierApp;
import org.kaaproject.kaa.demo.verifiersdemo.entity.User;

/**
 * Extends {@see SocialNetworkHelper}.
 * Helper class for Google Sign In functionality.
 *
 * @see <a href="https://developers.google.com/identity/sign-in/android/sign-in">Google Sign In Manual</a>
 */
public class GplusHelper extends SocialNetworkHelper implements GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 1010;

    private GoogleApiClient mGoogleApiClient;

    public GplusHelper(Handler callback, AppCompatActivity activity) {
        super(callback, activity);
    }

    /**
     * Configure sign-in to request the user's ID, email address, and basic
     * profile. ID and basic profile are included in DEFAULT_SIGN_IN.
     * Build a GoogleApiClient with access to the Google Sign-In API and the
     * options specified by mGoogleSignInOptions.
     */
    @Override
    public void init() {
        final GoogleSignInOptions googleSignInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                        .requestServerAuthCode("182711447959-2c4meei5hpnb6aol4t432ago73isvsbp.apps.googleusercontent.com")
                        .requestEmail()
                        .build();

        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                .enableAutoManage(mActivity, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();
    }

    @Override
    public void initSignInButton(View button) {
        final SignInButton signInButton = (SignInButton) button;
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.startActivityForResult(
                        Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient),
                        RC_SIGN_IN);
            }
        });
    }

    /**
     * Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            handleGoogleSignInResult(Auth.GoogleSignInApi.getSignInResultFromIntent(data));
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {

        final GoogleSignInAccount acct = result.getSignInAccount();

        if (result.isSuccess() && acct != null) {
            // Signed in successfully, show authenticated UI.
            final User user = new User(UserVerifierApp.AccountType.GOOGLE,
                    acct.getId(),
                    acct.getDisplayName(),
                    acct.getServerAuthCode());

            mEventBus.obtainMessage(
                    LoginActivity.EVENT_ATTACH_USER, user)
                    .sendToTarget();
        } else {
            mEventBus.obtainMessage(
                    LoginActivity.EVENT_ERROR, mActivity.getString(R.string.google_plus_error))
                    .sendToTarget();
        }
    }

    @Override
    public void logout() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                mEventBus.obtainMessage(LoginActivity.EVENT_TOAST,
                        mActivity.getString(R.string.google_plus_success_logout));
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mEventBus.obtainMessage(
                LoginActivity.EVENT_ERROR,
                mActivity.getString(R.string.google_plus_error_connection))
                .sendToTarget();
    }
}
