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

    private static final Logger LOG = LoggerFactory.getLogger(ProfilingDemoBuilder.class);
    private static final Integer AUTO_GENERATED_SCHEMA_VERSION = 1;
    private static final Integer AUTO_GENERATED_SCHEMA_INDEX = 0;
    private static final Integer GROUP_WEIGHT_START = 10;
    private static final Integer GROUP_WEIGHT_INCREMENT = 10;

    public ProfilingDemoBuilder() {
        super("demo/profiling");
    }

    // method for fast builder testing
    // just setup IP and port parameters
    public static void main(String[] args) {
        ProfilingDemoBuilder pdb = new ProfilingDemoBuilder();
        String kaaNodeIp = "10.2.3.18";
        int kaaNodePort = 8080;
        AdminClient client = new AdminClient(kaaNodeIp, kaaNodePort);
        try {
            pdb.buildDemoApplicationImpl(client);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

        LOG.info("Loading 'Endpoint profiling demo application' data...");

        loginTenantAdmin(client);
        ApplicationDto application = createApplicationDto(client, "Endpoint profiles and grouping demo", "Trustful");

        loginTenantDeveloper(client);

        EndpointGroupDto baseEndpointGroup = getBaseEndpointGroup(client, application);
        if (baseEndpointGroup == null) {
            throw new RuntimeException("Can't get default endpoint group for profiling application!");
        }

        // Configure SDK
        sdkProfileDto.setApplicationId(application.getId());
        sdkProfileDto.setApplicationToken(application.getApplicationToken());

        sdkProfileDto.setNotificationSchemaVersion(AUTO_GENERATED_SCHEMA_VERSION);
        sdkProfileDto.setLogSchemaVersion(AUTO_GENERATED_SCHEMA_VERSION);

        // Create configuration schema
        ConfigurationSchemaDto configurationSchema =
                createConfigurationSchema(client, application, "Configuration schema", "Configuration schema description", "configuration_schema.avsc");
        sdkProfileDto.setConfigurationSchemaVersion(configurationSchema.getVersion());

        // Create configuration
        createConfiguration(client, application, configurationSchema, baseEndpointGroup,
                            "All (base) group configuration", "configuration_default_conf.json", UpdateStatus.ACTIVE);

        // Client profile schema
        EndpointProfileSchemaDto clientProfileSchema = createClientProfileSchema(client, application,
                "Endpoint client profile schema", "Endpoint client profile schema description", "client_profile_schema.avsc");
        sdkProfileDto.setProfileSchemaVersion(clientProfileSchema.getVersion());

        // Server profile schema
        ServerProfileSchemaDto serverProfileSchema = createServerProfileSchema(client, application,
                "server_profile_schema.avsc", "Endpoint server profile schema", "Endpoint server profile schema description");

        // Create profile groups
        // first group
        Integer weight = GROUP_WEIGHT_START;
        EndpointGroupDto endpointGroup = createProfileGroup(client, application,
                "Audio and vibration support", "Audio and vibration group description", weight);
        createConfiguration(client, application, configurationSchema, endpointGroup,
                "Audio and vibration group configuration", "group_audio_vibro_conf.json", UpdateStatus.ACTIVE);
        createProfileGroupFilter(client, application, endpointGroup, clientProfileSchema, serverProfileSchema,
                "group_audio_vibro_filter.txt", "Audio and vibration group filter", UpdateStatus.ACTIVE);

        // second group
        weight += GROUP_WEIGHT_INCREMENT;
        endpointGroup = createProfileGroup(client, application, "Only vibration support", "Auto-generated group description", weight);
        createConfiguration(client, application, configurationSchema, endpointGroup,
                "Only vibration group configuration", "group_only_vibro_conf.json", UpdateStatus.ACTIVE);
        createProfileGroupFilter(client, application, endpointGroup, clientProfileSchema,  serverProfileSchema,
                "group_only_vibro_filter.txt", "Only vibration group filter", UpdateStatus.ACTIVE);

        // third group
        weight += GROUP_WEIGHT_INCREMENT;
        endpointGroup = createProfileGroup(client, application, "All services support", "Auto-generated group description", weight);
        createConfiguration(client, application, configurationSchema, endpointGroup,
                "All services group configuration", "group_all_services_conf.json", UpdateStatus.ACTIVE);
        createProfileGroupFilter(client, application, endpointGroup, clientProfileSchema,  serverProfileSchema,
                "group_all_services_filter.txt", "All services group filter", UpdateStatus.ACTIVE);

        // fourth group
        weight += GROUP_WEIGHT_INCREMENT;
        endpointGroup = createProfileGroup(client, application,
                "Disable vibration for those who have audio and video", "Disable vibration group description", weight);
        createConfiguration(client, application, configurationSchema, endpointGroup,
                "Disable vibration group configuration", "group_disable_vibro_conf.json", UpdateStatus.ACTIVE);
        createProfileGroupFilter(client, application, endpointGroup, clientProfileSchema,  serverProfileSchema,
                "group_disable_vibro_filter.txt", "Disable vibration group filter", UpdateStatus.INACTIVE);

        LOG.info("Finished loading 'Endpoint profiling demo application' data...");
    }

    private ApplicationDto createApplicationDto(AdminClient client, String appName, String credentialsService) throws Exception {
        ApplicationDto application = new ApplicationDto();
        application.setName(appName);
        application.setCredentialsServiceName(credentialsService);

        application = client.editApplication(application);
        return application;
    }

    private EndpointGroupDto getBaseEndpointGroup(AdminClient client, ApplicationDto application) throws Exception {
        List<EndpointGroupDto> groups = client.getEndpointGroupsByAppToken(application.getApplicationToken());
        if (groups.size() > 0) {
            EndpointGroupDto firstGroup = groups.get(AUTO_GENERATED_SCHEMA_INDEX);
            if (firstGroup.getWeight() == 0) {
                return firstGroup;
            }
        }

        return null;
    }

    private ConfigurationSchemaDto createConfigurationSchema(AdminClient client, ApplicationDto application,
                                                             String confName, String confDesc, String schemaFilename) throws Exception {

        CTLSchemaDto configurationCtlSchema = saveCTLSchemaWithAppToken(client, schemaFilename, application);
        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setApplicationId(application.getId());
        configurationSchema.setName(confName);
        configurationSchema.setDescription(confDesc);
        configurationSchema.setCtlSchemaId(configurationCtlSchema.getId());

        configurationSchema = client.saveConfigurationSchema(configurationSchema);
        LOG.info("Configuration schema \"{}\" was created. Schema version: {}", confName, configurationSchema.getVersion());

        return configurationSchema;
    }

    private void createConfiguration(AdminClient client, ApplicationDto application, ConfigurationSchemaDto configurationSchema,
                                     EndpointGroupDto baseEndpointGroup, String confDesc, String confFile, UpdateStatus status) throws Exception {

        ConfigurationDto configuration = new ConfigurationDto();
        configuration.setApplicationId(application.getId());
        configuration.setEndpointGroupId(baseEndpointGroup.getId());
        configuration.setSchemaId(configurationSchema.getId());
        configuration.setSchemaVersion(configurationSchema.getVersion());
        configuration.setDescription(confDesc);
        configuration.setBody(FileUtils.readResource(getResourcePath(confFile)));
        configuration.setStatus(status);

        configuration = client.editConfiguration(configuration);
        client.activateConfiguration(configuration.getId());

        LOG.info("Configuration \"{}\" was created and activated", confDesc);
    }

    private EndpointProfileSchemaDto createClientProfileSchema(AdminClient client, ApplicationDto application,
                                                               String profileName, String profileDescription, String schemaFilename) throws Exception {

        CTLSchemaDto endpointProfileCtlSchema = saveCTLSchemaWithAppToken(client, schemaFilename, application);
        EndpointProfileSchemaDto endpointProfileSchema = new EndpointProfileSchemaDto();
        endpointProfileSchema.setApplicationId(application.getId());
        endpointProfileSchema.setName(profileName);
        endpointProfileSchema.setDescription(profileDescription);
        endpointProfileSchema.setCtlSchemaId(endpointProfileCtlSchema.getId());
        endpointProfileSchema = client.saveProfileSchema(endpointProfileSchema);
        return endpointProfileSchema;
    }

    private EndpointGroupDto createProfileGroup(AdminClient client, ApplicationDto application, String groupName, String description, int weight)
            throws Exception {

        EndpointGroupDto endpointGroup = new EndpointGroupDto();
        endpointGroup.setApplicationId(application.getId());
        endpointGroup.setName(groupName);
        endpointGroup.setDescription(description);
        endpointGroup.setWeight(weight);
        endpointGroup = client.editEndpointGroup(endpointGroup);
        return endpointGroup;
    }

    private void createProfileGroupFilter(AdminClient client, ApplicationDto application, EndpointGroupDto endpointGroup,
                                          EndpointProfileSchemaDto clientProfileSchema,ServerProfileSchemaDto serverProfileSchema,
                                          String filterNameFile, String filterDesc, UpdateStatus filterStatus) throws Exception {
        ProfileFilterDto profileFilter = new ProfileFilterDto();
        profileFilter.setApplicationId(application.getId());
        profileFilter.setEndpointGroupId(endpointGroup.getId());
        profileFilter.setEndpointProfileSchemaId(clientProfileSchema.getId());
        profileFilter.setServerProfileSchemaId(serverProfileSchema.getId());
        profileFilter.setServerProfileSchemaVersion(serverProfileSchema.getVersion());
        profileFilter.setDescription(filterDesc);
        profileFilter.setBody(FileUtils.readResource(getResourcePath(filterNameFile)));

        profileFilter = client.editProfileFilter(profileFilter);

        if (filterStatus == UpdateStatus.ACTIVE) {
            client.activateProfileFilter(profileFilter.getId());
        }
    }

    private ServerProfileSchemaDto createServerProfileSchema(AdminClient client, ApplicationDto application,
                                                             String fileName, String schemaName, String schemaDesc) throws Exception {

        CTLSchemaDto serverProfileCtlSchema = saveCTLSchemaWithAppToken(client, fileName, application);
        ServerProfileSchemaDto serverProfileSchema = new ServerProfileSchemaDto();
        serverProfileSchema.setApplicationId(application.getId());
        serverProfileSchema.setName(schemaName);
        serverProfileSchema.setDescription(schemaDesc);
        serverProfileSchema.setCtlSchemaId(serverProfileCtlSchema.getId());

        serverProfileSchema = client.saveServerProfileSchema(serverProfileSchema);
        return serverProfileSchema;
    }

}
