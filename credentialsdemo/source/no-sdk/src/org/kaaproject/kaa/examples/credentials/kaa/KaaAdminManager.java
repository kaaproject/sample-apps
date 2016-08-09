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
import org.kaaproject.kaa.examples.credentials.utils.CredentialsConstants;
import org.kaaproject.kaa.examples.credentials.utils.IOUtils;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * @author Maksym Liashenko
 */
public class KaaAdminManager {

    private static final Logger LOG = LoggerFactory.getLogger(KaaAdminManager.class);


    private AdminClient adminClient;

    public KaaAdminManager() {
        this.adminClient = new AdminClient(CredentialsConstants.SANDBOX_IP_ADDRESS, CredentialsConstants.DEFAULT_KAA_PORT);
    }

    public void generateKeys() {
        LOG.info("Going to generate credentials...");
        try {
            getOrCreateKeyPair();

            LOG.info("Success generation!");
        } catch (IOException e) {
            LOG.error("Error in generateKeys", e);
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            LOG.error("Error in generateKeys", e);
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error in generateKeys", e);
            e.printStackTrace();
        }
    }

    public void provisionKeys() {
        LOG.info("Going to provision credentials...");
        try {
            provideCredentials(CredentialsConstants.APPLICATION_NAME, getOrCreateKeyPair().getPublic().getEncoded());

            LOG.info("Credentials is provisioning. Success!");
        } catch (IOException e) {
            LOG.error("Error in provisionKeys", e);
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            LOG.error("Error in provisionKeys", e);
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error in provisionKeys", e);
            e.printStackTrace();
        }
    }

    public void revokeCredentials() {
        LOG.info("Enter ID of credentials that needs to be revoked:");
        String credentialsId = IOUtils.getUserInput();
        LOG.info("Going to revoke credentials");
        revokeCredentials(CredentialsConstants.APPLICATION_NAME, credentialsId.trim());
    }

    private void provideCredentials(String applicationName, byte[] publicKey) {
        CredentialsDto credentialsDto = adminClient.provisionCredentials(getApplicationByName(applicationName)
                .getApplicationToken(), publicKey);
        LOG.debug("APP TOKEN: {}", getApplicationByName(applicationName).getApplicationToken());
        LOG.info("Credentials with ID={} are now in status: {}", credentialsDto.getId(), credentialsDto.getStatus());
    }

    private void revokeCredentials(String applicationName, String credentialsId) {
        LOG.debug("APP TOKEN: {}", getApplicationByName(applicationName).getApplicationToken());
        adminClient.revokeCredentials(getApplicationByName(applicationName).getApplicationToken(), credentialsId);
        LOG.info("Credentials revoked. Success!");
    }

    public CredentialsStatus getCredentialsStatus() {
        LOG.info("Enter ID of credentials for what needs to get status:");
        String credentialsId = IOUtils.getUserInput();
        LOG.info("Get status!");
        return getCredentialsStatus(CredentialsConstants.APPLICATION_NAME, credentialsId.trim());
    }

    private CredentialsStatus getCredentialsStatus(String applicationName, String credentialsId) {
        return adminClient.getCredentialsStatus(getApplicationByName(applicationName).getApplicationToken(), credentialsId);
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
            adminClient.login(CredentialsConstants.TENANT_ADMIN_USERNAME, CredentialsConstants.TENANT_ADMIN_PASSWORD);
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


    private KeyPair getOrCreateKeyPair() throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        if (!isExists()) {
            return IOUtils.generateKeyPair(CredentialsConstants.PRIVATE_KEY_LOCATION, CredentialsConstants.PUBLIC_KEY_LOCATION);
        } else {
            return getKeyPair();
        }
    }

    private boolean isExists() {
        return new File(CredentialsConstants.PRIVATE_KEY_LOCATION).exists() &&
                new File(CredentialsConstants.PUBLIC_KEY_LOCATION).exists();
    }

    private KeyPair getKeyPair() throws IOException, InvalidKeyException {
        InputStream publicKeyInput = new FileInputStream(new File(CredentialsConstants.PUBLIC_KEY_LOCATION));
        InputStream privateKeyInput = new FileInputStream(new File(CredentialsConstants.PRIVATE_KEY_LOCATION));

        PublicKey publicKey = IOUtils.getPublic(publicKeyInput);
        PrivateKey privateKey = IOUtils.getPrivate(privateKeyInput);

        return new KeyPair(publicKey, privateKey);
    }
}
