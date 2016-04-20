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

package org.kaaproject.kaa.examples.streetlight;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.examples.common.AbstractDemoBuilder;
import org.kaaproject.kaa.examples.common.KaaDemoBuilder;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KaaDemoBuilder
public class StreetLightDriverDemoBuilder extends AbstractDemoBuilder {

    private static final Logger logger = LoggerFactory.getLogger(StreetLightDriverDemoBuilder.class);

    private static final int LIGHT_ZONE_COUNT = 4;

    public StreetLightDriverDemoBuilder() {
        super("demo/streetlight");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

        logger.info("Loading 'Street lights driver application' data...");

        loginTenantAdmin(client);

        ApplicationDto streetLightApplication = new ApplicationDto();
        streetLightApplication.setName("Street light driver");
        streetLightApplication = client.editApplication(streetLightApplication);

        sdkProfileDto.setApplicationId(streetLightApplication.getId());
        sdkProfileDto.setProfileSchemaVersion(0);
        sdkProfileDto.setNotificationSchemaVersion(1);
        sdkProfileDto.setLogSchemaVersion(1);
        sdkProfileDto.setConfigurationSchemaVersion(1);

        loginTenantDeveloper(client);

        logger.info("Creating profile schema...");
        
        CTLSchemaDto profileCtlSchema = client.saveCTLSchemaWithAppToken(getResourceAsString("profile.avsc"), streetLightApplication.getTenantId(),
                streetLightApplication.getApplicationToken());
        
        EndpointProfileSchemaDto profileSchemaDto = new EndpointProfileSchemaDto();
        profileSchemaDto.setApplicationId(streetLightApplication.getId());
        profileSchemaDto.setName("StreetLightsDriverProfile schema");
        profileSchemaDto.setDescription("Street light driver profile schema");
        profileSchemaDto.setCtlSchemaId(profileCtlSchema.getId());
        profileSchemaDto = client.saveProfileSchema(profileSchemaDto);
        logger.info("Profile schema version: {}", profileSchemaDto.getVersion());
        sdkProfileDto.setProfileSchemaVersion(profileSchemaDto.getVersion());
        logger.info("Profile schema was created.");

        logger.info("Creating configuration schema...");
        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setApplicationId(streetLightApplication.getId());
        configurationSchema.setName("StreetLightsConfiguration schema");
        configurationSchema.setDescription("Street Light configuration schema");
        configurationSchema = client.createConfigurationSchema(configurationSchema, getResourcePath("configuration.avsc"));
        logger.info("Configuration schema version: {}", configurationSchema.getVersion());
        sdkProfileDto.setConfigurationSchemaVersion(configurationSchema.getVersion());
        logger.info("Configuration schema was created");

        for (int i = 0; i < LIGHT_ZONE_COUNT; ++i) {
            EndpointGroupDto group = new EndpointGroupDto();
            group.setApplicationId(streetLightApplication.getId());
            group.setName("Zone " + Integer.toString(i));
            group.setWeight(i + 1);
            logger.info("Creating Endpoint group for Light Zone {}", i);
            group = client.editEndpointGroup(group);
            logger.info("Created Endpoint group for Light Zone {}", i);

            ProfileFilterDto filter = new ProfileFilterDto();
            filter.setApplicationId(streetLightApplication.getId());
            filter.setEndpointGroupId(group.getId());
            filter.setEndpointProfileSchemaId(profileSchemaDto.getId());
            filter.setEndpointProfileSchemaVersion(profileSchemaDto.getVersion());
            filter.setBody("lightZones.contains(new Integer(" + Integer.toString(i) + "))");
            filter.setStatus(UpdateStatus.INACTIVE);
            logger.info("Creating Profile filter for Light Zone {}", i);
            filter = client.editProfileFilter(filter);
            logger.info("Activating Profile filter for Light Zone {}", i);
            client.activateProfileFilter(filter.getId());
            logger.info("Created and activated Profile filter for Light Zone {}", i);

            ConfigurationDto baseGroupConfiguration = new ConfigurationDto();
            baseGroupConfiguration.setApplicationId(streetLightApplication.getId());
            baseGroupConfiguration.setEndpointGroupId(group.getId());
            baseGroupConfiguration.setSchemaId(configurationSchema.getId());
            baseGroupConfiguration.setSchemaVersion(configurationSchema.getVersion());
            baseGroupConfiguration.setDescription("Base street light driver configuration");
            String body = getConfigurationBodyForEndpointGroup(i);
            logger.info("Configuration body: [{}]", body);
            baseGroupConfiguration.setBody(body);
            logger.info("Editing the configuration...");
            baseGroupConfiguration = client.editConfiguration(baseGroupConfiguration);
            logger.info("Configuration was successfully edited");
            logger.info("Activating the configuration");
            client.activateConfiguration(baseGroupConfiguration.getId());
            logger.info("Configuration was activated");
        }

        logger.info("Finished loading 'Street lights driver application' data...");
    }

    private String getConfigurationBodyForEndpointGroup(int zoneId) {
        return "{\n" +
                "  \"lightZones\": \n" +
                "    { \"array\": \n" +
                "      [\n" +
                "        {\"zoneId\":\n" +
                "          { \"int\": " + zoneId + " },\n" +
                "          \"zoneStatus\": \n" +
                "            { \"org.kaaproject.kaa.demo.iotworld.lights.street.ZoneStatus\" :\"DISABLE\"},\n" +
                "          \"__uuid\": null\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "  \"__uuid\": null\n" +
                "}\n";
    }

}
