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

package org.kaaproject.kaa.examples.datacollection;


import java.util.Arrays;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.examples.common.AbstractDemoBuilder;
import org.kaaproject.kaa.examples.common.KaaDemoBuilder;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KaaDemoBuilder
public class DataCollectionDemoBuider extends AbstractDemoBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(DataCollectionDemoBuider.class);

    // method for fast builder testing
    // just setup IP and port parameters
    public static void main(String[] args) {
        DataCollectionDemoBuider pdb = new DataCollectionDemoBuider();
        String kaaNodeIp = "10.2.3.18";
        int kaaNodePort = 8080;
        AdminClient client = new AdminClient(kaaNodeIp, kaaNodePort);
        try {
            pdb.buildDemoApplicationImpl(client);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DataCollectionDemoBuider() {
        super("demo/datacollection");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

        LOG.info("Loading 'Data Collection Demo Application' data...");

        loginTenantAdmin(client);

        ApplicationDto dataCollectionApplication = new ApplicationDto();
        dataCollectionApplication.setName("Data collection demo");
        dataCollectionApplication = client.editApplication(dataCollectionApplication);

        LOG.info("Data collection demo: Creating SDK profile...");
        sdkProfileDto.setApplicationId(dataCollectionApplication.getId());
        sdkProfileDto.setName("Default SDK profile");
        sdkProfileDto.setApplicationToken(dataCollectionApplication.getApplicationToken());
        sdkProfileDto.setProfileSchemaVersion(0);
        sdkProfileDto.setNotificationSchemaVersion(1);

        loginTenantDeveloper(client);

        LOG.info("Data collection demo: Creating configuration schema...");
        CTLSchemaDto configCtlSchema = saveCTLSchemaWithAppToken(client, "config_schema.avsc", dataCollectionApplication);
        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setApplicationId(dataCollectionApplication.getId());
        configurationSchema.setName("Configuration schema");
        configurationSchema.setDescription("Default configuration schema for the Data collection demo application");
        configurationSchema.setCtlSchemaId(configCtlSchema.getId());
        configurationSchema = client.saveConfigurationSchema(configurationSchema);

        LOG.info("Configuration schema version: {}", configurationSchema.getVersion());
        sdkProfileDto.setConfigurationSchemaVersion(configurationSchema.getVersion());
        LOG.info("Configuration schema was created.");

        LOG.info("Data collection demo: Creating log schema...");
        LogSchemaDto logSchemaDto = new LogSchemaDto();
        logSchemaDto.setApplicationId(dataCollectionApplication.getId());
        logSchemaDto.setName("Logging schema");
        logSchemaDto.setDescription("Default logging schema for the Data collection demo application");
        CTLSchemaDto loggingCtlSchema = saveCTLSchemaWithAppToken(client, "logging_schema.avsc", dataCollectionApplication);
        logSchemaDto.setCtlSchemaId(loggingCtlSchema.getId());
        logSchemaDto = client.saveLogSchema(logSchemaDto);

        LOG.info("Log schema version: {}", logSchemaDto.getVersion());
        sdkProfileDto.setLogSchemaVersion(logSchemaDto.getVersion());
        LOG.info("Log schema was created.");

        LOG.info("Data collection demo: Creating Log appender...");
        LogAppenderDto dataCollectionLogAppender = new LogAppenderDto();
        dataCollectionLogAppender.setName("Data collection log appender");
        dataCollectionLogAppender.setDescription("Log appender used to deliver log records from data collection application to local mongo db instance");
        dataCollectionLogAppender.setApplicationId(dataCollectionApplication.getId());
        dataCollectionLogAppender.setApplicationToken(dataCollectionApplication.getApplicationToken());
        dataCollectionLogAppender.setTenantId(dataCollectionApplication.getTenantId());
        dataCollectionLogAppender.setMinLogSchemaVersion(1);
        dataCollectionLogAppender.setMaxLogSchemaVersion(Integer.MAX_VALUE);
        dataCollectionLogAppender.setConfirmDelivery(true);
        dataCollectionLogAppender.setHeaderStructure(Arrays.asList(LogHeaderStructureDto.KEYHASH, 
                LogHeaderStructureDto.TIMESTAMP, LogHeaderStructureDto.TOKEN, LogHeaderStructureDto.VERSION));
        dataCollectionLogAppender.setPluginTypeName("MongoDB");
        dataCollectionLogAppender.setPluginClassName("org.kaaproject.kaa.server.appenders.mongo.appender.MongoDbLogAppender");
        dataCollectionLogAppender.setJsonConfiguration(FileUtils.readResource(getResourcePath("mongo_appender.json")));
        client.editLogAppenderDto(dataCollectionLogAppender);

        LOG.info("Finished loading 'Data Collection Demo Application' data.");
    }

}
