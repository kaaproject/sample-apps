

package producer;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Values;
import org.apache.flume.Event;

import java.io.Serializable;


public interface AvroTupleProducer extends Serializable{
	Values toTuple(Event event) throws Exception;
	void declareOutputFields(OutputFieldsDeclarer declarer);
}
