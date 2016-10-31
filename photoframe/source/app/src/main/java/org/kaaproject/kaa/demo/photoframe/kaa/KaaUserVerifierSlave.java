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

package org.kaaproject.kaa.demo.photoframe.kaa;

import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.registration.OnDetachEndpointOperationCallback;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Class, that control only user verifying feature.
 * More you can see at @see <a href="http://docs.kaaproject.org/display/KAA/Creating+custom+user+verifier">User verifier</a>
 */
final class KaaUserVerifierSlave implements UserAttachCallback, OnDetachEndpointOperationCallback {

    private final KaaManager mManager;
    private boolean mUserAttached;

    KaaUserVerifierSlave(KaaManager manager) {
        this.mManager = manager;
        mUserAttached = manager.getClient().isAttachedToUser();
    }

    /**
     * Attach the endpoint to the provided user using the default user verifier.
     */
    void login(String userExternalId, String userAccessToken) {
        mManager.getClient().attachUser(userExternalId, userAccessToken, this);
    }

    /**
     * Detach the endpoint from the user.
     */
    void logout() {
        mManager.getClient().detachEndpoint(
                new EndpointKeyHash(mManager.getClient().getEndpointKeyHash()), this);
    }

    /**
     * Check if the endpoint is already attached to the verified user.
     */
    boolean isUserAttached() {
        return mUserAttached;
    }

    /**
     * Receive the result of the endpoint attach operation.
     * Notify remote devices about availability in case of success.
     */
    @Override
    public void onAttachResult(final UserAttachResponse response) {
        final SyncResponseResultType result = response.getResult();

        final boolean successResult = result == SyncResponseResultType.SUCCESS;
        mManager.onUserAttach(successResult, response.getErrorReason());
        mUserAttached = successResult;
    }

    /**
     * Receive the result of the endpoint detach operation.
     */
    @Override
    public void onDetach(SyncResponseResultType result) {
        mUserAttached = false;
        mManager.onUserDetach(result == SyncResponseResultType.SUCCESS);
    }
}
