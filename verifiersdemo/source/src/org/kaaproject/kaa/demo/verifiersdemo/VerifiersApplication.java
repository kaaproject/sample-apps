/*
 * Copyright 2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.demo.verifiersdemo;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.registration.OnDetachEndpointOperationCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;

public class VerifiersApplication extends Application {

    public static final String TAG = VerifiersApplication.class.getSimpleName();

    private static Context mContext;
    private KaaClient mClient;

    public static Context getContext() {
        return mContext;
    }

    public void onCreate() {
        super.onCreate();
        mContext = this;
        
        /*
        * Initialize the Kaa client using the Android context.
        */
        mClient = Kaa.newClient(new AndroidKaaPlatformContext(this), new SimpleKaaClientStateListener());
        
        /*
         * Start the Kaa client workflow.
         */
        mClient.start();
    }

    public void pause() {
        /*
         * Suspend the Kaa client. Release all network connections and application
         * resources. Suspend all the Kaa client tasks.
         */
        mClient.pause();
    }

    public void resume() {
        /*
         * Resume the Kaa client. Restore the Kaa client workflow. Resume all the Kaa client
         * tasks.
         */
        mClient.resume();
    }

    @Override
    public void onTerminate() {
        /*
         * Stop the Kaa client. Release all network connections and application
         * resources. Shut down all the Kaa client tasks.
         */

        mClient.stop();
    }

    public KaaClient getKaaClient() {
        return  mClient;
    }

    public void detachEndpoint() {
        EndpointKeyHash endpointKey = new EndpointKeyHash(mClient.getEndpointKeyHash());
        mClient.detachEndpoint(endpointKey, new OnDetachEndpointOperationCallback() {
            @Override
            public void onDetach(SyncResponseResultType syncResponseResultType) {
                Log.i(TAG, "User was detached");
            }
        });
    }

}
