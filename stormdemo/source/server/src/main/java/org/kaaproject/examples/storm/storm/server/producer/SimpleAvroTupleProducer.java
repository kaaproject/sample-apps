
package org.kaaproject.examples.storm.storm.server.producer;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import org.apache.flume.Event;
import org.kaaproject.examples.storm.storm.server.common.Constants;

import java.util.UUID;


@SuppressWarnings("serial")
public class SimpleAvroTupleProducer implements AvroTupleProducer{

	public Values toTuple(Event event) throws Exception {
		String msgID = event.getHeaders().get(Constants.MESSAGE_ID);

		//set the msgId if not present
		if(null == msgID) {
			UUID randMsgID = UUID.randomUUID();
			msgID = randMsgID.toString();
			event.getHeaders().put(Constants.MESSAGE_ID, msgID);
		}
		return new Values(msgID,event);
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields(Constants.MESSAGE_ID,Constants.MESSAGE));
	}
}
