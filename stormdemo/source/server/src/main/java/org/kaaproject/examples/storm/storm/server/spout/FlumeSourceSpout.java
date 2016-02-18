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

package org.kaaproject.examples.storm.storm.server.spout;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.flume.*;
import org.apache.flume.instrumentation.SinkCounter;
import org.apache.flume.node.MaterializedConfiguration;
import org.kaaproject.examples.storm.storm.server.common.*;
import org.kaaproject.examples.storm.storm.server.producer.AvroTupleProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


@SuppressWarnings("serial")
public class 	FlumeSourceSpout implements IRichSpout {
    public static final String FLUME_PROPERTY_PREFIX = "flume-agent";
    public static final String PROP_FLUME_AGENT_NAME = "flume-agent";
    public static final String PROP_FLUME_BATCH_SIZE = "batch-size";
    public static final int BATCH_SIZE = 1;

    private Channel channel;
    private SourceRunner sourceRunner;
    private int batchSize = BATCH_SIZE;
    private SinkCounter sinkCounter;
    private SpoutOutputCollector outputCollector;
    private static final Logger LOG = LoggerFactory.getLogger(FlumeSourceSpout.class);


    public String getFlumePropertyPrefix() {
        return FLUME_PROPERTY_PREFIX;
    }

    private AvroTupleProducer avroTupleProducer;

    public AvroTupleProducer getAvroTupleProducer() {
        return avroTupleProducer;
    }

    public void setAvroTupleProducer(AvroTupleProducer avroTupleProducer) {
        this.avroTupleProducer = avroTupleProducer;
    }

    @SuppressWarnings("rawtypes")
    public void open(Map config, TopologyContext context,
            SpoutOutputCollector collector) {
        MaterializedConfigurationProvider configurationProvider = new MaterializedConfigurationProvider();

        Map<String, String> flumeAgentProps = Maps.newHashMap();
        for (Object key : config.keySet()) {
            LOG.debug("Spout config:" + key.toString() + ":" + config.get(key));
            if (key.toString().startsWith(getFlumePropertyPrefix())) {
                if (key.toString().contains(PROP_FLUME_BATCH_SIZE)) {
                    String batchSizeStr = (String) config.get(key);
                    try {
                        this.batchSize = Integer.parseInt(batchSizeStr);
                    } catch (Exception e) {
                        this.batchSize = BATCH_SIZE;
                    }
                } else {
                    flumeAgentProps.put(key.toString().replace(getFlumePropertyPrefix() + ".",""),
                            (String) config.get(key));
                }
            }
        }

        flumeAgentProps = StormEmbeddedAgentConfiguration.configure(
                PROP_FLUME_AGENT_NAME, flumeAgentProps);
        MaterializedConfiguration conf = configurationProvider.get(
                getFlumePropertyPrefix(), flumeAgentProps);

        Map<String, Channel> channels = conf.getChannels();
        if (channels.size() != 1) {
            throw new FlumeException("Expected one channel and got "
                    + channels.size());
        }
        Map<String, SourceRunner> sources = conf.getSourceRunners();
        if (sources.size() != 1) {
            throw new FlumeException("Expected one source and got "
                    + sources.size());
        }

        this.sourceRunner = sources.values().iterator().next();
        this.channel = channels.values().iterator().next();

        if (sinkCounter == null) {
            sinkCounter = new SinkCounter(FlumeSourceSpout.class.getName());
        }
        if (null == this.getAvroTupleProducer()) {
            throw new IllegalStateException("Tuple Producer has not been set.");
        }

        this.outputCollector = collector;

        try {
            this.start();
        } catch (Exception e) {
            LOG.warn("Error starting source/channel", e);
        }
    }


    private void start() {
        if (null == this.sourceRunner || null == this.channel) {
            throw new FlumeException(
                    "Source/Channel is null. Cannot start flume components");
        }
        this.sourceRunner.start();
        this.channel.start();
        this.sinkCounter.start();
    }

    private void stop() {
        if (null == this.sourceRunner || null == this.channel) {
            return;
        }
        this.sourceRunner.stop();
        this.channel.stop();
        this.sinkCounter.stop();
    }


    public void close() {
        try {
            this.stop();
        } catch (Exception e) {
            LOG.warn("Error closing Avro RPC storm.server.", e);
        }
    }

    @Override
    public void activate() {
    }


    @Override
    public void deactivate() {
    }
    @Override
    public void nextTuple() {

        LOG.debug("Transaction begins...");
        Transaction transaction = channel.getTransaction();

        int size = 0;
        try {
            transaction.begin();
            List<Event> batch = Lists.newLinkedList();

            for (int i = 0; i < this.batchSize; i++) {
                Event event = channel.take();
                if (event == null) {
                    break;
                }
                batch.add(event);
            }
            LOG.debug("Batch size: " + batch.size());
            size = batch.size();
            if (size == 0) {
                sinkCounter.incrementBatchEmptyCount();
            } else {
                if (size < this.batchSize) {
                    sinkCounter.incrementBatchUnderflowCount();
                } else {
                    sinkCounter.incrementBatchCompleteCount();
                }
                sinkCounter.addToEventDrainAttemptCount(size);
            }

            for (Event event : batch) {
                Values vals = this.getAvroTupleProducer().toTuple(event);
                //Emit tuple with ability to process fail state
                this.outputCollector.emit(vals, event);
                LOG.debug("NextTuple:"
                        + event.getHeaders().get(org.kaaproject.examples.storm.storm.server.common.Constants.MESSAGE_ID));
            }
            transaction.commit();
            sinkCounter.addToEventDrainSuccessCount(size);
        } catch (Throwable t) {
            transaction.rollback();
            if (t instanceof Error) {
                throw (Error) t;
            } else if (t instanceof ChannelException) {
                LOG.error(
                        "Unable to get event from" + " channel "
                                + channel.getName() + ". Exception follows.", t);
            } else {
                LOG.error("Failed to emit events", t);
            }
        } finally {
            transaction.close();
        }
        LOG.info("Sleeping at empty batch...");
        if (size == 0) {
            Utils.sleep(100);
        }
    }

    @Override
    public void fail(Object msgId) {
        //Here you can implement logic to Tuples that failed at processing state
        System.out.println("FAILED msgId: " + msgId);

    }
    @Override
    public void ack(Object msgId) {
        LOG.info("Message proceeded successfully " + msgId);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        this.getAvroTupleProducer().declareOutputFields(declarer);
    }
    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
