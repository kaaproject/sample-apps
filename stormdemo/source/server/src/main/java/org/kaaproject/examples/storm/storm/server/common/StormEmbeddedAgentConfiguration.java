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

package org.kaaproject.examples.storm.storm.server.common;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import org.apache.flume.FlumeException;
import org.apache.flume.annotations.InterfaceAudience;
import org.apache.flume.annotations.InterfaceStability;
import org.apache.flume.conf.BasicConfigurationConstants;
import org.apache.flume.conf.channel.ChannelType;
import org.apache.flume.conf.source.SourceType;

import java.util.*;


@InterfaceAudience.Public
@InterfaceStability.Stable
public class StormEmbeddedAgentConfiguration {
    public static final String SEPARATOR = ".";
    private static final Joiner JOINER = Joiner.on(SEPARATOR);
    private static final String TYPE = "type";


    public static final String SOURCE = "source";

    public static final String CHANNEL = "channel";

    public static final String SOURCE_TYPE = join(SOURCE, TYPE);

    public static final String SOURCE_PREFIX = join(SOURCE, "");

    public static final String CHANNEL_TYPE = join(CHANNEL, TYPE);

    public static final String CHANNEL_PREFIX = join(CHANNEL, "");

    public static final String CHANNEL_TYPE_MEMORY = ChannelType.MEMORY.name();

    public static final String CHANNEL_TYPE_FILE = ChannelType.FILE.name();

    public static final String SOURCE_TYPE_AVRO = SourceType.AVRO.name();

    public static final String SOURCE_TYPE_HTTP = SourceType.HTTP.name();

    private static final String[] ALLOWED_SOURCES = {
        SOURCE_TYPE_AVRO,
        SOURCE_TYPE_HTTP
    };

    private static final String[] ALLOWED_CHANNELS = {
        CHANNEL_TYPE_MEMORY,
        CHANNEL_TYPE_FILE
    };

    /*
     * Validates the source and channel configuration for required properties
     */
    private static void validate(String name,
                                 Map<String, String> properties) throws FlumeException {
        if(properties.containsKey(SOURCE_TYPE)) {
          checkAllowed(ALLOWED_SOURCES, properties.get(SOURCE_TYPE));
        }
        checkRequired(properties, CHANNEL_TYPE);
        checkAllowed(ALLOWED_CHANNELS, properties.get(CHANNEL_TYPE));
    }

    public static Map<String, String> configure(String name,
                                                Map<String, String> properties) throws FlumeException {
        validate(name, properties);
        // we are going to modify the properties as we parse the config
        properties = new HashMap<String, String>(properties);

        if(!properties.containsKey(SOURCE_TYPE) || SOURCE_TYPE_AVRO.
                equalsIgnoreCase(properties.get(SOURCE_TYPE))) {
            properties.put(SOURCE_TYPE, SOURCE_TYPE_AVRO);
        }
        String sourceName = "src-" + name;
        String channelName = "ch-" + name;

        Map<String, String> result = Maps.newHashMap();

        Set<String> userProvidedKeys;

        result.put(join(name, BasicConfigurationConstants.CONFIG_SOURCES),
                sourceName);
        result.put(join(name, BasicConfigurationConstants.CONFIG_CHANNELS),
                channelName);

        result.put(join(name,
                BasicConfigurationConstants.CONFIG_SOURCES, sourceName,
                BasicConfigurationConstants.CONFIG_CHANNELS), channelName);

        userProvidedKeys = new HashSet<String>(properties.keySet());
        for(String key : userProvidedKeys) {
            String value = properties.get(key);
            if(key.startsWith(SOURCE_PREFIX)) {
                key = key.replaceFirst(SOURCE, sourceName);
                result.put(join(name,
                        BasicConfigurationConstants.CONFIG_SOURCES, key), value);
            } else if(key.startsWith(CHANNEL_PREFIX)) {
                key = key.replaceFirst(CHANNEL, channelName);
                result.put(join(name,
                        BasicConfigurationConstants.CONFIG_CHANNELS, key), value);
            } else {
                throw new FlumeException("Unknown/Unsupported configuration " + key);
            }
        }
        System.out.println("result:" + result.toString());
        return result;
    }
    private static void checkAllowed(String[] allowedTypes, String type) {
        boolean isAllowed = false;
        type = type.trim();
        for(String allowedType : allowedTypes) {
            if(allowedType.equalsIgnoreCase(type)) {
              isAllowed = true;
              break;
            }
        }
        if(!isAllowed) {
            throw new FlumeException("Component type of " + type + " is not in correct types of "
                  + Arrays.toString(allowedTypes));
        }
    }
    private static void checkRequired(Map<String, String> properties,
                                      String name) {
        System.out.println(name + ":" + properties.toString());
        if(!properties.containsKey(name)) {
            throw new FlumeException("Parameter not found " + name);
        }
    }

    private static String join(String... parts) {
        return JOINER.join(parts);
    }

    private StormEmbeddedAgentConfiguration() {}
}
