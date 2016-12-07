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

package org.kaaproject.kaa.server.appenders.telemetry.appender;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.appenders.telemetry.config.gen.CassandraCredential;
import org.kaaproject.kaa.server.appenders.telemetry.config.gen.TelemetryMonitorConfiguration;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.log.shared.appender.AbstractLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.kaaproject.kaa.server.appenders.telemetry.appender.DataCollection.DataCollectionField.TEMPERATURE;
import static org.kaaproject.kaa.server.appenders.telemetry.appender.DataCollection.DataCollectionField.TIME_STAMP;

/**
 * Sample appender implementation that uses {@link TelemetryMonitorConfiguration} as configuration.
 */
public class TelemetryMonitor extends AbstractLogAppender<TelemetryMonitorConfiguration> {

    private static final Logger LOG = LoggerFactory.getLogger(TelemetryMonitor.class);
    public static final ObjectMapper MAPPER = new ObjectMapper();

    private AdminClient adminClient;
    private static final int SCHEMA_VERSION = 1;

    private Cluster cluster;
    private Session session;
    private CassandraOperations cassandraOps;
    private String monitoringValuesTableName;

    private int thresholdValue;
    private FailedThresholdStat thresholdStat;

    private boolean previousFailed;

    private boolean closed = false;

    private ThreadLocal<Map<String, GenericAvroConverter<GenericRecord>>> converters =
            new ThreadLocal<Map<String, GenericAvroConverter<GenericRecord>>>() {
                @Override
                protected Map<String, GenericAvroConverter<GenericRecord>> initialValue() {
                    return new HashMap<>();
                }
            };

    public TelemetryMonitor() {
        super(TelemetryMonitorConfiguration.class);
    }

    public TelemetryMonitor(Class<TelemetryMonitorConfiguration> configurationClass) {
        super(configurationClass);
    }

    /**
     * Inits the appender from configuration.
     *
     * @param appender      the metadata object that contains useful info like application token, tenant id, etc.
     * @param configuration the configuration object that you have specified during appender provisioning.
     */
    @Override
    protected void initFromConfiguration(LogAppenderDto appender, TelemetryMonitorConfiguration configuration) {
        try {
            thresholdValue = configuration.getThreshold();
            thresholdStat = new FailedThresholdStat();
            initAdminClient();
            initCassandra(configuration);
        } catch (Exception e) {
            LOG.error("Couldn't initialize " + TelemetryMonitor.class.getName(), e);
            throw new RuntimeException(e);
        }
    }

    private void initAdminClient() {
        try {
            adminClient = new AdminClient("localhost", 8080);
            adminClient.login("devuser", "devuser123");
        } catch (Exception e) {
            LOG.warn("Cannot innit admin client, the statistics will not updated.", e);
            adminClient = null;
        }
    }

    private void initCassandra(TelemetryMonitorConfiguration configuration) throws UnknownHostException {

        LOG.info("Initializing cassandra database connection");

        List<InetSocketAddress> cassandraNodes = configuration.getCassandraServers()
                .stream()
                .map(s -> {
                    try {
                        return new InetSocketAddress(InetAddress.getByName(s.getHost()), s.getPort());
                    } catch (UnknownHostException e) {
                        LOG.error("Cannot init cassandra node with address {}:{}", s.getHost(), s.getPort());
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());

        Cluster.Builder builder = Cluster.builder().addContactPointsWithPorts(cassandraNodes);
        CassandraCredential credential = configuration.getCassandraCredential();
        if (null != credential) {
            builder.withCredentials(credential.getUser(), credential.getPassword());
        }
        cluster = builder.build();

        session = cluster.connect(configuration.getKeySpace());
        cassandraOps = new CassandraTemplate(session);

        monitoringValuesTableName = configuration.getMonitoringValuesTableName();

        cassandraOps.execute(CreateTableSpecification.createTable(monitoringValuesTableName).ifNotExists()
                .partitionKeyColumn(TIME_STAMP.getName(), DataType.bigint())
                .column(TEMPERATURE.getName(), DataType.cint())
        );
    }

    /**
     * Consumes and delivers logs.
     *
     * @param logEventPack        container for log events with some metadata like log event schema.
     * @param recordHeader        additional data about log event source (endpoint key hash, application token, header version, timestamp).
     * @param logDeliveryCallback report status of log delivery.
     */
    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader recordHeader, LogDeliveryCallback logDeliveryCallback) {
        if (closed) {
            LOG.info("Attempted to append to closed appender named [{}].", getName());
            return;
        }

        logEventPack.getEvents().stream()
                .filter(logEvent -> null != logEvent && null != logEvent.getLogData())
                .map(convertSamples(getConverter(logEventPack.getLogSchema().getSchema())))
                .filter(Objects::nonNull)
                .forEach(processSamples(logDeliveryCallback, logEventPack.getEndpointKey()));
    }

    private Function<LogEvent, DataCollection> convertSamples(GenericAvroConverter<GenericRecord> eventConverter) {
        return logEvent -> {
            LOG.trace("Avro record converter [{}] with log data [{}]", eventConverter, logEvent.getLogData());
            try {
                return getDataCollectionSample(eventConverter.decodeBinary(logEvent.getLogData()));
            } catch (Exception e) {
                LOG.warn("Cannot convert samples.", e);
            }
            return null;
        };
    }

    private static DataCollection getDataCollectionSample(GenericRecord decodedLog) {
        return DataCollection.of((Integer) decodedLog.get(TEMPERATURE.getName()), (Long) decodedLog.get(TIME_STAMP.getName()));
    }

    private Consumer<DataCollection> processSamples(LogDeliveryCallback logDeliveryCallback, String endpointKey) {
        return sample -> {
            try {
                Integer temperature = sample.getTemperature();
                Long timeStamp = sample.getTimeStamp();
                if (temperature > thresholdValue) {
                    LOG.info("Temperature [{}] is above threshold [{}]", temperature, thresholdValue);
                    thresholdStat.updateStats(timeStamp, previousFailed);

                    Optional.ofNullable(adminClient).ifPresent(sendStatisticsToAdminClient(adminClient, endpointKey, thresholdStat));

                    previousFailed = true;
                    LOG.debug("Inserting values into table [{}]", monitoringValuesTableName);
                    cassandraOps.execute(QueryBuilder.insertInto(monitoringValuesTableName)
                            .values(Arrays.asList(TIME_STAMP.getName(), TEMPERATURE.getName()), Arrays.asList(timeStamp, temperature)));
                    LOG.debug("Values: temperature [{}], timeStamp [{}], have been successfully inserted.", temperature, timeStamp);
                } else {
                    previousFailed = false;
                }
                logDeliveryCallback.onSuccess();
            } catch (Exception e) {
                LOG.error("Error during processing data collection sample.", e);
                logDeliveryCallback.onInternalError();
            }
        };
    }

    private static Consumer<AdminClient> sendStatisticsToAdminClient(AdminClient adminClient, String endpointKey, FailedThresholdStat thresholdStat) {
        return client -> {
            try {
                adminClient.updateServerProfile(endpointKey, SCHEMA_VERSION, MAPPER.writeValueAsString(thresholdStat));
                LOG.debug("Updated threshold statistic [{}] has been sent to the server", thresholdStat);
            } catch (Exception e) {
                LOG.warn("Could't send threshold statistic to the server", e);
            }
        };
    }


    /**
     * Closes this appender and releases any resources associated with it.
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        LOG.info("Trying to stop custom log appender...");
        closed = true;
        session.close();
        cluster.close();
        LOG.info("Custom log appender has been stopped.");
    }

    private GenericAvroConverter<GenericRecord> getConverter(String schema) {
        LOG.trace("Get converter for schema [{}]", schema);
        Map<String, GenericAvroConverter<GenericRecord>> converterMap = converters.get();
        GenericAvroConverter<GenericRecord> genAvroConverter = converterMap.get(schema);
        if (genAvroConverter == null) {
            LOG.trace("Create new converter for schema [{}]", schema);
            genAvroConverter = new GenericAvroConverter<GenericRecord>(schema);
            converterMap.put(schema, genAvroConverter);
            converters.set(converterMap);
        }
        LOG.trace("Get converter [{}] from map.", genAvroConverter);
        return genAvroConverter;
    }
}
