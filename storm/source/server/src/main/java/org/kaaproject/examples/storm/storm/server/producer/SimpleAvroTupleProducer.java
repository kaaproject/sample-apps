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
