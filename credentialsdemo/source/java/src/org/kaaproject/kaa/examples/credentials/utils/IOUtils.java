/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.examples.credentials.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Maksym Liashenko
 */
public class IOUtils {

    private static final Logger LOG = LoggerFactory.getLogger(IOUtils.class);

    public static String getUserInput() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String userInput = null;
        try {
            userInput = br.readLine();
        } catch (IOException e) {
            LOG.error("IOException has occurred: " + e.getMessage());
        }
        return userInput;
    }

    public static void readSymbol() {
        try {
            System.in.read();
        } catch (IOException e) {
            LOG.error("IOException has occurred: " + e.getMessage());
        }
    }
}
