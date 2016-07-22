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

package org.kaaproject.kaa.examples.powerplant;

import java.io.FileInputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.examples.common.AbstractDemoBuilder;
import org.kaaproject.kaa.examples.common.KaaDemoBuilder;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KaaDemoBuilder
public class PowerPlantAndroidDemoBuilder extends AbstractDemoBuilder {
    private static final Logger logger = LoggerFactory.getLogger(PowerPlantAndroidDemoBuilder.class);

    public PowerPlantAndroidDemoBuilder() {
        super("demo/powerplantandroid");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

        logger.info("Loading 'Power plant android application' data...");

        loginTenantAdmin(client);

        ApplicationDto powerPlantAndroidApplciation = new ApplicationDto();
        powerPlantAndroidApplciation.setName("Power plant demo android");
        powerPlantAndroidApplciation = client.editApplication(powerPlantAndroidApplciation);

        sdkProfileDto.setApplicationId(powerPlantAndroidApplciation.getId());
        sdkProfileDto.setNotificationSchemaVersion(1);
        sdkProfileDto.setProfileSchemaVersion(0);
        sdkProfileDto.setLogSchemaVersion(1);

        loginTenantDeveloper(client);

        logger.info("Creating ctl schema...");

        CTLSchemaDto ctlSchema = client.saveCTLSchemaWithAppToken(getResourceAsString("config_schema.avsc"), powerPlantAndroidApplciation.getTenantId(), powerPlantAndroidApplciation.getApplicationToken());

        logger.info("Creating configuration schema...");
        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setApplicationId(powerPlantAndroidApplciation.getId());
        configurationSchema.setName("Powerplant android demo schema");
        configurationSchema.setDescription("Default configuration schema for the powerplant android demo");
        configurationSchema.setCtlSchemaId(ctlSchema.getId());
        configurationSchema = client.saveConfigurationSchema(configurationSchema);

        logger.info("Configuration schema version: {}", configurationSchema.getVersion());
        sdkProfileDto.setConfigurationSchemaVersion(configurationSchema.getVersion());
        logger.info("Configuration schema was created.");

        EndpointGroupDto baseEndpointGroup = null;
        List<EndpointGroupDto> endpointGroups = client.getEndpointGroupsByAppToken(powerPlantAndroidApplciation.getApplicationToken());
        if (endpointGroups.size() == 1 && endpointGroups.get(0).getWeight() == 0) {
            baseEndpointGroup = endpointGroups.get(0);
        }

        if (baseEndpointGroup == null) {
            throw new RuntimeException("Can't get default endpoint group for powerplant android demo!");
        }

        ConfigurationDto baseConfiguration = new ConfigurationDto();
        baseConfiguration.setApplicationId(powerPlantAndroidApplciation.getId());
        baseConfiguration.setEndpointGroupId(baseEndpointGroup.getId());
        baseConfiguration.setSchemaId(configurationSchema.getId());
        baseConfiguration.setSchemaVersion(configurationSchema.getVersion());
        baseConfiguration.setDescription("Base powerplant android demo configuration");
        String body = FileUtils.readResource(getResourcePath("config_data.json"));
        logger.info("Configuration body: [{}]", body);
        baseConfiguration.setBody(body);
        baseConfiguration.setStatus(UpdateStatus.INACTIVE);
        logger.info("Editing the configuration...");
        baseConfiguration = client.editConfiguration(baseConfiguration);
        logger.info("Configuration was successfully edited");
        logger.info("Activating the configuration");
        client.activateConfiguration(baseConfiguration.getId());
        logger.info("Configuration was activated");

        logger.info("Finished loading 'Power plant android application' data.");
    }
}
