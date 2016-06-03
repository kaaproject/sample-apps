package org.kaaproject.kaa.demo.cityguide.kaa;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaClientPlatformContext;
import org.kaaproject.kaa.client.KaaClientStateListener;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.configuration.base.ConfigurationListener;
import org.kaaproject.kaa.client.exceptions.KaaException;
import org.kaaproject.kaa.client.profile.ProfileContainer;
import org.kaaproject.kaa.demo.cityguide.Area;
import org.kaaproject.kaa.demo.cityguide.AvailableArea;
import org.kaaproject.kaa.demo.cityguide.CityGuideConfig;
import org.kaaproject.kaa.demo.cityguide.event.Events;
import org.kaaproject.kaa.demo.cityguide.fragment.BaseFragment;
import org.kaaproject.kaa.demo.cityguide.profile.CityGuideProfile;
import org.kaaproject.kaa.demo.cityguide.util.GuideConstants;

import java.util.List;

/**
 * Performs initialization of the application resources including initialization of the Kaa client.
 * Handles the Kaa client lifecycle.
 * Stores a reference to the actual endpoint configuration.
 * Receives configuration updates from the Kaa cluster.
 * Manages the endpoint profile object, notifies the Kaa cluster of the profile updates.
 * <p/>
 * Implements interface {@link KaaClientStateListener}. For simpler and faster use you can
 * get {@link SimpleKaaClientStateListener}
 */
public class KaaManager implements KaaClientStateListener, ProfileContainer, ConfigurationListener {

    private KaaClient mClient;
    private EventBus mEventBus;

    private boolean mKaaStarted;

    private KaaProfilingSlave profilingSlave;
    private KaaConfigurationSlave configurationSlave;

    public KaaManager() {
        profilingSlave = new KaaProfilingSlave();
        configurationSlave = new KaaConfigurationSlave();

        mEventBus = EventBus.getDefault();
    }

    public void start(Context context) {

        /*
         * Initialize the Kaa client using the Android context.
         */
        KaaClientPlatformContext kaaClientContext = new AndroidKaaPlatformContext(context);
        mClient = Kaa.newClient(kaaClientContext, this);

        configurationSlave.createConfigurationStorage(kaaClientContext, mClient);

        /*
         * Set a configuration listener to get notified about configuration
         * updates from the Kaa cluster. Update configuration object and notify UI
         * components to start using the updated configuration.
         */
        mClient.addConfigurationListener(this);

        /*
         * Set a profile container used by the Kaa client to obtain the actual profile
         * object.
         */
        mClient.setProfileContainer(this);

        /*
         * Start the Kaa client workflow.
         */
        mClient.start();
    }

    public void registerEventBus(BaseFragment fragment) {
        if (!mEventBus.isRegistered(fragment))
            mEventBus.register(fragment);
    }

    public void unregisterEventBus(BaseFragment fragment) {
        if (mEventBus.isRegistered(fragment))
            mEventBus.unregister(fragment);
    }

    /**
     * Update the city guide profile object and
     * notify the Kaa client about the profile update.
     */
    public void updateLocation(String area, String city) {
        profilingSlave.saveInfo(area, city);
        mClient.updateProfile();
    }

    public String getArea() {
        // there can be some checks
        return profilingSlave.getArea();
    }

    public String getCity() {
        // there can be some checks
        return profilingSlave.getCity();
    }

    public List<AvailableArea> getAvailableAreas() {
        // there can be some checks
        return configurationSlave.getAvailableAreas();
    }

    public List<Area> getAreas() {
        return configurationSlave.getAreas();
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

    public boolean isKaaStarted() {
        return mKaaStarted;
    }

    @Override
    public void onStarted() {
        GuideConstants.LOGGER.info("Kaa client started");

        configurationSlave.update(mClient.getConfiguration());

        mKaaStarted = true;
        mEventBus.post(new Events.KaaStarted());
    }


    @Override
    public CityGuideProfile getProfile() {
        return profilingSlave.getProfile();
    }

    @Override
    public void onConfigurationUpdate(CityGuideConfig cityGuideConfig) {
        GuideConstants.LOGGER.info("Configuration updated!");

        configurationSlave.update(cityGuideConfig);
        mEventBus.post(new Events.ConfigurationUpdated());
    }


    // TODO: on error
    @Override
    public void onStartFailure(KaaException e) {
        GuideConstants.LOGGER.info("Kaa client startup failure", e);

    }

    @Override
    public void onPaused() {
        GuideConstants.LOGGER.info("Kaa client paused");

    }

    @Override
    public void onPauseFailure(KaaException e) {
        GuideConstants.LOGGER.info("Kaa client pause failure", e);

    }

    @Override
    public void onResume() {
        GuideConstants.LOGGER.info("Kaa client resumed");

    }

    @Override
    public void onResumeFailure(KaaException e) {
        GuideConstants.LOGGER.info("Kaa client resume failure", e);

    }

    @Override
    public void onStopped() {
        GuideConstants.LOGGER.info("Kaa client stopped");

    }

    @Override
    public void onStopFailure(KaaException e) {
        GuideConstants.LOGGER.info("Kaa client stop failure", e);

    }

}
