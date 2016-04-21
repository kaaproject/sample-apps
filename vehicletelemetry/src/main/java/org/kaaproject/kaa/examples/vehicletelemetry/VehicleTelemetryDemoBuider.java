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

package org.kaaproject.kaa.examples.vehicletelemetry;


import java.util.Arrays;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
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
public class VehicleTelemetryDemoBuider extends AbstractDemoBuilder {


    private static final Logger logger = LoggerFactory.getLogger(VehicleTelemetryDemoBuider.class);

    public VehicleTelemetryDemoBuider() {
        super("demo/vehicletelemetry");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

        logger.info("Loading 'Vehicle telemetry application' data...");

        loginTenantAdmin(client);

        ApplicationDto vehicleTelemetryApplication = new ApplicationDto();
        vehicleTelemetryApplication.setName("Vehicle telemetry");
        vehicleTelemetryApplication = client.editApplication(vehicleTelemetryApplication);

        sdkProfileDto.setApplicationId(vehicleTelemetryApplication.getId());
        sdkProfileDto.setConfigurationSchemaVersion(1);
        sdkProfileDto.setNotificationSchemaVersion(1);

        loginTenantDeveloper(client);

        CTLSchemaDto profileCtlSchema = client.saveCTLSchemaWithAppToken(getResourceAsString("profileSchema.json"), vehicleTelemetryApplication.getTenantId(),
                vehicleTelemetryApplication.getApplicationToken());

        EndpointProfileSchemaDto profileSchema = new EndpointProfileSchemaDto();
        profileSchema.setApplicationId(vehicleTelemetryApplication.getId());
        profileSchema.setName("Vehicle telemetry profile schema");
        profileSchema.setDescription("Profile schema describing vehicle telemetry application profile");
        profileSchema.setCtlSchemaId(profileCtlSchema.getId());
        profileSchema = client.saveProfileSchema(profileSchema);
        sdkProfileDto.setProfileSchemaVersion(profileSchema.getVersion());

        LogSchemaDto logSchemaDto = new LogSchemaDto();
        logSchemaDto.setApplicationId(vehicleTelemetryApplication.getId());
        logSchemaDto.setName("Vehicle telemetry log schema");
        logSchemaDto.setDescription("Log schema describing incoming logs");
        logSchemaDto = client.createLogSchema(logSchemaDto, getResourcePath("logSchema.json"));
        sdkProfileDto.setLogSchemaVersion(logSchemaDto.getVersion());

        LogAppenderDto vehicleTelemetryLogAppender = new LogAppenderDto();
        vehicleTelemetryLogAppender.setName("Vehicle telemetry log appender");
        vehicleTelemetryLogAppender.setDescription("Log appender used to deliver log records from vehicle telemetry application to local mongo db instance");
        vehicleTelemetryLogAppender.setApplicationId(vehicleTelemetryApplication.getId());
        vehicleTelemetryLogAppender.setApplicationToken(vehicleTelemetryApplication.getApplicationToken());
        vehicleTelemetryLogAppender.setTenantId(vehicleTelemetryApplication.getTenantId());
        vehicleTelemetryLogAppender.setMinLogSchemaVersion(1);
        vehicleTelemetryLogAppender.setMaxLogSchemaVersion(Integer.MAX_VALUE);
        vehicleTelemetryLogAppender.setConfirmDelivery(true);
        vehicleTelemetryLogAppender.setHeaderStructure(Arrays.asList(LogHeaderStructureDto.KEYHASH,
                LogHeaderStructureDto.TIMESTAMP, LogHeaderStructureDto.TOKEN, LogHeaderStructureDto.VERSION));
        vehicleTelemetryLogAppender.setPluginTypeName("MongoDB");
        vehicleTelemetryLogAppender.setPluginClassName("org.kaaproject.kaa.server.appenders.mongo.appender.MongoDbLogAppender");
        vehicleTelemetryLogAppender.setJsonConfiguration(FileUtils.readResource(getResourcePath("mongoAppender.json")));
        vehicleTelemetryLogAppender = client.editLogAppenderDto(vehicleTelemetryLogAppender);

        logger.info("Finished loading 'Vehicle telemetry application' data.");
    }

}
