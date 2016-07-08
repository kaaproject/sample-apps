package org.kaaproject.kaa.demo.credentials.kaa;

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.configuration.base.ConfigurationListener;
import org.kaaproject.kaa.client.configuration.base.SimpleConfigurationStorage;
import org.kaaproject.kaa.client.profile.ProfileContainer;
import org.kaaproject.kaa.demo.activation.DeviceType;
import org.kaaproject.kaa.schema.system.EmptyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Maksym Liashenko
 */
public class KaaClientManager {

    private static final Logger LOG = LoggerFactory.getLogger(KaaClientManager.class);
    private KaaClient kaaClient;

    public void start() {
        /*
         * Create the Kaa desktop context for the application.
         */
        DesktopKaaPlatformContext desktopKaaPlatformContext = new DesktopKaaPlatformContext();

        /*
         * Create a Kaa client and add a listener which displays the Kaa client
         * configuration as soon as the Kaa client is started.
         */
        kaaClient = Kaa.newClient(desktopKaaPlatformContext, new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {
                super.onStarted();
                LOG.info("Kaa client started");

                LOG.info("Endpoint ID:" + kaaClient.getEndpointKeyHash());

            }
        });
        kaaClient.setProfileContainer(new ProfileContainer() {
            @Override
            public EmptyData getProfile() {
                return new EmptyData();
            }
        });
        /*
         * Persist configuration in a local storage to avoid downloading it each
         * time the Kaa client is started.
         */
        kaaClient.setConfigurationStorage(new SimpleConfigurationStorage(desktopKaaPlatformContext, "saved_config.cfg"));

        /*
         * Start the Kaa client and connect it to the Kaa server.
         */
        kaaClient.start();
    }

    public void stop() {
        kaaClient.stop();
    }
}
