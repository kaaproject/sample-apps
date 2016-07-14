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

package org.kaaproject.kaa.examples.credentials;

import org.kaaproject.kaa.examples.credentials.kaa.KaaAdminManager;
import org.kaaproject.kaa.examples.credentials.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A demo application that shows how to use the Kaa credentials API.
 *
 * @author Maksym Liashenko
 */
public class CredentialsDemo {
    private static final Logger LOG = LoggerFactory.getLogger(CredentialsDemo.class);

    public static void main(String[] args) throws InterruptedException {
        LOG.info("Credentials demo: admin part started!");

        KaaAdminManager manager = new KaaAdminManager();

        LOG.info("Choose action by entering corresponding number:");
        while (true) {
            LOG.info("\n1. Generate endpoint credentials.\n" +
                    "2. Provision endpoint credentials.\n" +
                    "3. Revoke endpoint credentials\n" +
                    "4. Get credentials status\n" +
                    "5. Exit");
            switch (IOUtils.getUserInput()) {
                case "1":
                    LOG.info("You choose \"Generate endpoint credentials\".");
                    manager.generateKeys();
                    break;
                case "2":
                    LOG.info("You choose \"Provision endpoint credentials\".");
                    manager.provisionKeys();
                    break;
                case "3":
                    LOG.info("You choose \"Revoke endpoint credentials\".");
                    manager.revokeCredentials();
                    break;
                case "4":
                    LOG.info("You choose \"Get credentials status\".");
                    LOG.info("Your credential status - " + manager.getCredentialsStatus());
                    break;
                case "5":
                    LOG.info("You choose \"Exit\". Have a good day!");
                    System.exit(0);
                    break;
                default:
                    LOG.info("You input not readable symbol. Please, input again.");
            }
        }

    }
}
