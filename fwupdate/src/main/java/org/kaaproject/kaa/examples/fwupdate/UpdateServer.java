package org.kaaproject.kaa.examples.fwupdate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.CRC32;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

public class UpdateServer {

    private static final String SERVER_PROPERTIES = "server.properties";

    public static void main(String[] args) throws Exception {
        final String serverHost;
        final int serverPort;
        final String updatesDir;

        try {
            Properties properties = new Properties();
            InputStream is = UpdateServer.class.getClassLoader().getResourceAsStream(SERVER_PROPERTIES);
            properties.load(is);
            serverHost = properties.getProperty("server.host");
            serverPort = Integer.parseInt(properties.getProperty("server.port"));
            updatesDir = properties.getProperty("updates.dir");
            is.close();
        } catch (Exception e) {
            System.err.println("Failed to load server properties from classpath resource: " + SERVER_PROPERTIES);
            throw e;
        }

        final List<FWUpdate> updates = new ArrayList<FWUpdate>();

        System.out.println("Initializing server with host: " + serverHost + " and " + serverPort);
        System.out.println("Scanning updates directory: " + updatesDir);
        File f = new File(updatesDir);
        if (!f.exists()) {
            throw new IllegalArgumentException("Updates directory does not exist!");
        } else if (!f.isDirectory()) {
            throw new IllegalArgumentException("Updates directory is not a directory!");
        } else {
            File[] files = f.listFiles();
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".bin")) {
                    FWUpdate update = new FWUpdate(Paths.get(file.toURI()));
                    updates.add(update);
                    System.out.println("Update file scanned: " + update);
                }
            }
            if (updates.size() == 0) {
                throw new IllegalArgumentException("Updates directory does not contain any *.bin files!");
            }
        }

        HttpServer server = new HttpServer();
        NetworkListener listener = new NetworkListener("fwupdate", serverHost, serverPort);
        listener.setChunkingEnabled(true);
        server.addListener(listener);

        server.getServerConfiguration().addHttpHandler(new HttpHandler() {
            public void service(Request request, Response response) throws Exception {
                for (final FWUpdate update : updates) {
                    response.getWriter().write(update.toString());
                    response.getWriter().write(System.lineSeparator());
                }
            }
        }, "/meta");

        for (final FWUpdate update : updates) {
            server.getServerConfiguration().addHttpHandler(new HttpHandler() {
                public void service(Request request, Response response) throws Exception {
                    response.setContentType("application/octet-stream");
                    response.getOutputStream().write(Files.readAllBytes(update.getFilePath()));
                }
            }, "/" + update.getFileName());
            
            server.getServerConfiguration().addHttpHandler(new HttpHandler() {
                public void service(Request request, Response response) throws Exception {
                    response.getWriter().write(update.toString());
                }
            }, "/" + update.getFileName() + "/meta");
        }
        try {
            server.start();
            System.out.println("Press any key to stop the server...");
            System.in.read();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private static class FWUpdate {
        private final long size;
        private final long checksum;
        private final Path filePath;

        public FWUpdate(Path filePath) throws IOException {
            this.filePath = filePath;
            byte[] fwBytes = Files.readAllBytes(filePath);
            CRC32 crc = new CRC32();
            crc.reset();
            crc.update(fwBytes);
            checksum = crc.getValue();
            size = fwBytes.length;
        }

        public Path getFilePath() {
            return filePath;
        }

        public String getFileName() {
            return filePath.getFileName().toString().replace(".bin", "");
        }

        @Override
        public String toString() {
            return "FWUpdate [size=" + size + ", checksum=" + checksum + ", file=" + filePath.getFileName() + "]";
        }

    }

}
