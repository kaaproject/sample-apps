/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.demo.activation;

import org.kaaproject.kaa.client.*;
import org.kaaproject.kaa.client.channel.IPTransportInfo;
import org.kaaproject.kaa.client.configuration.base.ConfigurationListener;
import org.kaaproject.kaa.client.configuration.base.SimpleConfigurationStorage;
import org.kaaproject.kaa.client.profile.ProfileContainer;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.demo.activation.model.DeviceState;
import org.kaaproject.kaa.demo.activation.utils.AdminClientManager;
import org.kaaproject.kaa.demo.activation.utils.Utils;
import org.kaaproject.kaa.schema.system.EmptyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A demo application that shows how to use the Kaa activation API.
 */
public class ActivationDemo {
    private static final Logger LOG = LoggerFactory.getLogger(ActivationDemo.class);

    private final static String APPLICATION_NAME = "Endpoint activation demo";

    private static KaaClient kaaClient;

    public static void main(String[] args) throws InterruptedException {
        LOG.info("Activation demo started");
        if (args.length < 1) {
            LOG.info("Invalid parameters");
            LOG.info("Possible options:");
            LOG.info(" java -jar ActivationDemo.jar client");
            LOG.info(" java -jar ActivationDemo.jar admin host [port]");
            LOG.info(" java -jar ActivationDemo.jar developer host [port]");
            return;
        }

        String mode = args[0];
        switch (mode) {
            case "developer":
                if (args.length < 2) {
                }
                if (args.length == 2) {
                    AdminClientManager.init(args[1]);
                } else if (args.length == 3) {
                    AdminClientManager.init(args[1], Integer.valueOf(args[2]));
                } else {
                    LOG.info("ip/host is not specified or address is invalid");
                    return;
                }
                useDeveloperClient();
                break;
            case "admin":
                if (args.length < 2) {
                }
                if (args.length == 2) {
                    AdminClientManager.init(args[1], AdminClientManager.UserType.TENANT_ADMIN);
                } else if (args.length == 3) {
                    AdminClientManager.init(args[1], Integer.valueOf(args[2]), AdminClientManager.UserType.TENANT_ADMIN);
                } else {
                    LOG.info("ip/host is not specified or address is invalid");
                    return;
                }
                useAdminClient();
            case "client":
                useKaaClient();
                break;
            default:
                LOG.info("Invalid parameters. Please specify 'client' or 'admin' or 'developer'");

        }
    }

    private static void useKaaClient() {
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
                DeviceType config = kaaClient.getConfiguration();
                LOG.info("Device state: " + (config.getActive() ? "active" : "inactive"));
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
            public void onConfigurationUpdate(DeviceType deviceType) {
                LOG.info("Configuration was updated. New device state: " + (deviceType.getActive() ? "active" : "inactive"));
            }
        });

        /*
         * Start the Kaa client and connect it to the Kaa server.
         */
        kaaClient.start();

        Utils.getUserInput();

        /*
         * Stop the Kaa client and connect it to the Kaa server.
         */
        kaaClient.stop();
    }

    private static void useAdminClient(){
        LOG.info("Choose action by entering corresponding number:");
        while (true){
            LOG.info("\n1. Generate and provision endpoint credentials.\n2. Revoke endpoint " +
                    "credentials\n3. Exit");
            switch(Utils.getUserInput()){
                case "1":
                    generateAndProvisionKeys();
                    break;
                case "2":
                    revokeCredentials();
                    break;
                default:
                    System.exit(0);
            }
        }
    }

    private static void useDeveloperClient() {

        Map<String, EndpointProfileDto> endpointProfiles = retrieveEndpointProfiles();
        if (endpointProfiles.isEmpty()) {
            LOG.info("There is no endpoints registered!");
            return;
        }
        printAllEndpointProfiles(endpointProfiles);



        for (;;) {
            LOG.info("Specify endpoint profile id# you want to activate/deactivate or print 'exit' to exit");
            String userInput = Utils.getUserInput();
            if (userInput.equalsIgnoreCase("exit")) {
                return;
            }
            if (endpointProfiles.size() > 0) {
                if (endpointProfiles.containsKey(userInput)) {
                    updateServerProfile(endpointProfiles.get(userInput));
                } else {
                    LOG.info("Profile index is incorrect");
                }
            }
            endpointProfiles = retrieveEndpointProfiles();
            printAllEndpointProfiles(endpointProfiles);
        }
    }

    private static void generateAndProvisionKeys(){
        AdminClientManager clientManager = AdminClientManager.instance();
        LOG.info("Going to generate and provision credentials");
        clientManager.provideCredentials(APPLICATION_NAME, clientManager.generateKeyPair().getPublic().getEncoded());
    }

    private static void revokeCredentials(){
        AdminClientManager clientManager = AdminClientManager.instance();
        LOG.info("Enter ID of credentials that needs to be revoked:");
        String credentialsId = Utils.getUserInput();
        LOG.info("Going to revoke credentials");
        clientManager.revokeCredentials(APPLICATION_NAME, credentialsId);
    }

    /**
     * Retrieve all endpoint profiles associated with activation application
     *
     * @return endpoint profiles associated with activation application
     */
    private static Map<String, EndpointProfileDto> retrieveEndpointProfiles() {
        AdminClientManager clientManager = AdminClientManager.instance();
        List<EndpointGroupDto> endpointGroups = clientManager.getEndpointGroupsByApplicationName(APPLICATION_NAME);
        return endpointGroups != null ? clientManager.getEndpointProfiles(endpointGroups) : new HashMap<>();
    }

    /**
     * Output all available endpoint profiles associated to activation
     * application
     *
     * @param endpointProfiles
     *            endpoint profiles associated to activation application
     */
    private static void printAllEndpointProfiles(Map<String, EndpointProfileDto> endpointProfiles) {
        LOG.info("Endpoint profiles: ");
        for (Map.Entry<String, EndpointProfileDto> entry : endpointProfiles.entrySet()) {
            EndpointProfileDto endpointProfile = entry.getValue();
            String endpointKeyHash = Base64.getEncoder().encodeToString(endpointProfile.getEndpointKeyHash());
            boolean isActive = DeviceState.parseJsonString(endpointProfile.getServerProfileBody());
            LOG.info("Profile id: {} endpointHash: {} device state: {}", entry.getKey(), endpointKeyHash, isActive ? "active" : "inactive");
        }
    }

    /**
     * Update the server profile object using REST API
     *
     * @param endpointProfile
     *            the endpointProfileDto object
     */
    public static void updateServerProfile(EndpointProfileDto endpointProfile) {
        LOG.info("Update server profile");
        String profileBody = endpointProfile.getServerProfileBody();
        boolean isActive = DeviceState.parseJsonString(profileBody);
        int version = endpointProfile.getServerProfileVersion();
        String endpointKeyHash = Base64.getEncoder().encodeToString(endpointProfile.getEndpointKeyHash());
        updateServerProfile(endpointKeyHash, version, !isActive);
    }

    /**
     * Update the server profile object using REST API
     *
     * @param endpointKeyHash
     *            the endpointKeyHash
     * @param profileVersion
     *            the server profile version
     * @param newState
     *            new device state
     */
    public static void updateServerProfile(String endpointKeyHash, int profileVersion, boolean newState) {
        LOG.info("Update server profile");
        AdminClientManager clientManager = AdminClientManager.instance();
        clientManager.updateServerProfile(endpointKeyHash, profileVersion, DeviceState.toJsonString(newState));
    }

    private static String getServerHost() {
        IPTransportInfo transportInfo = (IPTransportInfo) kaaClient.getChannelManager().getActiveServer(TransportType.BOOTSTRAP);
        return transportInfo.getHost();
    }

}
