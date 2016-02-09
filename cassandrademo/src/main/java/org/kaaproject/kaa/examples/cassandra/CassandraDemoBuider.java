/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.examples.cassandra;

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
public class CassandraDemoBuider extends AbstractDemoBuilder {


    private static final Logger LOG = LoggerFactory.getLogger(CassandraDemoBuider.class);

    public CassandraDemoBuider() {
        super("demo/cassandra");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

        LOG.info("Loading 'Cassandra data analytics demo application' data...");

        loginTenantAdmin(client);

        ApplicationDto cassandraApplication = new ApplicationDto();
        cassandraApplication.setName("Cassandra data analytics demo");
        cassandraApplication = client.editApplication(cassandraApplication);

        sdkProfileDto.setApplicationId(cassandraApplication.getId());
        sdkProfileDto.setApplicationToken(cassandraApplication.getApplicationToken());
        sdkProfileDto.setProfileSchemaVersion(0);
        sdkProfileDto.setConfigurationSchemaVersion(1);
        sdkProfileDto.setNotificationSchemaVersion(1);

        loginTenantDeveloper(client);

        LogSchemaDto logSchemaDto = new LogSchemaDto();
        logSchemaDto.setApplicationId(cassandraApplication.getId());
        logSchemaDto.setName("SensorData");
        logSchemaDto.setDescription("Log schema describing incoming logs");
        logSchemaDto = client.createLogSchema(logSchemaDto, getResourcePath("logSchema.json"));
        sdkProfileDto.setLogSchemaVersion(logSchemaDto.getVersion());

        LogAppenderDto sensorPerRowCassandraLogAppender = new LogAppenderDto();
        sensorPerRowCassandraLogAppender.setName("sensor_per_row");
        sensorPerRowCassandraLogAppender.setDescription("Sensor per row Cassandra log appender.");
        sensorPerRowCassandraLogAppender.setApplicationId(cassandraApplication.getId());
        sensorPerRowCassandraLogAppender.setApplicationToken(cassandraApplication.getApplicationToken());
        sensorPerRowCassandraLogAppender.setTenantId(cassandraApplication.getTenantId());
        sensorPerRowCassandraLogAppender.setMinLogSchemaVersion(1);
        sensorPerRowCassandraLogAppender.setMaxLogSchemaVersion(Integer.MAX_VALUE);
        sensorPerRowCassandraLogAppender.setConfirmDelivery(true);
        sensorPerRowCassandraLogAppender.setHeaderStructure(Arrays.asList(LogHeaderStructureDto.TIMESTAMP));
        sensorPerRowCassandraLogAppender.setPluginTypeName("Cassandra");
        sensorPerRowCassandraLogAppender.setPluginClassName("org.kaaproject.kaa.server.appenders.cassandra.appender.CassandraLogAppender");
        sensorPerRowCassandraLogAppender.setJsonConfiguration(FileUtils.readResource(getResourcePath("sensor_per_row_cassandra_appender.json")));
        sensorPerRowCassandraLogAppender = client.editLogAppenderDto(sensorPerRowCassandraLogAppender);

        LogAppenderDto sensorPerDateCassandraLogAppender = new LogAppenderDto();
        sensorPerDateCassandraLogAppender.setName("sensor_per_date");
        sensorPerDateCassandraLogAppender.setDescription("Sensor per date Cassandra log appender.");
        sensorPerDateCassandraLogAppender.setApplicationId(cassandraApplication.getId());
        sensorPerDateCassandraLogAppender.setApplicationToken(cassandraApplication.getApplicationToken());
        sensorPerDateCassandraLogAppender.setTenantId(cassandraApplication.getTenantId());
        sensorPerDateCassandraLogAppender.setMinLogSchemaVersion(1);
        sensorPerDateCassandraLogAppender.setMaxLogSchemaVersion(Integer.MAX_VALUE);
        sensorPerDateCassandraLogAppender.setConfirmDelivery(true);
        sensorPerDateCassandraLogAppender.setHeaderStructure(Arrays.asList(LogHeaderStructureDto.TIMESTAMP));
        sensorPerDateCassandraLogAppender.setPluginTypeName("Cassandra");
        sensorPerDateCassandraLogAppender.setPluginClassName("org.kaaproject.kaa.server.appenders.cassandra.appender.CassandraLogAppender");
        sensorPerDateCassandraLogAppender.setJsonConfiguration(FileUtils.readResource(getResourcePath("sensor_per_date_cassandra_appender.json")));
        sensorPerDateCassandraLogAppender = client.editLogAppenderDto(sensorPerDateCassandraLogAppender);

        LogAppenderDto sensorPerRegionCassandraLogAppender = new LogAppenderDto();
        sensorPerRegionCassandraLogAppender.setName("sensor_per_region");
        sensorPerRegionCassandraLogAppender.setDescription("Sensor per region Cassandra log appender.");
        sensorPerRegionCassandraLogAppender.setApplicationId(cassandraApplication.getId());
        sensorPerRegionCassandraLogAppender.setApplicationToken(cassandraApplication.getApplicationToken());
        sensorPerRegionCassandraLogAppender.setTenantId(cassandraApplication.getTenantId());
        sensorPerRegionCassandraLogAppender.setMinLogSchemaVersion(1);
        sensorPerRegionCassandraLogAppender.setMaxLogSchemaVersion(Integer.MAX_VALUE);
        sensorPerRegionCassandraLogAppender.setConfirmDelivery(true);
        sensorPerRegionCassandraLogAppender.setHeaderStructure(Arrays.asList(LogHeaderStructureDto.TIMESTAMP));
        sensorPerRegionCassandraLogAppender.setPluginTypeName("Cassandra");
        sensorPerRegionCassandraLogAppender.setPluginClassName("org.kaaproject.kaa.server.appenders.cassandra.appender.CassandraLogAppender");
        sensorPerRegionCassandraLogAppender.setJsonConfiguration(FileUtils.readResource(getResourcePath("sensor_per_region_cassandra_appender.json")));
        sensorPerRegionCassandraLogAppender = client.editLogAppenderDto(sensorPerRegionCassandraLogAppender);

        LOG.info("Finished loading 'Cassandra data analytics demo application' data.");
    }

}
