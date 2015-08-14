import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import bolt.AvroSinkBolt;
import producer.AvroTupleProducer;
import producer.SimpleAvroFlumeEventProducer;
import producer.SimpleAvroTupleProducer;
import spout.FlumeSourceSpout;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws Throwable{
        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.DEBUG);
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
        //Set 2 threads bolt
        builder.setBolt("AvroSinkBolt", bolt, 2).shuffleGrouping("FlumeSourceSpout").addConfigurations(props);

        Config config = new Config(); //Default configuration
        config.setDebug(false);

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("T1", config, builder.createTopology());
        LOG.info("Topology running...");

        //Kill topology if needed
        //cluster.shutdown();
    }
}
