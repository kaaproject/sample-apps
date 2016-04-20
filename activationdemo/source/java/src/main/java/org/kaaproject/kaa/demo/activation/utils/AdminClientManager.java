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

package org.kaaproject.kaa.demo.activation.utils;

import org.kaaproject.kaa.common.dto.*;
import org.kaaproject.kaa.common.dto.admin.AuthResultDto;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AdminClientManager {

    private static final Logger LOG = LoggerFactory.getLogger(AdminClientManager.class);

    private static final String TENANT_DEV_USERNAME = "devuser";
    private static final String TENANT_DEV_PASSWORD = "devuser123";
    private static final String DEFAULT_LIMIT = "20";
    private static final String DEFAULT_OFFSET = "0";
    private static final int KAA_PORT = 8080;

    private AdminClient adminClient;
    private static AdminClientManager instance;

    private AdminClientManager(String host, int port) {
        adminClient = new AdminClient(host, port);
    }

    public static void init(String host) {
        init(host, KAA_PORT);
    }

    public static void init(String host, int port) {
        instance = new AdminClientManager(host, port);
    }

    public static AdminClientManager instance() {
        if (instance == null) {
            LOG.error("Admin client was not initialized");
        }
        return instance;
    }

    /**
     * Do authorization check
     *
     * @return true if user is authorized otherwise false
     */
    public boolean checkAuth() {
        AuthResultDto.Result authResult = null;
        try {
            authResult = adminClient.checkAuth().getAuthResult();
        } catch (Exception e) {
            LOG.error("Exception has occurred: " + e.getMessage());
        }
        return authResult == AuthResultDto.Result.OK;
    }

    /**
     * Checks authorization and log in
     */
    public void checkAuthorizationAndLogin() {
        if (!checkAuth()) {
            adminClient.login(TENANT_DEV_USERNAME, TENANT_DEV_PASSWORD);
        }
    }

    /**
     * Update server profile by endpoint profile key
     *
     * @param endpointProfileKey
     *            the endpointProfileKey
     * @param serverProfileVersion
     *            the server profile version
     * @param serverProfileBody
     *            the server profile body
     * @return the endpoint profile object
     */
    public EndpointProfileDto updateServerProfile(String endpointProfileKey, int serverProfileVersion, String serverProfileBody) {
        checkAuthorizationAndLogin();

        EndpointProfileDto endpointProfile = null;
        try {
            endpointProfile = adminClient.updateServerProfile(endpointProfileKey, serverProfileVersion, serverProfileBody);
        } catch (Exception e) {
            LOG.error("Exception has occurred: " + e.getMessage());
        }
        return endpointProfile;
    }

    /**
     * Get application object by specified application name
     *
     * @param applicationName
     *            the application name
     * @return the application object
     */
    public ApplicationDto getApplicationByName(String applicationName) {
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
     * Get all endpoint groups associated with given application Token
     * 
     * @param applicationToken
     *            the application Token
     * @return list of endpoint groups
     */
    public List<EndpointGroupDto> getEndpointGroups(String applicationToken) {
        checkAuthorizationAndLogin();

        List<EndpointGroupDto> endpointGroups = null;
        try {
            endpointGroups = adminClient.getEndpointGroupsByAppToken(applicationToken);
        } catch (Exception e) {
            LOG.error("Exception has occurred: " + e.getMessage());
        }
        return endpointGroups;
    }

    /**
     * Get all endpoint profiles associated with given endpoint group Id
     *
     * @param endpointGroupId
     *            the endpoint group id
     * @return endpoint profile page object
     */
    public EndpointProfilesPageDto getEndpointProfileByEndpointGroupId(String endpointGroupId) {
        checkAuthorizationAndLogin();

        PageLinkDto pageLink = new PageLinkDto(endpointGroupId, DEFAULT_LIMIT, DEFAULT_OFFSET);
        EndpointProfilesPageDto endpointProfile = null;
        try {
            endpointProfile = adminClient.getEndpointProfileByEndpointGroupId(pageLink);
        } catch (Exception e) {
            LOG.error("Exception has occurred: " + e.getMessage());
        }
        return endpointProfile;
    }

    /**
     * Get all endpoint groups associated with given application
     *
     * @param applicationName
     *            the application name
     * @return list of endpoint groups
     */
    public List<EndpointGroupDto> getEndpointGroupsByApplicationName(String applicationName) {
        ApplicationDto applicationDto = getApplicationByName(applicationName);
        if (applicationDto == null) {
            LOG.error("There is no application with {} name", applicationName);
            return null;
        }

        return getEndpointGroups(applicationDto.getApplicationToken());
    }

    /**
     * Get all endpoint profiles associated with list of endpoint groups
     *
     * @param endpointGroups
     *            list of endpoint groups
     * @return map of endpoint profiles
     */
    public Map<String, EndpointProfileDto> getEndpointProfiles(List<EndpointGroupDto> endpointGroups) {
        Map<String, EndpointProfileDto> endpointProfiles = new LinkedHashMap<>();
        for (EndpointGroupDto endpointGroup : endpointGroups) {
            EndpointProfilesPageDto endpointProfilesDto = getEndpointProfileByEndpointGroupId(endpointGroup.getId());
            if (endpointProfilesDto == null) {
                continue;
            }
            for (EndpointProfileDto endpointProfile : endpointProfilesDto.getEndpointProfiles()) {
                endpointProfiles.put(endpointProfile.getId(), endpointProfile);
            }
        }

        return endpointProfiles;
    }

}
