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

package org.kaaproject.kaa.examples.zeppelin;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.examples.common.AbstractDemoBuilder;
import org.kaaproject.kaa.examples.common.KaaDemoBuilder;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@KaaDemoBuilder
public class ZeppelinDemoBuider extends AbstractDemoBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(ZeppelinDemoBuider.class);

    public ZeppelinDemoBuider() {
        super("demo/zeppelin");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

        LOG.info("Loading 'Zeppelin data analytics demo application' data...");

        loginTenantAdmin(client);

        ApplicationDto sparkApplication = new ApplicationDto();
        sparkApplication.setName("Zeppelin data analytics demo");
        sparkApplication = client.editApplication(sparkApplication);

        sdkProfileDto.setApplicationId(sparkApplication.getId());
        sdkProfileDto.setApplicationToken(sparkApplication.getApplicationToken());
        sdkProfileDto.setProfileSchemaVersion(0);
        sdkProfileDto.setConfigurationSchemaVersion(1);
        sdkProfileDto.setNotificationSchemaVersion(1);

        loginTenantDeveloper(client);

        LogSchemaDto powerReportLogSchemaDto = new LogSchemaDto();
        powerReportLogSchemaDto.setApplicationId(sparkApplication.getId());
        powerReportLogSchemaDto.setName("Power report");
        powerReportLogSchemaDto.setDescription("Zeppelin data analytics demo Power report log schema");
        powerReportLogSchemaDto = client.createLogSchema(powerReportLogSchemaDto, getResourcePath("powerReportLogSchema.json"));
        sdkProfileDto.setLogSchemaVersion(powerReportLogSchemaDto.getVersion());

        LogAppenderDto panelPerRowCassandraLogAppender = new LogAppenderDto();
        panelPerRowCassandraLogAppender.setName("Panel per row appender");
        panelPerRowCassandraLogAppender.setDescription("Panel per row Cassandra log appender.");
        panelPerRowCassandraLogAppender.setApplicationId(sparkApplication.getId());
        panelPerRowCassandraLogAppender.setApplicationToken(sparkApplication.getApplicationToken());
        panelPerRowCassandraLogAppender.setTenantId(sparkApplication.getTenantId());
        panelPerRowCassandraLogAppender.setMinLogSchemaVersion(1);
        panelPerRowCassandraLogAppender.setMaxLogSchemaVersion(Integer.MAX_VALUE);
        panelPerRowCassandraLogAppender.setConfirmDelivery(true);
        panelPerRowCassandraLogAppender.setPluginTypeName("Cassandra");
        panelPerRowCassandraLogAppender.setPluginClassName("org.kaaproject.kaa.server.appenders.cassandra.appender.CassandraLogAppender");
        panelPerRowCassandraLogAppender.setJsonConfiguration(FileUtils.readResource(getResourcePath("panel_per_row.json")));
        panelPerRowCassandraLogAppender = client.editLogAppenderDto(panelPerRowCassandraLogAppender);

        LogAppenderDto zonePerRowCassandraLogAppender = new LogAppenderDto();
        zonePerRowCassandraLogAppender.setName("Zone per row appender");
        zonePerRowCassandraLogAppender.setDescription("Zone per row Cassandra log appender.");
        zonePerRowCassandraLogAppender.setApplicationId(sparkApplication.getId());
        zonePerRowCassandraLogAppender.setApplicationToken(sparkApplication.getApplicationToken());
        zonePerRowCassandraLogAppender.setTenantId(sparkApplication.getTenantId());
        zonePerRowCassandraLogAppender.setMinLogSchemaVersion(1);
        zonePerRowCassandraLogAppender.setMaxLogSchemaVersion(Integer.MAX_VALUE);
        zonePerRowCassandraLogAppender.setConfirmDelivery(true);
        zonePerRowCassandraLogAppender.setPluginTypeName("Cassandra");
        zonePerRowCassandraLogAppender.setPluginClassName("org.kaaproject.kaa.server.appenders.cassandra.appender.CassandraLogAppender");
        zonePerRowCassandraLogAppender.setJsonConfiguration(FileUtils.readResource(getResourcePath("zone_per_row.json")));
        zonePerRowCassandraLogAppender = client.editLogAppenderDto(zonePerRowCassandraLogAppender);

        LOG.info("Finished loading 'Zeppelin data analytics demo application' data.");
    }

}
