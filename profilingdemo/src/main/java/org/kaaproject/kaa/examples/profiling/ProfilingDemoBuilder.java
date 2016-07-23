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
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.examples.common.AbstractDemoBuilder;
import org.kaaproject.kaa.examples.common.KaaDemoBuilder;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Maksym Liashenko
 */
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

        ApplicationDto profilingApplication = new ApplicationDto();
        profilingApplication.setName("Endpoint profiling demo");
        profilingApplication.setCredentialsServiceName("Trustful");
        profilingApplication = client.editApplication(profilingApplication);


        sdkProfileDto.setApplicationId(profilingApplication.getId());
        sdkProfileDto.setApplicationToken(profilingApplication.getApplicationToken());
        sdkProfileDto.setNotificationSchemaVersion(1);
        sdkProfileDto.setLogSchemaVersion(1);

        loginTenantDeveloper(client);

        EndpointGroupDto baseEndpointGroup = null;
        List<EndpointGroupDto> endpointGroups = client.getEndpointGroupsByAppToken(profilingApplication.getApplicationToken());
        if (endpointGroups.size() == 1 && endpointGroups.get(0).getWeight() == 0) {
            baseEndpointGroup = endpointGroups.get(0);
        }
        if (baseEndpointGroup == null) {
            throw new RuntimeException("Can't get default endpoint group for profiling application!");
        }

        // Profile schema
        CTLSchemaDto endpointProfileCtlSchema = client.saveCTLSchemaWithAppToken(getResourceAsString("endpoint_profile_schema.avsc"),
                profilingApplication.getTenantId(), profilingApplication.getApplicationToken());
        CTLSchemaDto configurationCTLSchema = saveCTLSchemaWithAppToken(client, "configuration-schema.avsc",
                profilingApplication);

        EndpointProfileSchemaDto endpointProfileSchema = new EndpointProfileSchemaDto();
        endpointProfileSchema.setApplicationId(profilingApplication.getId());
        endpointProfileSchema.setName("Endpoint profiling endpoint profile schema");
        endpointProfileSchema.setDescription("Endpoint profile schema describing profiling application profile");
        endpointProfileSchema.setCtlSchemaId(endpointProfileCtlSchema.getId());
        endpointProfileSchema = client.saveProfileSchema(endpointProfileSchema);
        sdkProfileDto.setProfileSchemaVersion(endpointProfileSchema.getVersion());

        // Configuration schema
        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setApplicationId(profilingApplication.getId());
        configurationSchema.setName("Endpoint profiling configuration schema");
        configurationSchema.setDescription("Configuration schema describing profiling application profile");
        configurationSchema.setCtlSchemaId(configurationCTLSchema.getId());
        configurationSchema = client.saveConfigurationSchema(configurationSchema);

        logger.info("Configuration schema version: {}", configurationSchema.getVersion());
        sdkProfileDto.setConfigurationSchemaVersion(configurationSchema.getVersion());
        logger.info("Configuration schema was created.");

        // Configuration
        ConfigurationDto baseConfiguration = new ConfigurationDto();
        baseConfiguration.setApplicationId(profilingApplication.getId());
        baseConfiguration.setEndpointGroupId(baseEndpointGroup.getId());
        baseConfiguration.setSchemaId(configurationSchema.getId());
        baseConfiguration.setSchemaVersion(configurationSchema.getVersion());
        baseConfiguration.setDescription("Base endpoint profiling configuration");

        String body = FileUtils.readResource(getResourcePath("configuration_devices.json"));
        logger.info("Configuration body: [{}]", body);
        baseConfiguration.setBody(body);
        baseConfiguration.setStatus(UpdateStatus.INACTIVE);

        logger.info("Editing the configuration...");
        baseConfiguration = client.editConfiguration(baseConfiguration);
        logger.info("Configuration was successfully edited");

        logger.info("Activating the configuration");
        client.activateConfiguration(baseConfiguration.getId());
        logger.info("Configuration was activated");

        logger.info("Finished loading 'Endpoint profiling demo application' data...");
    }
}
