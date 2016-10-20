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

package org.kaaproject.kaa.examples.event;


import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyVersionDto;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.examples.common.AbstractDemoBuilder;
import org.kaaproject.kaa.examples.common.KaaDemoBuilder;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithmImpl;
import org.kaaproject.kaa.server.common.core.configuration.RawData;
import org.kaaproject.kaa.server.common.core.configuration.RawDataFactory;
import org.kaaproject.kaa.server.common.core.schema.RawSchema;
import org.kaaproject.kaa.server.verifiers.trustful.config.TrustfulVerifierConfig;
import org.kaaproject.kaa.server.verifiers.trustful.config.gen.TrustfulAvroConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KaaDemoBuilder
public class EventDemoBuilder extends AbstractDemoBuilder {

    private static final Logger logger = LoggerFactory.getLogger(EventDemoBuilder.class);

    private static final String EVENT_DEMO_ANDROID_ID = "event_demo_android";

    public EventDemoBuilder() {
        super("demo/event");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {
        logger.info("Loading 'Event Demo Application' data...");

        loginTenantAdmin(client);

        ApplicationDto eventApplication = new ApplicationDto();
        eventApplication.setName("Event demo");
        eventApplication = client.editApplication(eventApplication);

        EventClassFamilyDto thermoEventClassFamily = new EventClassFamilyDto();
        thermoEventClassFamily.setName("Chat Event Class Family");
        thermoEventClassFamily.setNamespace("org.kaaproject.kaa.examples.event");
        thermoEventClassFamily.setClassName("Chat");
        thermoEventClassFamily = client.editEventClassFamily(thermoEventClassFamily);

        addEventClassFamilyVersion(thermoEventClassFamily, client, eventApplication.getTenantId(), "chatEventClassFamily.json");

        sdkProfileDto.setApplicationId(eventApplication.getId());
        sdkProfileDto.setApplicationToken(eventApplication.getApplicationToken());
        sdkProfileDto.setProfileSchemaVersion(0);
        sdkProfileDto.setConfigurationSchemaVersion(1);
        sdkProfileDto.setNotificationSchemaVersion(1);
        sdkProfileDto.setLogSchemaVersion(1);

        loginTenantDeveloper(client);

        ApplicationEventFamilyMapDto thermoAefMap = mapEventClassFamily(client, eventApplication, thermoEventClassFamily);

        List<String> aefMapIds = new ArrayList<>();
        aefMapIds.add(thermoAefMap.getId());
        sdkProfileDto.setAefMapIds(aefMapIds);

        TrustfulVerifierConfig trustfulVerifierConfig = new TrustfulVerifierConfig();
        UserVerifierDto trustfulUserVerifier = new UserVerifierDto();
        trustfulUserVerifier.setApplicationId(eventApplication.getId());
        trustfulUserVerifier.setName("Trustful verifier");
        trustfulUserVerifier.setPluginClassName(trustfulVerifierConfig.getPluginClassName());
        trustfulUserVerifier.setPluginTypeName(trustfulVerifierConfig.getPluginTypeName());

        TrustfulAvroConfig trustfulAvroConfig = new TrustfulAvroConfig();
        trustfulUserVerifier.setJsonConfiguration(trustfulAvroConfig.toString());
        logger.info("Trustful config: {} ", trustfulAvroConfig.toString());
        trustfulUserVerifier = client.editUserVerifierDto(trustfulUserVerifier);
        sdkProfileDto.setDefaultVerifierToken(trustfulUserVerifier.getVerifierToken());

        logger.info("Finished loading 'Event Demo Application' data.");
    }

}