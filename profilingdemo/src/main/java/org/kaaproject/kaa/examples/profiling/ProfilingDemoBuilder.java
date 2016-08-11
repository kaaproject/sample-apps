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

package org.kaaproject.kaa.examples.profiling;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.examples.common.AbstractDemoBuilder;
import org.kaaproject.kaa.examples.common.KaaDemoBuilder;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@KaaDemoBuilder
public class ProfilingDemoBuilder extends AbstractDemoBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ProfilingDemoBuilder.class);

    public ProfilingDemoBuilder() {
        super("demo/profiling");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

        logger.info("Loading 'Endpoint profiling demo application' data...");

        loginTenantAdmin(client);
        ApplicationDto profilingApplication = createApplicationDto(client, "Endpoint profiling demo", "Trustful");

        loginTenantDeveloper(client);

        EndpointGroupDto baseEndpointGroup = createBaseEndpointGroup(client, profilingApplication);
        if (baseEndpointGroup == null) {
            throw new RuntimeException("Can't get default endpoint group for profiling application!");
        }

        // Cofigure sdk
        sdkProfileDto.setApplicationId(profilingApplication.getId());
        sdkProfileDto.setApplicationToken(profilingApplication.getApplicationToken());

        sdkProfileDto.setNotificationSchemaVersion(1);
        sdkProfileDto.setLogSchemaVersion(1);

        // Client profile schema
        CTLSchemaDto endpointProfileCtlSchema = client.saveCTLSchemaWithAppToken(getResourceAsString("endpoint_profile_schema.avsc"),
                profilingApplication.getTenantId(), profilingApplication.getApplicationToken());

        createClientProfile(client, profilingApplication, endpointProfileCtlSchema,
                "Endpoint profiling endpoint profile schema", "Endpoint profile schema describing profiling application profile");

        // Configuration schema
        CTLSchemaDto configurationCTLSchema = saveCTLSchemaWithAppToken(client, "configuration-schema.avsc", profilingApplication);
        ConfigurationSchemaDto configurationSchema = createConfigurationSchema(client, profilingApplication, configurationCTLSchema,
                "Endpoint profiling configuration schema", "Configuration schema describing profiling application profile");

        // Configuration
        editActivateConfiguration(client, profilingApplication, configurationSchema, baseEndpointGroup,
                "Base endpoint profiling configuration", "configuration_devices.json", UpdateStatus.INACTIVE);
        sdkProfileDto.setConfigurationSchemaVersion(configurationSchema.getVersion());

        // Profile groups
        ServerProfileSchemaDto serverProfileSchema = createServerSchema(client, profilingApplication,
                "server_profile_schema.avsc", "Endpoint server-side profile schema", "Auto-generated description");

        // first
        EndpointGroupDto endpointGroup = createProfileGroup(client, profilingApplication, "Audio and vibration support", "Auto-generated", 2);

        createProfileGroupConfiguration(client, profilingApplication, endpointGroup, configurationSchema,
                "Auto-generated configuration schema", "group_audio_vibration_conf.json", UpdateStatus.INACTIVE);
        createProfileGroupFilter(client, profilingApplication, endpointGroup, serverProfileSchema,
                "group_audio_vibration_filter.json", "Auto-generated description", UpdateStatus.INACTIVE);

        //second
        endpointGroup = createProfileGroup(client, profilingApplication, "Only vibration support", "Auto-generated", 5);

        createProfileGroupConfiguration(client, profilingApplication, endpointGroup, configurationSchema,
                "Auto-generated configuration schema", "group_only_vibration_conf.json", UpdateStatus.INACTIVE);
        createProfileGroupFilter(client, profilingApplication, endpointGroup, serverProfileSchema,
                "group_only_vibration_filter.json", "Auto-generated description", UpdateStatus.INACTIVE);

        //third
        endpointGroup = createProfileGroup(client, profilingApplication, "All services support", "Auto-generated", 7);
        createProfileGroupConfiguration(client, profilingApplication, endpointGroup, configurationSchema,
                "Auto-generated configuration schema", "group_all_conf.json", UpdateStatus.INACTIVE);
        createProfileGroupFilter(client, profilingApplication, endpointGroup, serverProfileSchema,
                "group_all_filter.json", "Auto-generated description", UpdateStatus.INACTIVE);

        //fourth
        endpointGroup = createProfileGroup(client, profilingApplication, "Disable vibration support for those who have audio and video", "Auto-generated", 10);
        createProfileGroupConfiguration(client, profilingApplication, endpointGroup, configurationSchema,
                "Auto-generated configuration schema", "group_disable_vibro_conf.json", UpdateStatus.INACTIVE);
        createProfileGroupFilter(client, profilingApplication, endpointGroup, serverProfileSchema,
                "group_disable_vibro_filter.json", "Auto-generated description", UpdateStatus.INACTIVE);

        logger.info("Finished loading 'Endpoint profiling demo application' data...");
    }

    private EndpointGroupDto createBaseEndpointGroup(AdminClient client, ApplicationDto profilingApplication) throws Exception {
        EndpointGroupDto baseEndpointGroup = null;
        List<EndpointGroupDto> endpointGroups = client.getEndpointGroupsByAppToken(profilingApplication.getApplicationToken());
        if (endpointGroups.size() == 1 && endpointGroups.get(0).getWeight() == 0) {
            baseEndpointGroup = endpointGroups.get(0);
        }
        return baseEndpointGroup;
    }

    private void editActivateConfiguration(AdminClient client, ApplicationDto profilingApplication, ConfigurationSchemaDto configurationSchema,
                                           EndpointGroupDto baseEndpointGroup,
                                           String confDesc, String confFile, UpdateStatus status) throws Exception {

        ConfigurationDto baseConfiguration = new ConfigurationDto();
        baseConfiguration.setApplicationId(profilingApplication.getId());
        baseConfiguration.setEndpointGroupId(baseEndpointGroup.getId());
        baseConfiguration.setSchemaId(configurationSchema.getId());
        baseConfiguration.setSchemaVersion(configurationSchema.getVersion());
        baseConfiguration.setDescription(confDesc);

        baseConfiguration.setBody(FileUtils.readResource(getResourcePath(confFile)));
        baseConfiguration.setStatus(status);

        logger.info("Editing the configuration...");
        baseConfiguration = client.editConfiguration(baseConfiguration);
        logger.info("Configuration was successfully edited");

        logger.info("Activating the configuration");
        client.activateConfiguration(baseConfiguration.getId());
        logger.info("Configuration was activated");
    }

    private ConfigurationSchemaDto createConfigurationSchema(AdminClient client, ApplicationDto profilingApplication, CTLSchemaDto configurationCTLSchema,
                                                             String confName, String confDesc) throws Exception {

        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setApplicationId(profilingApplication.getId());
        configurationSchema.setName(confName);
        configurationSchema.setDescription(confDesc);
        configurationSchema.setCtlSchemaId(configurationCTLSchema.getId());

        configurationSchema = client.saveConfigurationSchema(configurationSchema);

        logger.info("Configuration schema version: {}", configurationSchema.getVersion());

        sdkProfileDto.setConfigurationSchemaVersion(configurationSchema.getVersion());

        logger.info("Configuration schema was created.");
        return configurationSchema;
    }

    private void createClientProfile(AdminClient client, ApplicationDto profilingApplication, CTLSchemaDto endpointProfileCtlSchema,
                                     String profileName, String profileDescription) throws Exception {
        EndpointProfileSchemaDto endpointProfileSchema = new EndpointProfileSchemaDto();
        endpointProfileSchema.setApplicationId(profilingApplication.getId());
        endpointProfileSchema.setName(profileName);
        endpointProfileSchema.setDescription(profileDescription);
        endpointProfileSchema.setCtlSchemaId(endpointProfileCtlSchema.getId());
        endpointProfileSchema = client.saveProfileSchema(endpointProfileSchema);
        sdkProfileDto.setProfileSchemaVersion(endpointProfileSchema.getVersion());
    }

    private ApplicationDto createApplicationDto(AdminClient client, String appName, String credentialsService) throws Exception {
        ApplicationDto profilingApplication = new ApplicationDto();
        profilingApplication.setName(appName);
        profilingApplication.setCredentialsServiceName(credentialsService);

        profilingApplication = client.editApplication(profilingApplication);
        return profilingApplication;
    }

    private EndpointGroupDto createProfileGroup(AdminClient client, ApplicationDto profilingApplication, String groupName, String description, int weight)
            throws Exception {

        EndpointGroupDto endpointGroup = new EndpointGroupDto();
        endpointGroup.setApplicationId(profilingApplication.getId());
        endpointGroup.setName(groupName);
        endpointGroup.setDescription(description);
        endpointGroup.setWeight(weight);

        endpointGroup = client.editEndpointGroup(endpointGroup);
        return endpointGroup;
    }

    private void createProfileGroupFilter(AdminClient client, ApplicationDto profilingApplication, EndpointGroupDto endpointGroup,
                                          ServerProfileSchemaDto serverProfileSchema,
                                          String filterNameFile, String filterDesc, UpdateStatus filterStatus) throws Exception {

        ProfileFilterDto INACTIVEProfileFilter = new ProfileFilterDto();
        INACTIVEProfileFilter.setApplicationId(profilingApplication.getId());
        INACTIVEProfileFilter.setEndpointGroupId(endpointGroup.getId());
        INACTIVEProfileFilter.setServerProfileSchemaId(serverProfileSchema.getId());
        INACTIVEProfileFilter.setServerProfileSchemaVersion(serverProfileSchema.getVersion());
        INACTIVEProfileFilter.setDescription(filterDesc);
        INACTIVEProfileFilter.setBody(FileUtils.readResource(getResourcePath(filterNameFile)));
        INACTIVEProfileFilter.setStatus(filterStatus);

        INACTIVEProfileFilter = client.editProfileFilter(INACTIVEProfileFilter);
        client.activateProfileFilter(INACTIVEProfileFilter.getId());
    }

    private void createProfileGroupConfiguration(AdminClient client, ApplicationDto profilingApplication, EndpointGroupDto endpointGroup,
                                                 ConfigurationSchemaDto configurationSchema,
                                                 String confDesc, String confFilename, UpdateStatus status) throws Exception {

        ConfigurationDto profileGroupConfiguration = new ConfigurationDto();
        profileGroupConfiguration.setApplicationId(profilingApplication.getId());
        profileGroupConfiguration.setEndpointGroupId(endpointGroup.getId());
        profileGroupConfiguration.setSchemaId(configurationSchema.getId());
        profileGroupConfiguration.setSchemaVersion(configurationSchema.getVersion());
        profileGroupConfiguration.setDescription(confDesc);
        profileGroupConfiguration.setBody(FileUtils.readResource(getResourcePath(confFilename)));
        profileGroupConfiguration.setStatus(status);

        profileGroupConfiguration = client.editConfiguration(profileGroupConfiguration);
        client.activateConfiguration(profileGroupConfiguration.getId());
    }

    private ServerProfileSchemaDto createServerSchema(AdminClient client, ApplicationDto profilingApplication,
                                                      String fileName, String serverName, String serverDesc) throws Exception {

        CTLSchemaDto serverProfileCtlSchema = saveCTLSchemaWithAppToken(client, fileName, profilingApplication);
        return getServerProfileSchemaDto(client, profilingApplication, serverProfileCtlSchema, serverName, serverDesc);
    }

    private ServerProfileSchemaDto getServerProfileSchemaDto(AdminClient client, ApplicationDto profilingApplication, CTLSchemaDto serverProfileCtlSchema,
                                                             String serverName, String serverDesc) throws Exception {

        ServerProfileSchemaDto serverProfileSchema = new ServerProfileSchemaDto();
        serverProfileSchema.setApplicationId(profilingApplication.getId());
        serverProfileSchema.setName(serverName);
        serverProfileSchema.setDescription(serverDesc);
        serverProfileSchema.setCtlSchemaId(serverProfileCtlSchema.getId());

        serverProfileSchema = client.saveServerProfileSchema(serverProfileSchema);

        return serverProfileSchema;
    }
}
