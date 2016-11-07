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
 */
public class CredentialsAdminDemo {
    private static final Logger LOG = LoggerFactory.getLogger(CredentialsAdminDemo.class);

    public static void main(String[] args) throws InterruptedException {
        LOG.info("Credentials demo: admin part started!");

        verifyInputData(args);

        LOG.info("Creating admin manager ...");
        KaaAdminManager adminManager = initAdminManager(args);

        LOG.info("Choose action by entering corresponding number:");

        while (true) {
            LOG.info("\n1. Generate endpoint credentials\n" +
                        "2. Provision endpoint credentials using key files\n" +
                        "3. Provision endpoint credentials using public key string\n" +
                        "4. Revoke endpoint credentials\n" +
                        "5. Get credentials status\n" +
                        "6. Exit");
            switch (IOUtils.getUserInput()) {
                case "1":
                    adminManager.generateKeys();
                    break;
                case "2":
                    adminManager.provisionKeys();
                    break;
                case "3":
                    adminManager.provisionWithKeyString();
                    break;
                case "4":
                    adminManager.revokeCredentials();
                    break;
                case "5":
                    adminManager.checkCredentialsStatus();
                    break;
                case "6":
                    LOG.info("Going to \"Exit\". Have a good day!");
                    System.exit(0);
                    break;
                default:
                    LOG.info("You input not readable symbol. Please, input again.");
            }
        }
    }

    private static void verifyInputData(String[] args) {
        System.out.println(args.length);
        if (!(args.length == 1 || args.length == 3)) {
            LOG.info("We need more info about your system. Please provide it.");
            LOG.info("Possible options:");
            LOG.info(" java -jar JCredentialsAdminDemo.jar sandboxIp");
            LOG.info(" java -jar JCredentialsAdminDemo.jar sandboxIp tenantAdminUsername tenantAdminPassword");
            System.exit(0);
        }

        if (!IOUtils.validateIp(args[0])) {
            LOG.info("Your ip isn't valid! Please, check it and input again!");
            System.exit(0);
        }
    }

    private static KaaAdminManager initAdminManager(String[] args) {
        String ip = args[0];
        KaaAdminManager manager = new KaaAdminManager(ip);
        if (args.length == 3) {
            manager.tenantAdminUsername = args[1];
            manager.tenantAdminPassword = args[2];
        }
        return manager;
    }
}
