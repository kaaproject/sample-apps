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
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoRequest;
import org.kaaproject.kaa.examples.gpiocontrol.RemoteControlECF;

public class KaaProvider {

    private static final String LOG_TAG = KaaProvider.class.getSimpleName();

    private static volatile KaaClient kaaClient;

    public static KaaClient getClient(Context context) {
        if (kaaClient == null) {
            synchronized (KaaProvider.class) {
                if (kaaClient == null) {
                    kaaClient = Kaa.newClient(new AndroidKaaPlatformContext(context), new SimpleKaaClientStateListener(), true);
                }
            }
        }
        return kaaClient;
    }

    public static void attachUser(Context context) {
        final KaaClient client = getClient(context);

        client.attachUser(PreferencesManager.getUserExternalId(context), client.getEndpointAccessToken(), new UserAttachCallback() {
            @Override
            public void onAttachResult(UserAttachResponse response) {
                Log.d(LOG_TAG, "User attach result: " + response.toString());
            }
        });
    }

    public static void setUpEventListener(Context context, RemoteControlECF.Listener callback) {
        final KaaClient client = getClient(context);
        EventFamilyFactory eventFamilyFactory = client.getEventFamilyFactory();
        final RemoteControlECF ecf = eventFamilyFactory.getRemoteControlECF();

        ecf.addListener(callback);

        ecf.sendEventToAll(new DeviceInfoRequest());
    }

    public static void sendDeviceInfoRequestToAll(Context context) {
        final KaaClient client = getClient(context);
        EventFamilyFactory eventFamilyFactory = client.getEventFamilyFactory();
        final RemoteControlECF ecf = eventFamilyFactory.getRemoteControlECF();

        ecf.sendEventToAll(new DeviceInfoRequest());
    }

}
