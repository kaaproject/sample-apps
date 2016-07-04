package org.kaaproject.kaa.demo.configuration;

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.configuration.base.ConfigurationListener;
import org.kaaproject.kaa.client.configuration.base.SimpleConfigurationStorage;
import org.kaaproject.kaa.client.profile.ProfileContainer;
import org.kaaproject.kaa.schema.sample.Configuration;
import org.kaaproject.kaa.schema.system.EmptyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * A demo application that shows how to use the Kaa configuration API.
 */
public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static KaaClient kaaClient;

    public static void main(String[] args) throws InterruptedException {
        LOG.info("Activation demo started");

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
                Configuration configuration = kaaClient.getConfiguration();
                LOG.info("Device sample period: " + (configuration.getSamplePeriod()));
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
         * Add a listener which displays the Kaa client configuration each time
         * it is updated.
         */
        kaaClient.addConfigurationListener(new ConfigurationListener() {
            @Override
            public void onConfigurationUpdate(Configuration deviceType) {
                LOG.info("Configuration was updated. New sampling period: " + deviceType.getSamplePeriod());
            }
        });

        /*
         * Start the Kaa client and connect it to the Kaa server.
         */
        kaaClient.start();
        LOG.info("Kaa endpoint key hash : " + kaaClient.getEndpointKeyHash());
        LOG.info("Default sampling period : " + kaaClient.getConfiguration().getSamplePeriod());

        LOG.info("--= Press any key to exit =--");
        readUserExit();

        /*
         * Stop the Kaa client and connect it to the Kaa server.
         */
        kaaClient.stop();
    }


    public static void readUserExit() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            br.readLine();
        } catch (IOException e) {
            LOG.error("IOException has occurred: " + e.getMessage());
        }
    }

}
