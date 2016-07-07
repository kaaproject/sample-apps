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

package org.kaaproject.kaa.demo.activation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * A demo application class that use the Kaa activation API.
 */
public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        LOG.info("Configuration demo started");

        // we need folder to save properties in it. In other case we can't create more then one KaaClient
        if (!Files.exists(Paths.get(KaaManager.PROPERTIES_OUT_DIR))) {
            LOG.error("No resource folder. Configuration demo cancelled");
            return;
        }

        KaaManager manager = new KaaManager();
        for (int i = 0; i < KaaManager.KAA_CLIENT_NUMBER; i++) {
            try {
                createResDir(i);
                manager.startKaaClient(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        LOG.info("--= Press any key to exit =--");
        try {
            System.in.read();
        } catch (IOException e) {
            LOG.error("IOException was caught - ", e);
        }

        // Stop the Kaa client and release all the resources which were in use.
        try {
            deleteResDir();
            manager.stopKaaClients();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOG.info("Configuration demo stopped");
    }

    private static void createResDir(int index) throws IOException {
        Path path = Paths.get(KaaManager.PROPERTIES_OUT_DIR + KaaManager.KAA_PROPERTIES_DIR_PREFIX + index);
        Files.createDirectory(path);
    }

    private static void deleteResDir() throws IOException {
        final Path directory = Paths.get(KaaManager.PROPERTIES_OUT_DIR);
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (dir != directory)
                    Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

        });
    }

}