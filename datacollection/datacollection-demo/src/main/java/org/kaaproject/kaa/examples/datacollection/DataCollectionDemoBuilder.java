/**
 * Copyright 2014-2016 CyberVision, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.examples.datacollection;


import org.apache.commons.lang3.tuple.Pair;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.examples.common.AbstractDemoBuilder;
import org.kaaproject.kaa.examples.common.KaaDemoBuilder;
import org.kaaproject.kaa.examples.util.cmd.CommandLine;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

@KaaDemoBuilder
public class DataCollectionDemoBuilder extends AbstractDemoBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(DataCollectionDemoBuilder.class);

    private static final String DATA_COLLECTION_TAR_PATH = "/usr/lib/kaa-sandbox/demo_projects/java/datacollection_demo.tar.gz";
    private static final String KAA_NODE_LIB_PATH = "/usr/lib/kaa-node/lib/";
    private static final String ADDITIONAL_LIBS_IN_TAR_PATH = "JDataCollectionDemo/lib/kaa-node-lib/";
    private static final String EXTRACT_ADDITIONAL_LIBS_PARAMS = "-xvf " + DATA_COLLECTION_TAR_PATH + " -C " + KAA_NODE_LIB_PATH + " " + ADDITIONAL_LIBS_IN_TAR_PATH + " --strip-components=3";

    public DataCollectionDemoBuilder() {
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


        LOG.info("Data collection demo: Creating server profile schema for telemetry monitor...");
        CTLSchemaDto serverProfileCtlSchema = saveCTLSchemaWithAppToken(client, "telemetry_monitor_server_profile.avsc", dataCollectionApplication);
        ServerProfileSchemaDto serverProfileSchemaDto = new ServerProfileSchemaDto();
        serverProfileSchemaDto.setApplicationId(dataCollectionApplication.getId());
        serverProfileSchemaDto.setName("TelemetryMonitorServerProfile");
        serverProfileSchemaDto.setVersion(serverProfileCtlSchema.getVersion());
        serverProfileSchemaDto.setCtlSchemaId(serverProfileCtlSchema.getId());
        serverProfileSchemaDto.setDescription("Telemetry Monitor Server Profile");
        serverProfileSchemaDto = client.saveServerProfileSchema(serverProfileSchemaDto);
        LOG.info("Telemetry Monitor Server Profile schema was created: [{}]", serverProfileSchemaDto);


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

        LOG.info("Data collection demo: Creating Log appenders...");
        LOG.info("Creating Cassandra Log Appender");
        client.editLogAppenderDto(createCassandraLogAppender(dataCollectionApplication));
        LOG.info("Creating Telemetry Monitor Appender");
        client.editLogAppenderDto(createTelemetryMonitorAppender(dataCollectionApplication));
        LOG.info("Finished loading 'Data Collection Demo Application' data.");
    }

    private LogAppenderDto createCassandraLogAppender(ApplicationDto dataCollectionApplication) throws IOException {
        LogAppenderDto dataCollectionLogAppender = new LogAppenderDto();
        dataCollectionLogAppender.setName("Data collection log appender");
        dataCollectionLogAppender.setDescription("Log appender used to deliver log records from data collection application to cassandra database");
        dataCollectionLogAppender.setApplicationId(dataCollectionApplication.getId());
        dataCollectionLogAppender.setApplicationToken(dataCollectionApplication.getApplicationToken());
        dataCollectionLogAppender.setTenantId(dataCollectionApplication.getTenantId());
        dataCollectionLogAppender.setMinLogSchemaVersion(1);
        dataCollectionLogAppender.setMaxLogSchemaVersion(Integer.MAX_VALUE);
        dataCollectionLogAppender.setConfirmDelivery(true);
        dataCollectionLogAppender.setHeaderStructure(Arrays.asList(LogHeaderStructureDto.TIMESTAMP, LogHeaderStructureDto.TOKEN));
        dataCollectionLogAppender.setPluginTypeName("Cassandra");
        dataCollectionLogAppender.setPluginClassName("org.kaaproject.kaa.server.appenders.cassandra.appender.CassandraLogAppender");
        dataCollectionLogAppender.setJsonConfiguration(FileUtils.readResource(getResourcePath("cassandra_appender.json")));
        return dataCollectionLogAppender;
    }

    private  LogAppenderDto createTelemetryMonitorAppender(ApplicationDto dataCollectionApplication) throws IOException {
        LogAppenderDto dataCollectionLogAppender = new LogAppenderDto();
        dataCollectionLogAppender.setName("Telemetry Monitor");
        dataCollectionLogAppender.setDescription("Log appender for detecting, processing and saving specific threshold values to the database and save the statistics to the server profile.");
        dataCollectionLogAppender.setApplicationId(dataCollectionApplication.getId());
        dataCollectionLogAppender.setApplicationToken(dataCollectionApplication.getApplicationToken());
        dataCollectionLogAppender.setTenantId(dataCollectionApplication.getTenantId());
        dataCollectionLogAppender.setMinLogSchemaVersion(1);
        dataCollectionLogAppender.setMaxLogSchemaVersion(Integer.MAX_VALUE);
        dataCollectionLogAppender.setConfirmDelivery(true);
        dataCollectionLogAppender.setHeaderStructure(Arrays.asList(LogHeaderStructureDto.TIMESTAMP, LogHeaderStructureDto.TOKEN));
        dataCollectionLogAppender.setPluginTypeName("TelemetryMonitor");
        dataCollectionLogAppender.setPluginClassName("org.kaaproject.kaa.server.appenders.telemetry.appender.TelemetryMonitor");
        dataCollectionLogAppender.setJsonConfiguration(FileUtils.readResource(getResourcePath("telemetry_monitor_appender.json")));
        return dataCollectionLogAppender;
    }

    @Override
    public Stream<Pair<CommandLine, String>> getAdditionalCommandsAndParams() {
        return Stream.of(Pair.of(CommandLine.TAR, EXTRACT_ADDITIONAL_LIBS_PARAMS));
    }
}
