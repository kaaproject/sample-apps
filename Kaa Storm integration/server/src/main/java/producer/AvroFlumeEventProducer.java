

package producer;

import backtype.storm.tuple.Tuple;
import org.apache.flume.Event;

import java.io.Serializable;


public interface AvroFlumeEventProducer extends Serializable {
	Event toEvent(Tuple input) throws Exception;
}
