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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.regex.Pattern;

public class IOUtils {

    public static String SANDBOX_IP_ADDRESS = "provided by user";
    public static final int DEFAULT_KAA_PORT = 8080;

    public static final String PRIVATE_KEY_LOCATION = "key.private";
    public static final String PUBLIC_KEY_LOCATION = "key.public";

    public static String TENANT_ADMIN_USERNAME = "admin";
    public static String TENANT_ADMIN_PASSWORD = "admin123";

    public final static String APPLICATION_NAME = "Credentials demo";

    private static final Logger LOG = LoggerFactory.getLogger(IOUtils.class);

    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    /**
     * Validate user input ip for avoid errors
     * @param ip - user ip
     * @return if validated
     */
    public static boolean validateIp(String ip) {
        if (PATTERN.matcher(ip).matches()) {
            LOG.info("Ip is valid");
            return true;
        } else {
            LOG.info("Ip isn't valid. Please, provide it again.");
            return false;
        }
    }

    /**
     * Read from console user string
     * @return string, that  user input in console
     */
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

    /**
     * Saves public and private keys to specified files.
     *
     * @param keyPair        the key pair
     * @param privateKeyFile the private key file
     * @param publicKeyFile  the public key file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void saveKeyPair(KeyPair keyPair, String privateKeyFile, String publicKeyFile) throws IOException {
        File privateFile = makeDirs(privateKeyFile);
        File publicFile = makeDirs(publicKeyFile);
        OutputStream privateKeyOutput = null;
        OutputStream publicKeyOutput = null;
        privateKeyOutput = new FileOutputStream(privateFile);
        publicKeyOutput = new FileOutputStream(publicFile);
        saveKeyPair(keyPair, privateKeyOutput, publicKeyOutput);
    }

    /**
     * Saves public and private keys to specified streams.
     *
     * @param keyPair          the key pair
     * @param privateKeyOutput the private key output stream
     * @param publicKeyOutput  the public key output stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void saveKeyPair(KeyPair keyPair, OutputStream privateKeyOutput, OutputStream publicKeyOutput) throws IOException {
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // Store Public Key.
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
                publicKey.getEncoded());
        publicKeyOutput.write(x509EncodedKeySpec.getEncoded());

        // Store Private Key.
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                privateKey.getEncoded());
        privateKeyOutput.write(pkcs8EncodedKeySpec.getEncoded());
    }

    private static File makeDirs(String privateKeyFile) {
        File privateFile = new File(privateKeyFile);
        if (privateFile.getParentFile() != null && !privateFile.getParentFile().exists() && !privateFile.getParentFile().mkdirs()) {
            LOG.warn("Failed to create required directories: {}", privateFile.getParentFile().getAbsolutePath());
        }

        return privateFile;
    }

    public static KeyPair generateKeyPair(String privateKeyLocation, String publicKeyLocation) {
        try {
            KeyPair e = generateKeyPair();
            saveKeyPair(e, (String) privateKeyLocation, (String) publicKeyLocation);
            return e;
        } catch (Exception var3) {
            LOG.error("Error generating client key pair", var3);
            return null;
        }
    }

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator clientKeyGen = KeyPairGenerator.getInstance("RSA");
        clientKeyGen.initialize(2048);
        return clientKeyGen.genKeyPair();
    }

    /**
     * Gets the public key from input stream.
     *
     * @param input the input stream
     * @return the public
     * @throws IOException                       the i/o exception
     * @throws java.security.InvalidKeyException invalid key exception
     */
    public static PublicKey getPublic(InputStream input) throws IOException, InvalidKeyException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        byte[] keyBytes = output.toByteArray();

        return getPublic(keyBytes);
    }

    /**
     * Gets the public key from bytes.
     *
     * @param keyBytes the key bytes
     * @return the public
     * @throws InvalidKeyException invalid key exception
     */
    public static PublicKey getPublic(byte[] keyBytes) throws InvalidKeyException {
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new InvalidKeyException(e);
        }
    }

    /**
     * Gets the private key from input stream.
     *
     * @param input the input stream
     * @return the private
     * @throws IOException         the i/o exception
     * @throws InvalidKeyException invalid key exception
     */
    public static PrivateKey getPrivate(InputStream input) throws IOException, InvalidKeyException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        byte[] keyBytes = output.toByteArray();

        return getPrivate(keyBytes);
    }

    /**
     * Gets the private key from bytes.
     *
     * @param keyBytes the key bytes
     * @return the private
     * @throws InvalidKeyException invalid key exception
     */
    public static PrivateKey getPrivate(byte[] keyBytes) throws InvalidKeyException {
        try {
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new InvalidKeyException(e);
        }
    }
}
