package org.kaaproject.kaa.demo.photoframe.kaa;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaClientPlatformContext;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.demo.photoframe.AlbumInfo;
import org.kaaproject.kaa.demo.photoframe.DeviceInfo;
import org.kaaproject.kaa.demo.photoframe.PlayInfo;
import org.kaaproject.kaa.demo.photoframe.PlayStatus;
import org.kaaproject.kaa.demo.photoframe.app.Events;
import org.kaaproject.kaa.demo.photoframe.fragment.BaseFragment;
import org.kaaproject.kaa.demo.photoframe.util.PhotoFrameConstants;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Performs initialization of the application resources including initialization of the Kaa client.
 * Handles the Kaa client lifecycle.
 * Stores a reference to the actual endpoint configuration.
 * Receives configuration updates from the Kaa cluster.
 * Manages the endpoint profile object, notifies the Kaa cluster of the profile updates.
 * <p/>
 * Implements {@link SimpleKaaClientStateListener}
 */
public class KaaManager extends SimpleKaaClientStateListener {

    private KaaClient mClient;
    private EventBus mEventBus;

    private KaaInfoSlave infoSlave;
    private KaaEventsSlave eventsSlave;
    private KaaUserVerifierSlave userVerifierSlave;

    private boolean mKaaStarted;

    public KaaManager() {
        mEventBus = EventBus.getDefault();

        infoSlave = new KaaInfoSlave();
        eventsSlave = new KaaEventsSlave(infoSlave);
    }

    public void start(Context context) {

        /*
         * Initialize the Kaa client using the Android context.
         */
        KaaClientPlatformContext kaaClientContext = new AndroidKaaPlatformContext(context);
        mClient = Kaa.newClient(kaaClientContext, this);

        infoSlave.initDeviceInfo(context);
        eventsSlave.init(mClient);
        userVerifierSlave = new KaaUserVerifierSlave(this);

         /*
          * Start the Kaa client workflow.
          */
        mClient.start();


    }

    public void login(String userExternalId, String userAccessToken) {
        userVerifierSlave.login(userExternalId, userAccessToken);
    }

    public void logout() {
        userVerifierSlave.logout();
    }


    public void updateStatus(PlayStatus playing, String mBucketId) {
        eventsSlave.updateStatus(playing, mBucketId);
    }

    public void discoverRemoteDevices() {
        eventsSlave.discoverRemoteDevices();
    }

    public void stopPlayRemoteDeviceAlbum(String mEndpointKey) {
        eventsSlave.stopPlayRemoteDeviceAlbum(mEndpointKey);
    }

    public void requestRemoteDeviceInfo(String mEndpointKey) {
        eventsSlave.requestRemoteDeviceAlbums(mEndpointKey);
        eventsSlave.requestRemoteDeviceStatus(mEndpointKey);
    }

    public void playRemoteDeviceAlbum(String mEndpointKey, String bucketId) {
        eventsSlave.playRemoteDeviceAlbum(mEndpointKey, bucketId);
    }

    public String getRemoteDeviceEndpoint(int position) {
        //DevicesMap().values().toArray()[position];
        if (infoSlave.getRemoteDevicesMap().keySet().toArray().length > position)
            return (String) infoSlave.getRemoteDevicesMap().keySet().toArray()[position];
        return null;
    }

    public DeviceInfo getRemoteDevice(int position) {
        return (DeviceInfo) infoSlave.getRemoteDevicesMap().values().toArray()[position];
    }

    public PlayInfo getRemoteDeviceStatus(String endpointKey) {
        return infoSlave.getRemoteDeviceStatus(endpointKey);
    }

    public Map<String, DeviceInfo> getRemoteDevicesMap() {
        return infoSlave.getRemoteDevicesMap();
    }

    public List<AlbumInfo> getRemoteDeviceAlbums(String mEndpointKey) {
        return infoSlave.getRemoteDeviceAlbums(mEndpointKey);
    }

    public void registerEventBus(BaseFragment fragment) {
        if (!mEventBus.isRegistered(fragment))
            mEventBus.register(fragment);
    }

    public void unregisterEventBus(BaseFragment fragment) {
        if (mEventBus.isRegistered(fragment))
            mEventBus.unregister(fragment);
    }

    public boolean isUserAttached() {
        return userVerifierSlave.isUserAttached();
    }

    /**
     * Resume the Kaa client. Restore the Kaa client workflow.
     * Resume all the Kaa client tasks.
     */
    public void resume() {
        mClient.resume();
    }

    /**
     * Suspend the Kaa client. Release all network connections and application
     * resources. Suspend all the Kaa client tasks.
     */
    public void pause() {
        mClient.pause();
    }

    /**
     * Stop the Kaa client. Release all network connections and application
     * resources. Shut down all the Kaa client tasks.
     */
    public void stop() {
        mClient.stop();

        mKaaStarted = false;
    }

    public String getRemoteDeviceModel(String mEndpointKey) {
        return infoSlave.getRemoteDevicesMap().get(mEndpointKey).getModel();
    }

    public boolean isKaaStarted() {
        return mKaaStarted;
    }

    @Override
    public void onStarted() {
        PhotoFrameConstants.LOGGER.info("Kaa client started");

        /*
         *  For showing WaitFragment
         */
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                mKaaStarted = true;
                mEventBus.post(new Events.KaaStartedEvent());
            }
        }, 5000);
    }

    @Override
    public void onResume() {
        PhotoFrameConstants.LOGGER.info("Kaa client resumed");

        if (userVerifierSlave.isUserAttached()) {
            eventsSlave.notifyRemoteDevices();
        }
    }

    protected KaaClient getClient() {
        return mClient;
    }

    protected void onUserAttach(boolean successResult, String error) {
        if (successResult) {
            eventsSlave.notifyRemoteDevices();
            mEventBus.post(new Events.UserAttachEvent());
        } else {
            mEventBus.post(new Events.UserAttachEvent(error));
        }
    }

    protected void onUserDetach(boolean successResult) {
        if (successResult) {
            mEventBus.post(new Events.UserDetachEvent());
        } else {
            mEventBus.post(new Events.UserDetachEvent("Failed to detach endpoint from user!"));
        }
    }

}