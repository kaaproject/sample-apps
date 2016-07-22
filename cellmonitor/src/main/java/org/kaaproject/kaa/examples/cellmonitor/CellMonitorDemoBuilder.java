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

package org.kaaproject.kaa.examples.cellmonitor;

import java.util.Arrays;

import org.kaaproject.kaa.common.dto.ApplicationDto;
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
public class CellMonitorDemoBuilder extends AbstractDemoBuilder {
    
    private static final Logger LOG = LoggerFactory.getLogger(CellMonitorDemoBuilder.class);
    
    public CellMonitorDemoBuilder() {
        super("demo/cellmonitor");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client)
            throws Exception {
        
        LOG.info("Loading 'Cell Monitor Demo Application' data...");
        
        loginTenantAdmin(client);
        
        ApplicationDto cellMonitorApplication = new ApplicationDto();
        cellMonitorApplication.setName("Cell monitor");
        cellMonitorApplication = client.editApplication(cellMonitorApplication);
        
        sdkProfileDto.setApplicationId(cellMonitorApplication.getId());
        sdkProfileDto.setApplicationToken(cellMonitorApplication.getApplicationToken());
        sdkProfileDto.setNotificationSchemaVersion(1);
        sdkProfileDto.setConfigurationSchemaVersion(1);
        sdkProfileDto.setProfileSchemaVersion(0);

        loginTenantDeveloper(client);
        
        LogSchemaDto logSchema = new LogSchemaDto();
        logSchema.setApplicationId(cellMonitorApplication.getId());
        logSchema.setName("Cell monitor log schema");
        logSchema.setDescription("Log schema describing cell monitor record with information about current cell location, signal strength and phone gps location.");
        CTLSchemaDto ctlSchema =
                client.saveCTLSchemaWithAppToken(getResourceAsString("cell_monitor_log.avsc"), cellMonitorApplication.getTenantId(), cellMonitorApplication.getApplicationToken());
        logSchema.setCtlSchemaId(ctlSchema.getId());
        logSchema = client.createLogSchema(logSchema);
        sdkProfileDto.setLogSchemaVersion(logSchema.getVersion());
        
        LogAppenderDto cellMonitorLogAppender = new LogAppenderDto();
        cellMonitorLogAppender.setName("Cell monitor log appender");
        cellMonitorLogAppender.setDescription("Log appender used to deliver log records from cell monitor application to local mongo db instance");
        cellMonitorLogAppender.setApplicationId(cellMonitorApplication.getId());
        cellMonitorLogAppender.setApplicationToken(cellMonitorApplication.getApplicationToken());
        cellMonitorLogAppender.setTenantId(cellMonitorApplication.getTenantId());
        cellMonitorLogAppender.setMinLogSchemaVersion(1);
        cellMonitorLogAppender.setMaxLogSchemaVersion(Integer.MAX_VALUE);
        cellMonitorLogAppender.setConfirmDelivery(true);
        cellMonitorLogAppender.setHeaderStructure(Arrays.asList(LogHeaderStructureDto.KEYHASH, 
                LogHeaderStructureDto.TIMESTAMP, LogHeaderStructureDto.TOKEN, LogHeaderStructureDto.VERSION));
        cellMonitorLogAppender.setPluginTypeName("MongoDB");
        cellMonitorLogAppender.setPluginClassName("org.kaaproject.kaa.server.appenders.mongo.appender.MongoDbLogAppender");
        cellMonitorLogAppender.setJsonConfiguration(FileUtils.readResource(getResourcePath("mongo_appender.json")));
        cellMonitorLogAppender = client.editLogAppenderDto(cellMonitorLogAppender);
        
        LOG.info("Finished loading 'Cell Monitor Demo Application' data.");
    }

}
