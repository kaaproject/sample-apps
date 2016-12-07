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

package org.kaaproject.kaa.examples.gpiocontol.utils;

import android.content.Context;
import android.util.Log;

import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.event.EndpointAccessToken;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.FindEventListenersCallback;
import org.kaaproject.kaa.client.event.registration.OnAttachEndpointOperationCallback;
import org.kaaproject.kaa.client.event.registration.OnDetachEndpointOperationCallback;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.examples.gpiocontol.model.Device;
import org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoRequest;
import org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoResponse;
import org.kaaproject.kaa.examples.gpiocontrol.GpioToggleRequest;
import org.kaaproject.kaa.examples.gpiocontrol.RemoteControlECF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KaaManager {

    private static final String TAG = KaaManager.class.getSimpleName();

    private static volatile KaaManager INSTANCE = new KaaManager();

    public static KaaManager getInstance() {
        return INSTANCE;
    }

    private KaaManager() {

    }

    private KaaClient mKaaClient;

    private EventFamilyFactory mEventFamilyFactory;

    private Runnable mOnStartedCallback;

    private final List<Device> mDevices = new ArrayList<>();

    private final List<RemoteControlECF.Listener> mListeners = new ArrayList<>();

    public void init(Context context) {

        mKaaClient = Kaa.newClient(new AndroidKaaPlatformContext(context), new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {
                super.onStarted();

                Log.d(TAG, "Kaa started");

                mOnStartedCallback.run();

                Log.d(TAG, "Attaching user...");

                attachUser("userId");
            }
        }, true);

        mEventFamilyFactory = mKaaClient.getEventFamilyFactory();

        mEventFamilyFactory.getRemoteControlECF().addListener(new RemoteControlECF.Listener() {
            @Override
            public void onEvent(DeviceInfoResponse event, String source) {

                Log.d(TAG, "!!! onEvent: " + event + ", " + source);

                for (RemoteControlECF.Listener listener : mListeners) {
                    listener.onEvent(event, source);
                }
            }
        });
    }

    public void start(Runnable onStarted) {
        mOnStartedCallback = onStarted;

        mKaaClient.start();
    }

    public void attachUser(String userId) {
        mKaaClient.attachUser(userId,
                mKaaClient.getEndpointAccessToken(), new UserAttachCallback() {
                    @Override
                    public void onAttachResult(UserAttachResponse response) {
                        Log.d(TAG, "User attach result: " + response.toString());

                        switch (response.getResult()) {
                            case SUCCESS:
                                mKaaClient.findEventListeners(
                                        Collections.singletonList("org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoResponse"),
                                        new FindEventListenersCallback() {
                                            @Override
                                            public void onEventListenersReceived(List<String> eventListeners) {
                                                Log.d(TAG, "onEventListenersReceived: " + eventListeners);
                                            }

                                            @Override
                                            public void onRequestFailed() {
                                                Log.d(TAG, "onEventListenersReceived: failed");
                                            }
                                        });
                                break;
                        }
                    }
                });
    }

    public void addEventListener(RemoteControlECF.Listener callback) {
        mListeners.add(callback);
    }

    public void removeEventListener(RemoteControlECF.Listener callback) {
        mListeners.remove(callback);
    }

    public void sendDeviceInfoRequestToAll() {
        final RemoteControlECF ecf = mEventFamilyFactory.getRemoteControlECF();

        ecf.sendEventToAll(new DeviceInfoRequest());
    }

    public void attachEndpoint(String endpoint, final OnAttachEndpointOperationCallback onAttach) {
        mKaaClient.attachEndpoint(new EndpointAccessToken(endpoint), new OnAttachEndpointOperationCallback() {
            @Override
            public void onAttach(SyncResponseResultType result, EndpointKeyHash resultContext) {
                Log.d(TAG, "attachEndpoint result: " + result.toString() + ", endpoint hash:" + resultContext.toString());

                onAttach.onAttach(result, resultContext);
            }
        });
    }

    public void detachEndpoint(EndpointKeyHash endpointKeyHash,
                               OnDetachEndpointOperationCallback onDetachEndpointOperationCallback) {
        mKaaClient.detachEndpoint(endpointKeyHash, onDetachEndpointOperationCallback);
    }

    public void sendGpioToggleRequest(GpioToggleRequest gpioToggleRequest, String kaaEndpointId) {
        mEventFamilyFactory.getRemoteControlECF().sendEvent(gpioToggleRequest, kaaEndpointId);
    }

    public List<Device> getDevices() {
        return mDevices;
    }
}
