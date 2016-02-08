package org.kaaproject.examples.storm.storm.server;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.kaaproject.examples.storm.storm.server.bolt.AvroSinkBolt;
import org.kaaproject.examples.storm.storm.server.producer.AvroTupleProducer;
import org.kaaproject.examples.storm.storm.server.producer.SimpleAvroFlumeEventProducer;
import org.kaaproject.examples.storm.storm.server.producer.SimpleAvroTupleProducer;
import org.kaaproject.examples.storm.storm.server.spout.FlumeSourceSpout;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class Main {
    public static void main(String[] args) throws Throwable{
        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        Logger LOG = (Logger)LoggerFactory.getLogger(Main.class);

        Properties props = new Properties();
        props.load(Main.class.getResourceAsStream("/storm.properties"));

        TopologyBuilder builder = new TopologyBuilder();
        FlumeSourceSpout spout = new FlumeSourceSpout();

        AvroTupleProducer producer = new SimpleAvroTupleProducer();
        spout.setAvroTupleProducer(producer);

        builder.setSpout("FlumeSourceSpout", spout).addConfigurations(props);

        AvroSinkBolt bolt = new AvroSinkBolt();
        bolt.setProducer(new SimpleAvroFlumeEventProducer());
        //Set 2 threads storm.bolt
        builder.setBolt("AvroSinkBolt", bolt, 2).shuffleGrouping("FlumeSourceSpout").addConfigurations(props);

        Config config = new Config(); //Default configuration
        config.setDebug(false);

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("T1", config, builder.createTopology());
        LOG.info("Topology running...");

        //Wait for any input and kill topology
        System.in.read();
        cluster.shutdown();
    }
}
