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

package org.kaaproject.kaa.demo.verifiersdemo.kaa;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaClientStateListener;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.event.EndpointAccessToken;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.FindEventListenersCallback;
import org.kaaproject.kaa.client.event.registration.OnAttachEndpointOperationCallback;
import org.kaaproject.kaa.client.event.registration.OnDetachEndpointOperationCallback;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.client.exceptions.KaaException;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.demo.verifiersdemo.LoginActivity;
import org.kaaproject.kaa.demo.verifiersdemo.R;
import org.kaaproject.kaa.demo.verifiersdemo.entity.User;
import org.kaaproject.kaa.demo.verifiersdemo.KaaVerifiersTokens;
import org.kaaproject.kaa.demo.verifiersdemo.MessageEvent;
import org.kaaproject.kaa.demo.verifiersdemo.VerifiersDemoEventClassFamily;

import java.util.Collections;
import java.util.List;

public final class KaaManager {

    private static final String EVENT_MESSAGE = "org.kaaproject.kaa.demo.verifiersdemo.MessageEvent";
    private static final String TAG = KaaManager.class.getSimpleName();

    private KaaClient mClient;

    /**
     * Kaa verifiers tokens for Google, Facebook and Twitter
     */
    private KaaVerifiersTokens mKaaVerifiersTokens;

    private VerifiersDemoEventClassFamily mVerifiersDemoEventClassFamily;

    /**
     * Defines Kaa event mKaaEventListener
     */
    private final VerifiersDemoEventClassFamily.Listener mKaaEventListener = new VerifiersDemoEventClassFamily.Listener() {
        @Override
        public void onEvent(MessageEvent messageEvent, String s) {
            mEventBus.obtainMessage(
                    LoginActivity.EVENT_SEND_MESSAGE,
                    messageEvent.getMessage()).sendToTarget();
        }
    };

    private final Context mContext;
    private final Handler mEventBus;

    /**
     * Initialize the Kaa client using params
     *
     * @param ctx      Android context
     * @param eventBus event bus
     */
    public KaaManager(Context ctx, Handler eventBus) {
        mContext = ctx;
        mEventBus = eventBus;

        mClient = Kaa.newClient(new AndroidKaaPlatformContext(mContext),
                new SimpleKaaClientStateListener() {
                    @Override
                    public void onStarted() {
                        super.onStarted();
                        mKaaVerifiersTokens = mClient.getConfiguration();

                        mVerifiersDemoEventClassFamily = mClient.getEventFamilyFactory()
                                .getVerifiersDemoEventClassFamily();

                        mVerifiersDemoEventClassFamily.addListener(mKaaEventListener);
                    }
                }, true);
    }

    /**
     * Start the Kaa client workflow.
     */
    public void start() {
        mClient.start();
    }

    public void sendEventToAll(String message) {
        mVerifiersDemoEventClassFamily.sendEventToAll(new MessageEvent(message));
    }

    public void attachUser(final User user) {
        user.setCurrentInfo(mContext.getString(R.string.kaa_manager_waiting_for_response));

        mEventBus.sendEmptyMessage(LoginActivity.EVENT_UPDATE_VIEW);

        final String kaaVerifierToken = getKaaVerifierToken(user);

        mClient.attachUser(kaaVerifierToken, user.getId(), user.getToken(), new UserAttachCallback() {
            @Override
            public void onAttachResult(UserAttachResponse userAttachResponse) {

                final SyncResponseResultType result = userAttachResponse.getResult();

                switch (result) {
                    case SUCCESS:
                        user.setCurrentInfo(mContext.getString(R.string.kaa_manager_verification_success));

                        mEventBus.sendEmptyMessage(LoginActivity.EVENT_UPDATE_VIEW);

                        mClient.findEventListeners(
                                Collections.singletonList(EVENT_MESSAGE),
                                new FindEventListenersCallback() {
                                    @Override
                                    public void onEventListenersReceived(List<String> eventListeners) {
                                        mEventBus.obtainMessage(
                                                LoginActivity.EVENT_TOAST,
                                                mContext.getString(R.string.kaa_manager_event_listeners_received, eventListeners))
                                                .sendToTarget();
                                    }

                                    @Override
                                    public void onRequestFailed() {
                                        mEventBus.obtainMessage(
                                                LoginActivity.EVENT_ERROR,
                                                mContext.getString(R.string.kaa_manager_event_listeners_failed))
                                                .sendToTarget();
                                    }
                                });
                        break;
                    case FAILURE:

                        final String currentInfo = mContext.getString(
                                R.string.kaa_manager_verification_failed,
                                userAttachResponse.getErrorCode(),
                                userAttachResponse.getErrorReason(),
                                userAttachResponse.getResult());

                        user.setCurrentInfo(currentInfo);
                        mEventBus.sendEmptyMessage(LoginActivity.EVENT_UPDATE_VIEW);
                        mEventBus.obtainMessage(
                                LoginActivity.EVENT_ERROR,
                                currentInfo).sendToTarget();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public void attachAccessToken(String token, OnAttachEndpointOperationCallback callback) {
        final EndpointAccessToken endpointAccessToken = new EndpointAccessToken(token);
        mClient.attachEndpoint(endpointAccessToken, callback);
    }

    /**
     * Detach the endpoint from the user.
     */
    public void detachEndpoint() {
        final EndpointKeyHash endpointKey = new EndpointKeyHash(mClient.getEndpointKeyHash());
        mClient.detachEndpoint(endpointKey, new OnDetachEndpointOperationCallback() {
            @Override
            public void onDetach(SyncResponseResultType syncResponseResultType) {
                mEventBus.obtainMessage(
                        LoginActivity.EVENT_TOAST,
                        mContext.getString(R.string.kaa_manager_user_detached))
                        .sendToTarget();
            }
        });
    }

  /**
     * Suspend the Kaa client. Release all network connections and application
     * resources. Suspend all the Kaa client tasks.
     */
    public void pause() {
        mClient.pause();
    }

    /**
     * Resume the Kaa client. Restore the Kaa client workflow. Resume all the Kaa client
     * tasks.
     */
    public void resume() {
        mClient.resume();
    }

    /**
     * Stop the Kaa client. Release all network connections and application
     * resources. Shut down all the Kaa client tasks.
     */
    public void stop() {
        mClient.stop();
    }

    private String getKaaVerifierToken(User user) {
        switch (user.getType()) {
            case GOOGLE:
                return mKaaVerifiersTokens.getGoogleKaaVerifierToken();
            case FACEBOOK:
                return mKaaVerifiersTokens.getFacebookKaaVerifierToken();
            case TWITTER:
                return mKaaVerifiersTokens.getTwitterKaaVerifierToken();
            default:
                return null;
        }
    }
}
