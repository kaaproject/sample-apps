package org.kaaproject.kaa.demo.photoframe.kaa;

import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.registration.OnDetachEndpointOperationCallback;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;

import java.util.Timer;
import java.util.TimerTask;

/**
 */
//TODO: without logout after exit app
public class KaaUserVerifierSlave implements UserAttachCallback, OnDetachEndpointOperationCallback {

    private boolean mUserAttached;
    private KaaManager manager;

    public KaaUserVerifierSlave(KaaManager manager) {
        this.manager = manager;
        mUserAttached = manager.getClient().isAttachedToUser();
    }

    /**
     * Attach the endpoint to the provided user using the default user verifier.
     */
    public void login(String userExternalId, String userAccessToken) {
        manager.getClient().attachUser(userExternalId, userAccessToken, this);
    }

    /**
     * Detach the endpoint from the user.
     */
    public void logout() {
        EndpointKeyHash endpointKey = new EndpointKeyHash(manager.getClient().getEndpointKeyHash());
        manager.getClient().detachEndpoint(endpointKey, this);
    }

    /**
     * Check if the endpoint is already attached to the verified user.
     */
    public boolean isUserAttached() {
        return mUserAttached;
    }

    /**
     * Receive the result of the endpoint attach operation.
     * Notify remote devices about availability in case of success.
     */
    @Override
    public void onAttachResult(final UserAttachResponse response) {
        final SyncResponseResultType result = response.getResult();

        // For showing WaitFragment
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                manager.onUserAttach(result == SyncResponseResultType.SUCCESS, response.getErrorReason());
                mUserAttached = (result == SyncResponseResultType.SUCCESS);

            }
        }, 5000);
    }


    /**
     * Receive the result of the endpoint detach operation.
     */
    @Override
    public void onDetach(SyncResponseResultType result) {
        mUserAttached = false;
        manager.onUserDetach(result == SyncResponseResultType.SUCCESS);

    }


}
