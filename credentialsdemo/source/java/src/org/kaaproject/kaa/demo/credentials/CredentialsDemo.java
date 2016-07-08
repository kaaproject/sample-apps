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

package org.kaaproject.kaa.demo.credentials;

import org.kaaproject.kaa.demo.credentials.kaa.KaaAdminManager;
import org.kaaproject.kaa.demo.credentials.kaa.KaaClientManager;
import org.kaaproject.kaa.demo.credentials.utils.IOUtils;
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
        LOG.info("Credentials demo started");
        if (args.length < 1) {
            LOG.info("Invalid parameters");
            LOG.info("Possible options:");
            LOG.info(" java -jar CredentialsDemo.jar client");
            LOG.info(" java -jar CredentialsDemo.jar admin");
            return;

        }
        String mode = args[0];
        switch (mode) {
            case "admin":

                inputAdminMenu();
            case "client":
                inputClientMenu();
                break;
            default:
                LOG.info("Invalid parameters. Please specify 'client' or 'admin'");

        }
    }

    private static void inputClientMenu() {
        KaaClientManager manager = new KaaClientManager();
        manager.start();


        IOUtils.readSymbol();
        /*
         * Stop the Kaa client and connect it to the Kaa server.
         */
        manager.stop();
    }

    private static void inputAdminMenu() {
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
                    LOG.info("Your credential sttaus - " + manager.getCredentialsStatus());
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

//                if (args.length < 2) {
//                }
//                if (args.length == 2) {
//                    AdminClientManager.init(args[1], AdminClientManager.UserType.TENANT_ADMIN);
//                } else if (args.length == 3) {
//                    AdminClientManager.init(args[1], Integer.valueOf(args[2]), AdminClientManager.UserType.TENANT_ADMIN);
//                } else {
//                    LOG.info("ip/host is not specified or address is invalid");
//                    return;
//                }