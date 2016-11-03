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

package org.kaaproject.kaa.demo.verifiersdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.SignInButton;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.registration.OnAttachEndpointOperationCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.demo.verifiersdemo.entity.User;
import org.kaaproject.kaa.demo.verifiersdemo.socials.FacebookHelper;
import org.kaaproject.kaa.demo.verifiersdemo.socials.GplusHelper;
import org.kaaproject.kaa.demo.verifiersdemo.socials.TwitterHelper;
import org.kaaproject.kaa.demo.verifiersdemo.kaa.KaaManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Class extends {@link AppCompatActivity}.
 * Sign in with three social network - Facebook, Google, Twitter.
 */
public class LoginActivity extends AppCompatActivity {

    public static final int EVENT_SEND_MESSAGE = 0;
    public static final int EVENT_TOAST = 1;
    public static final int EVENT_ERROR = 2;
    public static final int EVENT_UPDATE_VIEW = 3;
    public static final int EVENT_ATTACH_USER = 4;

    public enum EventStatus {
        RECEIVED, SENT
    }

    View mLoginInfoLayout;
    View mLoginButtonsLayout;

    TextView mGreetingTextView;
    TextView mIdTextView;
    TextView mInfoTextView;
    EditText mMessageEditText;
    EditText mEventMessagesEditText;

    View mEnterTokenLayout;
    EditText mEnterTokenEditText;

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT =
            new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    private TwitterHelper mTwitterHelper;
    private FacebookHelper mFacebookHelper;
    //private GplusHelper mGplusHelper;

    private KaaManager mKaaManager;

    private User mUser;

    private final Handler mEventBus = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_SEND_MESSAGE:
                    addNewEventToChatBox(EventStatus.RECEIVED, (String) msg.obj);
                    break;
                case EVENT_TOAST:
                    toast(msg.obj.toString());
                    break;
                case EVENT_ERROR:
                    toast(getString(R.string.login_activity_error, msg.obj));
                    break;
                case EVENT_UPDATE_VIEW:
                    updateViews();
                    break;
                case EVENT_ATTACH_USER:
                    attachUser((User) msg.obj);
                    break;
                default:
                    return false;
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initHelpers();

        setContentView(R.layout.activity_login);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLoginInfoLayout = findViewById(R.id.login_info);
        mLoginButtonsLayout = findViewById(R.id.login_buttons);

        mGreetingTextView = (TextView) findViewById(R.id.greeting);
        mIdTextView = (TextView) findViewById(R.id.id_text);
        mInfoTextView = (TextView) findViewById(R.id.info_text);
        mMessageEditText = (EditText) findViewById(R.id.enter_event_edit_text);
        mEventMessagesEditText = (EditText) findViewById(R.id.event_messages);

        mEnterTokenLayout = findViewById(R.id.enter_token_container);
        mEnterTokenEditText = (EditText) findViewById(R.id.enter_token_edit_text);

        mKaaManager = new KaaManager(this, mEventBus);
        mKaaManager.start();

        /**
         * Twitter login button
         */
        final TwitterLoginButton twitterButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        mTwitterHelper.initSignInButton(twitterButton);

        /**
         * Google Plus login button
         */
        //final SignInButton googleButton = (SignInButton) findViewById(R.id.gplus_sign_in_button);
        //mGplusHelper.initSignInButton(googleButton);

        /**
         * Facebook login button
         */
        final LoginButton facebookButton = (LoginButton) findViewById(R.id.facebook_login_button);
        mFacebookHelper.initSignInButton(facebookButton);

        findViewById(R.id.send_event_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final String message = mMessageEditText.getText().toString();
                addNewEventToChatBox(EventStatus.SENT, message);
                if (message.length() > 0) {
                    mKaaManager.sendEventToAll(message);
                }
            }
        });

        findViewById(R.id.enter_token_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mKaaManager.attachAccessToken(mEnterTokenEditText.getText().toString(),
                        new OnAttachEndpointOperationCallback() {
                            @Override
                            public void onAttach(SyncResponseResultType syncResponseResultType,
                                                 EndpointKeyHash endpointKeyHash) {
                                toast(getString(R.string.login_activity_attach_access_token,
                                        syncResponseResultType,
                                        endpointKeyHash));
                            }
                        });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mTwitterHelper.onActivityResult(requestCode, resultCode, data);
        mFacebookHelper.onActivityResult(requestCode, resultCode, data);
        //mGplusHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateViews();
        mKaaManager.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mKaaManager.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mKaaManager.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logins, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_logout:
                if (mUser != null) {
                    logout(mUser.getType());
                }
                updateViews();
                break;
            default:
                return false;
        }
        return true;
    }

    private void logout(UserVerifierApp.AccountType type) {
        switch (type) {
            case GOOGLE:
                //mGplusHelper.logout();
                break;
            case FACEBOOK:
                mFacebookHelper.logout();
                break;
            case TWITTER:
                mTwitterHelper.logout();
                break;
        }

        mKaaManager.detachEndpoint();
        mUser = null;
    }

    private void attachUser(User user) {
        mUser = user;
        mKaaManager.attachUser(user);
    }

    private void updateViews() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mUser == null) {
                    mLoginInfoLayout.setVisibility(View.INVISIBLE);
                    mLoginButtonsLayout.setVisibility(View.VISIBLE);
                    return;
                }

                mLoginInfoLayout.setVisibility(View.VISIBLE);
                mLoginButtonsLayout.setVisibility(View.INVISIBLE);

                mGreetingTextView.setText(getString(R.string.login_activity_greeting,
                        mUser.getType(),
                        mUser.getName()));
                mIdTextView.setText(getString(R.string.login_activity_id_text, mUser.getId()));
                mInfoTextView.setText(mUser.getCurrentInfo());
                final String eventMessagesText = mUser.getEventMessagesText();
                if (eventMessagesText != null) {
                    mEventMessagesEditText.setText(eventMessagesText);
                }
            }
        });
    }

    private void addNewEventToChatBox(final EventStatus status, final String message) {
        if (message != null && message.length() > 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mEventMessagesEditText.append(getString(R.string.login_activity_event_text,
                            SIMPLE_DATE_FORMAT.format(new Date()),
                            status, message));

                    mUser.setEventMessagesText(mEventMessagesEditText.getText().toString());
                    mMessageEditText.setText(null);
                }
            });
        }
    }

    private void toast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initHelpers() {
        //mGplusHelper = new GplusHelper(mEventBus, this);
        mFacebookHelper = new FacebookHelper(mEventBus, this);
        mTwitterHelper = new TwitterHelper(mEventBus, this);

        mTwitterHelper.init();
        //mGplusHelper.init();
        mFacebookHelper.init();
    }
}
