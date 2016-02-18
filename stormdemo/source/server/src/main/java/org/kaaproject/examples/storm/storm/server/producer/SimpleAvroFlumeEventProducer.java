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

import backtype.storm.tuple.Tuple;
import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.event.SimpleEvent;
import org.kaaproject.examples.storm.storm.server.common.Constants;

import java.util.HashMap;
import java.util.Map;


@SuppressWarnings("serial")
public class SimpleAvroFlumeEventProducer implements AvroFlumeEventProducer {



    @SuppressWarnings("unchecked")
    public Event toEvent(Tuple input) throws Exception {

        Map<String, String> headers;
        Object headerObj;
        Object messageObj;

        if(input.size()==2){
            messageObj = input.getValue(1);
            headers = new HashMap<String, String>();
            headers.put(Constants.MESSAGE_ID, input.getString(0));
            headers.put(Constants.TIME_STAMP, String.valueOf(System.currentTimeMillis()));
        }else if(input.size()==3){
            headerObj = input.getValue(1);
            messageObj = input.getValue(2);
            headers = (Map<String, String>)headerObj;
        }else{
            throw new IllegalStateException("Wrong format of tuple expected 2 or 3 values. But found "
                    + input.size());
        }
        return EventBuilder.withBody(((SimpleEvent)messageObj).getBody(), headers);
    }
}
