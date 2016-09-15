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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageButton;

import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.FindEventListenersCallback;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;

import io.fabric.sdk.android.Fabric;

public class LoginActivity extends FragmentActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "01Y9gbsMeGPetye1w9kkNvNMi";
    private static final String TWITTER_SECRET = "g4Pwh51o7SQlhd3RL6inNF3VxixBURAJDZc494uSISF7yOyJjc";

    private static final String USER_NAME = "userName";
    private static final String USER_ID = "userId";
    private static final String USER_INFO = "userInfo";
    private static final String EVENT_MESSAGES = "eventMessages";

    private CharSequence currentUserName;
    private CharSequence currentUserId;
    private CharSequence currentUserInfo;
    private CharSequence eventMessagesText;

    private TextView greetingTextView;
    private TextView idTextView;
    private TextView infoTextView;
    private EditText messageEdit;
    private EditText eventMessagesEdit;

    public enum EventStatus {
        RECEIVED, SENT}

    private TwitterLoginButton twitterButton;
    private SignInButton googleButton;
    private LoginButton facebookButton;
    private Button sendEventButton;
    private ImageButton mockFbButton;
    private ImageButton mockGplusButton;
    private ImageButton mockTwitterButton;
    private boolean sendingEventsEnabled;

    private GplusSigninListener gplusSigninListener;

    /*
       Google API client used to establish connection with Google
     */
    private GoogleApiClient mGoogleApiClient;

    /*
        Facebook UI helper class used for managing the login UI.
     */
    private UiLifecycleHelper uiHelper;

    private VerifiersDemoEventClassFamily vdecf;

    /*
        Defines Kaa event listener
     */
    private KaaEventListener listener;

    public enum AccountType {GOOGLE, FACEBOOK, TWITTER};

    /*
        Kaa verifiers tokens for Google, Facebook and Twitter
     */
    private KaaVerifiersTokens verifiersTokens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logins);

        greetingTextView = (TextView) findViewById(R.id.greeting);
        idTextView = (TextView) findViewById(R.id.idText);
        infoTextView = (TextView) findViewById(R.id.infoText);
        sendEventButton = (Button) findViewById(R.id.sendEventButton);
        mockFbButton = (ImageButton) findViewById(R.id.mockFbButton);
        mockGplusButton = (ImageButton) findViewById(R.id.mockGplusButton);
        mockTwitterButton = (ImageButton) findViewById(R.id.mockTwitterButton);
        messageEdit = (EditText) findViewById(R.id.msgBox);
        eventMessagesEdit = (EditText) findViewById(R.id.eventMessages);

        sendEventButton.setEnabled(sendingEventsEnabled);
        mockTwitterButton.setOnClickListener(mClickListener);
        mockGplusButton.setOnClickListener(mClickListener);
        mockFbButton.setOnClickListener(mClickListener);

        if (savedInstanceState != null) {
            currentUserName = savedInstanceState.getCharSequence(USER_NAME);
            currentUserId = savedInstanceState.getCharSequence(USER_ID);
            currentUserInfo = savedInstanceState.getCharSequence(USER_INFO);
            eventMessagesText = savedInstanceState.getCharSequence(EVENT_MESSAGES);
            updateViews();
        }

        /*
            Creating a Twitter authConfig for Twitter credentials verification.
         */
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);

        /*
             Register the application using Fabric plug-in for managing Twitter apps
         */
        Fabric.with(this, new Twitter(authConfig));

        twitterButton = (TwitterLoginButton) findViewById(R.id.twitter_sign_in_button);

        twitterButton.setEnabled(true);
        TwitterSigninListener twitterSigninListener = new TwitterSigninListener(this);

        twitterButton.setCallback(twitterSigninListener);
        twitterButton.setOnClickListener(twitterSigninListener);
        twitterButton.setEnabled(true);

        /*
            Creating a listeners class for Google+.
         */
        gplusSigninListener = new GplusSigninListener(this);

        googleButton = (SignInButton) findViewById(R.id.gplus_sign_in_button);
        googleButton.setSize(SignInButton.SIZE_WIDE);
        googleButton.setOnClickListener(gplusSigninListener);

        /*
            Creating the Google API client capable of making requests for tokens, user info etc.
         */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(gplusSigninListener)
                .addOnConnectionFailedListener(gplusSigninListener)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
        gplusSigninListener.setClient(mGoogleApiClient);

        /*
            Creates listener for Facebook.
         */
        FacebookSigninListener facebookSigninListener = new FacebookSigninListener(this);
        facebookButton = (LoginButton) findViewById(R.id.facebook_sign_in_button);

        facebookButton.setUserInfoChangedCallback(facebookSigninListener);

        /*
            Creating the UI helper for managing the Facebook login UI.
         */
        uiHelper = new UiLifecycleHelper(this, facebookSigninListener);
        uiHelper.onCreate(savedInstanceState);

        sendEventButton.setOnClickListener(new SendEventButtonClickListener());

        KaaClient kaaClient = getVerifiersApplication().getKaaClient();
        verifiersTokens = kaaClient.getConfiguration();
        Log.i(TAG, "Verifiers tokens: " + verifiersTokens.toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*
            Call corresponding onActivityResult methods for Facebook, Twitter and Google.
         */
        uiHelper.onActivityResult(requestCode, resultCode, data);
        twitterButton.onActivityResult(requestCode, resultCode, data);
        gplusSigninListener.onActivityResult(requestCode, resultCode);
    }

    public VerifiersApplication getVerifiersApplication() {
        return (VerifiersApplication) getApplication();
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
        updateViews();

        /*
            Notify the application of the foreground state.
         */
        getVerifiersApplication().resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHelper.onPause();

        /*
            Notify the application of the background state.
         */
        getVerifiersApplication().pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        /*
            Save the current state of the UI (for Facebook).
         */
        uiHelper.onSaveInstanceState(bundle);
        bundle.putCharSequence(USER_NAME, greetingTextView.getText());
        bundle.putCharSequence(USER_ID, idTextView.getText());
        bundle.putCharSequence(USER_INFO, infoTextView.getText());
        bundle.putCharSequence(EVENT_MESSAGES, eventMessagesEdit.getText().toString());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        gplusSigninListener.onStop();
    }

    public void updateUI(String userName, String userId, String token, AccountType type) {
        currentUserName = type + " user name: " + userName;
        currentUserId = type + " user id: " + userId;
        String kaaVerifierToken = null;

        switch (type) {
            case GOOGLE:
                kaaVerifierToken = verifiersTokens.getGoogleKaaVerifierToken();
                Session.getActiveSession().closeAndClearTokenInformation();
                break;
            case FACEBOOK:
                kaaVerifierToken = verifiersTokens.getFacebookKaaVerifierToken();
                break;
            case TWITTER:
                kaaVerifierToken = verifiersTokens.getTwitterKaaVerifierToken();
                Session.getActiveSession().closeAndClearTokenInformation();
                getVerifiersApplication().resume();
                break;
        }

        currentUserInfo = "Waiting for Kaa response...";
        Log.i(TAG, currentUserInfo.toString());
        updateViews();

        Log.i(TAG, "Attaching user...");
        final KaaClient kaaClient = getVerifiersApplication().getKaaClient();
        kaaClient.attachUser(kaaVerifierToken, userId, token,
                new UserAttachCallback() {
                    @Override
                    public void onAttachResult(UserAttachResponse userAttachResponse) {
                        if (userAttachResponse.getResult() == SyncResponseResultType.SUCCESS) {
                            currentUserInfo = "Successful Kaa verification";
                            Log.i(TAG, "User was attached... " + userAttachResponse.toString());

                            sendingEventsEnabled = true;
                            updateViews();
                            EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
                            vdecf = eventFamilyFactory.getVerifiersDemoEventClassFamily();

                            List<String> FQNs = new LinkedList<>();
                            FQNs.add("org.kaaproject.kaa.demo.verifiersdemo.MessageEvent");

                            kaaClient.findEventListeners(FQNs, new FindEventListenersCallback() {
                                @Override
                                public void onRequestFailed() {
                                    Log.i(TAG, "Find event listeners request has failed");
                                }
                                @Override
                                public void onEventListenersReceived(List<String> eventListeners) {
                                    Log.i(TAG, "Event listeners received: " + eventListeners);
                                }
                            });
                            /*
                                Remove old listener to avoid duplication of messages.
                             */
                            if (listener != null) {
                                vdecf.removeListener(listener);
                            }
                            listener = new KaaEventListener();
                            vdecf.addListener(listener);
                        } else {
                            String failureString = userAttachResponse.getErrorReason() == null ?
                                    userAttachResponse.getErrorCode().toString() :
                                    userAttachResponse.getErrorReason();
                            currentUserInfo = "Kaa verification failure: " + failureString;
                            Log.i(TAG, currentUserInfo.toString());

                            sendingEventsEnabled = false;
                            updateViews();
                        }
                    }
                });
    }

    private void updateViews() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                greetingTextView.setText(currentUserName);
                idTextView.setText(currentUserId);
                infoTextView.setText(currentUserInfo);
                if (eventMessagesText != null) {
                    eventMessagesEdit.setText(eventMessagesText);
                }
                sendEventButton.setEnabled(sendingEventsEnabled);
            }
        });
    }

    private class SendEventButtonClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            String message = messageEdit.getText().toString();
            addNewEventToChatBox(EventStatus.SENT, message);
            if (message.length() > 0) {
                Log.i(TAG, "Sending event: " + message);
                vdecf.sendEventToAll(new MessageEvent(message));
            }
        }
    }

    OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.mockFbButton:
                    facebookButton.callOnClick();
                    break;

                case R.id.mockTwitterButton:
                    twitterButton.callOnClick();
                    break;

                case R.id.mockGplusButton:
                    gplusSigninListener.onClick(v);
                    break;
            }
        }
    };

    private void addNewEventToChatBox(final EventStatus status, final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("[HH:mm:ss] ");
                if (message != null && message.length() > 0) {
                    eventMessagesEdit.setText(format.format(calendar.getTime()) + " " + status +
                            ": " + message + "\n" + eventMessagesEdit.getText());
                    eventMessagesText = eventMessagesEdit.getText();
                    messageEdit.setText(null);
                }
            }
        });
    }

    /*
        Class is used to handle events
     */
    private class KaaEventListener implements VerifiersDemoEventClassFamily.Listener {
        @Override
        public void onEvent(MessageEvent messageEvent, String sourceEndpoint) {
            Log.i(TAG, "Event was received: " + messageEvent.getMessage());
            addNewEventToChatBox(EventStatus.RECEIVED, messageEvent.getMessage());
        }
    }

    /*
        Detach the endpoint from the user.
     */
    private void logout() {
        getVerifiersApplication().detachEndpoint();
    }
}
