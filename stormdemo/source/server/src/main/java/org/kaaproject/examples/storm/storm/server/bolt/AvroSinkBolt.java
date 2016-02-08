package org.kaaproject.examples.storm.storm.server.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import org.apache.flume.Event;
import org.kaaproject.examples.storm.storm.server.producer.AvroFlumeEventProducer;
import org.kaaproject.kaa.examples.powerplant.PowerReport;
import org.kaaproject.kaa.examples.powerplant.PowerSample;
import org.kaaproject.kaa.server.common.log.shared.KaaFlumeEventReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings("serial")
public class AvroSinkBolt implements IRichBolt {

    private static final Logger LOG = LoggerFactory.getLogger(AvroSinkBolt.class);
    public static final String DEFAULT_FLUME_PROPERTY_PREFIX = "flume-avro-forward";

    private static final KaaFlumeEventReader<PowerReport> kaaReader = new KaaFlumeEventReader<PowerReport>(PowerReport.class);
    private AvroFlumeEventProducer producer;
    private OutputCollector collector;

    public String getFlumePropertyPrefix() {
        return DEFAULT_FLUME_PROPERTY_PREFIX;
    }

    public void setProducer(AvroFlumeEventProducer producer) {
        this.producer = producer;
    }

    @SuppressWarnings("rawtypes")
    public void prepare(Map config, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        Properties sinkProperties  = new Properties();
        LOG.info("Looking for flume properties");
        for (Object key : config.keySet()) {
            if (key.toString().startsWith(this.getFlumePropertyPrefix())) {
                LOG.info("Found:Key:" + key.toString() + ":" + config.get(key));
                sinkProperties.put(key.toString().replace(this.getFlumePropertyPrefix() + ".",""),
                        config.get(key));
            }
        }
    }

    public void execute(Tuple input) {
        try {
            Event event = this.producer.toEvent(input);
            int reportsCount = 0;
            int samplesCount = 0;
            for(PowerReport report : kaaReader.decodeRecords(ByteBuffer.wrap(event.getBody()))){
                reportsCount++;
                samplesCount += report.getSamples().size();
                for(PowerSample sample : report.getSamples()){
                    LOG.info("Sample zone ID: " + sample.getZoneId() + ", "
                                + "panel ID: " + sample.getPanelId() + ", "
                                + "power: " + sample.getPower());
                }
            }
            LOG.info("Total records received: " + reportsCount);
            LOG.info("Total samples received: " + samplesCount);
            //All seems to be nice, notify storm.spout about it
            this.collector.ack(input);
        } catch (Exception e) {
            LOG.warn("Failing tuple: " + input);
            LOG.warn("Exception: ", e);
            //Notify storm.spout about fail
            this.collector.fail(input);
        }
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
