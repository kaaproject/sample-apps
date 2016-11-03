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

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.examples.common.AbstractDemoBuilder;
import org.kaaproject.kaa.examples.common.KaaDemoBuilder;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@KaaDemoBuilder
public class CredentialsDemoBuilder extends AbstractDemoBuilder {

    private static final Logger logger = LoggerFactory.getLogger(CredentialsDemoBuilder.class);

    public CredentialsDemoBuilder() {
        super("demo/credentials");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

        logger.info("Loading 'Credentials demo application' data...");

        loginTenantAdmin(client);

        ApplicationDto credentialsApplication = new ApplicationDto();
        credentialsApplication.setName("Credentials demo");
        credentialsApplication.setCredentialsServiceName("Internal");
        credentialsApplication = client.editApplication(credentialsApplication);


        sdkProfileDto.setApplicationId(credentialsApplication.getId());
        sdkProfileDto.setApplicationToken(credentialsApplication.getApplicationToken());
        sdkProfileDto.setNotificationSchemaVersion(1);
        sdkProfileDto.setProfileSchemaVersion(0);
        sdkProfileDto.setLogSchemaVersion(1);
        sdkProfileDto.setConfigurationSchemaVersion(1);

        loginTenantDeveloper(client);

        EndpointGroupDto baseEndpointGroup = null;
        List<EndpointGroupDto> endpointGroups = client.getEndpointGroupsByAppToken(credentialsApplication.getApplicationToken());
        if (endpointGroups.size() == 1 && endpointGroups.get(0).getWeight() == 0) {
            baseEndpointGroup = endpointGroups.get(0);
        }
        if (baseEndpointGroup == null) {
            throw new RuntimeException("Can't get default endpoint group for credentials application!");
        }

        logger.info("Finished loading 'Credentials demo application' data...");
    }

}
