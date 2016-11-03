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

package org.kaaproject.kaa.examples.credentials.kaa;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.admin.AuthResultDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.examples.credentials.utils.IOUtils;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

public class KaaAdminManager {

    private static final Logger LOG = LoggerFactory.getLogger(KaaAdminManager.class);

    private static final int DEFAULT_KAA_PORT = 8080;
    private static final String PRIVATE_KEY_LOCATION = "key.private";
    private static final String PUBLIC_KEY_LOCATION = "key.public";
    private static final String APPLICATION_NAME = "Credentials demo";

    public String sandboxIpAddress = "provided by user";
    public String tenantAdminUsername = "admin";
    public String tenantAdminPassword = "admin123";

    private AdminClient adminClient;

    public KaaAdminManager(String sandboxIp) {
        this.adminClient = new AdminClient(sandboxIp, DEFAULT_KAA_PORT);
    }

    /**
     * Generate or get created RSA public/private key pair from folder named in *_KEY_LOCATION values
     */
    public void generateKeys() {
        LOG.info("Going to generate credentials...");
        try {
            getOrCreateKeyPair();

            LOG.info("Success generation!");
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            LOG.error("Error in generateKeys", e);
            e.printStackTrace();
        }
    }

    private KeyPair getOrCreateKeyPair() throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        if (!isKeysExists()) {
            return IOUtils.generateKeyPair(PRIVATE_KEY_LOCATION, PUBLIC_KEY_LOCATION);
        } else {
            return getKeyPair();
        }
    }

    /**
     * Provision keys on sandbox with needed REST method
     */
    public void provisionKeys() {
        LOG.info("Going to provision credentials...");
        try {
            PublicKey publicKey = getOrCreateKeyPair().getPublic();
            provideCredentials(APPLICATION_NAME, publicKey);
            LOG.info("Credentials is successfully provisioned!");
        } catch (NullPointerException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            LOG.error("Provision credentials with public key failed. Please check if keys already generated.");
            e.printStackTrace();
        }
    }

    /**
     *  Provision keys on sandbox with needed REST method using key-string
     */
    public void provisionWithKeyString() {
        LOG.info("Enter public key string of endpoint which credentials needs to be provisioned:");
        String keyString = IOUtils.getUserInput().trim();
        LOG.info("Going to provision credentials with public key...");
        try {

            // TODO: test with real iOS public key example
            byte[] keyBytes = Base64Utils.decodeFromString(keyString);
            PublicKey publicKey = IOUtils.getPublic(keyBytes);
            provideCredentials(APPLICATION_NAME, publicKey);

            LOG.info("Credentials is successfully provisioned!");
        } catch (Exception e) {
            LOG.error("Provision credentials with public key failed. Please check your public key.");
            e.printStackTrace();
        }
    }

    private void provideCredentials(String applicationName, PublicKey publicKey) {
        String appToken = getApplicationByName(applicationName).getApplicationToken();
        CredentialsDto credentialsDto = adminClient.provisionCredentials(appToken, publicKey.getEncoded());
        LOG.debug("APP TOKEN: {}", appToken);
        LOG.info("Credentials with ID = {} are now in status: {}", credentialsDto.getId(), credentialsDto.getStatus());
    }

    /**
     * Revoke user credentials on sandbox with needed REST methodgit
     */
    public void revokeCredentials() {
        LOG.info("Enter endpoint ID which credentials needs to be revoked:");
        String endpointId = IOUtils.getUserInput().trim();
        LOG.info("Revoking credentials...");
        try {
            revokeCredentials(APPLICATION_NAME, endpointId);
            LOG.info("Credentials for endpoint ID = {} are now in status: REVOKED", endpointId);
        } catch (Exception e) {
            LOG.error("Revoke credentials for endpoint ID = {} failed. Error: {}", endpointId, e.getMessage());
        }
    }

    private void revokeCredentials(String applicationName, String endpointId) {
        String applicationToken = getApplicationByName(applicationName).getApplicationToken();
        LOG.debug("APP TOKEN: {}", applicationToken);
        adminClient.revokeCredentials(applicationToken, endpointId);
    }

    /**
     * Check credentials status for getting information
     * @return credential status
     */
    public void checkCredentialsStatus() {
        LOG.info("Enter endpoint ID:");
        String endpointId = IOUtils.getUserInput().trim();
        LOG.info("Getting credentials status...");
        try {
            CredentialsStatus status = getCredentialsStatus(APPLICATION_NAME, endpointId);
            LOG.info("Credentials for endpoint ID = {} are now in status: {}", endpointId, status.toString());
        } catch (Exception e) {
            LOG.error("Get credentials status for endpoint ID = {} failed. Error: {}", endpointId, e.getMessage());
        }
    }

    private CredentialsStatus getCredentialsStatus(String applicationName, String endpointId) {
        ApplicationDto app = getApplicationByName(applicationName);
        String appToken =  app.getApplicationToken();
        return adminClient.getCredentialsStatus(appToken, endpointId);
    }

    /**
     * Get application object by specified application name
     *
     * @param applicationName the application name
     * @return the application object
     */
    private ApplicationDto getApplicationByName(String applicationName) {
        checkAuthorizationAndLogin();

        try {
            List<ApplicationDto> applications = adminClient.getApplications();
            for (ApplicationDto application : applications) {
                if (application.getName().trim().equals(applicationName)) {
                    return application;
                }
            }
        } catch (Exception e) {
            LOG.error("Exception has occurred: " + e.getMessage());
        }
        return null;
    }

    /**
     * Checks authorization and log in
     */
    private void checkAuthorizationAndLogin() {
        if (!checkAuth()) {
            adminClient.login(tenantAdminUsername, tenantAdminPassword);
        }
    }

    /**
     * Do authorization check
     *
     * @return true if user is authorized otherwise false
     */
    private boolean checkAuth() {
        AuthResultDto.Result authResult = null;
        try {
            authResult = adminClient.checkAuth().getAuthResult();
        } catch (Exception e) {
            LOG.error("Exception has occurred: " + e.getMessage());
        }
        return authResult == AuthResultDto.Result.OK;
    }

    private boolean isKeysExists() {
        return new File(PRIVATE_KEY_LOCATION).exists() &&
                new File(PUBLIC_KEY_LOCATION).exists();
    }

    private KeyPair getKeyPair() throws IOException, InvalidKeyException {
        InputStream publicKeyInput = new FileInputStream(new File(PUBLIC_KEY_LOCATION));
        InputStream privateKeyInput = new FileInputStream(new File(PRIVATE_KEY_LOCATION));

        PublicKey publicKey = IOUtils.getPublic(publicKeyInput);
        PrivateKey privateKey = IOUtils.getPrivate(privateKeyInput);

        return new KeyPair(publicKey, privateKey);
    }
}
