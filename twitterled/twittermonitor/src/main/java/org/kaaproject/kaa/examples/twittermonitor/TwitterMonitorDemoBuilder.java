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

package org.kaaproject.kaa.examples.twittermonitor;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.kaaproject.kaa.common.dto.*;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.examples.common.AbstractDemoBuilder;
import org.kaaproject.kaa.examples.common.KaaDemoBuilder;
import org.kaaproject.kaa.examples.twitterboard.TwitterBoardDemoBuilder;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

@KaaDemoBuilder(dependsOnBuilders = TwitterBoardDemoBuilder.class)
public class TwitterMonitorDemoBuilder extends AbstractDemoBuilder {

    private static final Logger logger = LoggerFactory.getLogger(TwitterMonitorDemoBuilder.class);

    private static final String TWITTER_BOARD = "Twitter board";
    private static final String TWITTER_BOARD_NOTIFICATION_SCHEMA = "Twitter board notification schema";
    private static final String KAA_CLIENT_CONFIG = "kaaClientConfiguration";
    private static final String APP_TOKEN = "appToken";
    private static final String NF_SCHEMA_VERSION = "nfSchemaVersion";

    public TwitterMonitorDemoBuilder() {
        super("demo/twittermonitor");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {
        logger.info("Loading 'Twitter monitor application' data...");

        loginTenantAdmin(client);

        ApplicationDto twitterMonitorApplication = new ApplicationDto();
        twitterMonitorApplication.setName("Twitter monitor");
        twitterMonitorApplication = client.editApplication(twitterMonitorApplication);

        sdkProfileDto.setApplicationId(twitterMonitorApplication.getId());
        sdkProfileDto.setProfileSchemaVersion(0);
        sdkProfileDto.setNotificationSchemaVersion(1);
        sdkProfileDto.setLogSchemaVersion(1);

        loginTenantDeveloper(client);

        logger.info("Creating ctl schema...");
        CTLSchemaDto ctlSchema = client.saveCTLSchemaWithAppToken(getResourceAsString("config_schema.avsc"), twitterMonitorApplication.getTenantId(), twitterMonitorApplication.getApplicationToken());

        logger.info("Creating configuration schema...");
        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setApplicationId(twitterMonitorApplication.getId());
        configurationSchema.setName("TwitterMonitor schema");
        configurationSchema.setDescription("Default configuration schema for the twitter monitor application");
        configurationSchema.setCtlSchemaId(ctlSchema.getId());
        configurationSchema = client.saveConfigurationSchema(configurationSchema);
        logger.info("Configuration schema version: {}", configurationSchema.getVersion());
        sdkProfileDto.setConfigurationSchemaVersion(configurationSchema.getVersion());
        logger.info("Configuration schema was created.");

        EndpointGroupDto baseEndpointGroup = null;
        List<EndpointGroupDto> endpointGroups = client.getEndpointGroupsByAppToken(twitterMonitorApplication.getApplicationToken());
        if (endpointGroups.size() == 1 && endpointGroups.get(0).getWeight() == 0) {
            baseEndpointGroup = endpointGroups.get(0);
        }

        if (baseEndpointGroup == null) {
            throw new RuntimeException("Can't get default endpoint group twitter monitor application!");
        }

        ConfigurationDto baseConfiguration = new ConfigurationDto();
        baseConfiguration.setApplicationId(twitterMonitorApplication.getId());
        baseConfiguration.setEndpointGroupId(baseEndpointGroup.getId());
        baseConfiguration.setSchemaId(configurationSchema.getId());
        baseConfiguration.setSchemaVersion(configurationSchema.getVersion());
        baseConfiguration.setDescription("Base twitter monitor configuration");
        String body = FileUtils.readResource(getResourcePath("config_data.json"));
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> configBody = objectMapper.readValue(body, Map.class);
        logger.info("Getting config body: [{}]", configBody);
        Map<String, Object> kaaClientConfig = (Map<String, Object>) configBody.get(KAA_CLIENT_CONFIG);
        logger.info("Getting kaaClientConfig: [{}]", kaaClientConfig);
        logger.info("Getting twitter board app token...");
        List<ApplicationDto> applications = client.getApplications();
        logger.info("All available applications: [{}]", applications);
        ApplicationDto twitterBoardApplication = getTwitterBoardApplication(applications);
        if (twitterBoardApplication == null) {
            logger.info("No application with name as 'Twitter board' was found.");
            throw new RuntimeException("Can't get twitter board application!");
        }
        String twitterBoardAppToken = twitterBoardApplication.getApplicationToken();
        logger.info("Twitter board app token was gotten: [{}]", twitterBoardAppToken);
        logger.info("Getting twitter board notification...");
        List<NotificationSchemaDto> notificationSchemas = client.getNotificationSchemasByAppToken(twitterBoardApplication.getApplicationToken());
        logger.info("All available notification schemas for twitter board application: [{}]", notificationSchemas);
        NotificationSchemaDto twitterBoardNotificationSchema = getTwitterBoardNotificationSchema(notificationSchemas);
        Integer twitterBoardNfSchemaVersion = twitterBoardNotificationSchema.getVersion();
        logger.info("Twitter board schema version was gotten: {}", twitterBoardNfSchemaVersion);
        kaaClientConfig.put(APP_TOKEN, twitterBoardAppToken);
        kaaClientConfig.put(NF_SCHEMA_VERSION, twitterBoardNfSchemaVersion);
        body = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(configBody);
        logger.info("Configuration body: [{}]", body);
        baseConfiguration.setBody(body);
        baseConfiguration.setStatus(UpdateStatus.INACTIVE);
        logger.info("Editing the configuration...");
        baseConfiguration = client.editConfiguration(baseConfiguration);
        logger.info("Configuration was successfully edited");
        logger.info("Activating the configuration");
        client.activateConfiguration(baseConfiguration.getId());
        logger.info("Configuration was activated");

        logger.info("Finished loading 'Twitter monitor application' data...");
    }

    private ApplicationDto getTwitterBoardApplication(List<ApplicationDto> applications) {
        for (ApplicationDto application : applications) {
            if (TWITTER_BOARD.equalsIgnoreCase(application.getName())) {
                return application;
            }
        }
        return null;
    }

    private NotificationSchemaDto getTwitterBoardNotificationSchema(List<NotificationSchemaDto> notificationSchemas) {
        for (NotificationSchemaDto notificationSchema : notificationSchemas) {
            if (TWITTER_BOARD_NOTIFICATION_SCHEMA.equalsIgnoreCase(notificationSchema.getName())) {
                return notificationSchema;
            }
        }
        return null;
    }
}
