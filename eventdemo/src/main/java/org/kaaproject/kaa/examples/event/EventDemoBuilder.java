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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KaaDemoBuilder
public class EventDemoBuilder extends AbstractDemoBuilder {

    private static final Logger logger = LoggerFactory.getLogger(EventDemoBuilder.class);

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

        EventClassFamilyDto chatMessageEventClassFamily = new EventClassFamilyDto();
        chatMessageEventClassFamily.setName("Chat Message Event Class Family");
        chatMessageEventClassFamily.setNamespace("org.kaaproject.kaa.examples.event");
        chatMessageEventClassFamily.setClassName("chatMessageEventClassFamily");
        
        loginTenantDeveloper(client);

        chatMessageEventClassFamily = client.editEventClassFamily(chatMessageEventClassFamily);

        addEventClassFamilyVersion(chatMessageEventClassFamily, client, eventApplication.getTenantId(), "chat_event_class_message.avsc");

        sdkProfileDto.setApplicationId(eventApplication.getId());
        sdkProfileDto.setApplicationToken(eventApplication.getApplicationToken());
        sdkProfileDto.setProfileSchemaVersion(0);
        sdkProfileDto.setConfigurationSchemaVersion(1);
        sdkProfileDto.setNotificationSchemaVersion(1);
        sdkProfileDto.setLogSchemaVersion(1);

        loginTenantDeveloper(client);

        ApplicationEventFamilyMapDto chatMessageAefMap = mapEventClassFamily(client, eventApplication, chatMessageEventClassFamily);
        List<String> aefMapIds = new ArrayList<>();
        aefMapIds.add(chatMessageAefMap.getId());
        
        EventClassFamilyDto chatEventEventClassFamily = new EventClassFamilyDto();
        chatEventEventClassFamily.setName("Chat Event Class Family");
        chatEventEventClassFamily.setNamespace("org.kaaproject.kaa.examples.event");
        chatEventEventClassFamily.setClassName("chatEventEventClassFamily");
        chatEventEventClassFamily = client.editEventClassFamily(chatEventEventClassFamily);

        addEventClassFamilyVersion(chatEventEventClassFamily, client, eventApplication.getTenantId(), "chat_event_class_event.avsc");
        
        loginTenantDeveloper(client);
        
        ApplicationEventFamilyMapDto chatEventAefMap = mapEventClassFamily(client, eventApplication, chatEventEventClassFamily);
        aefMapIds.add(chatEventAefMap.getId());
        
        sdkProfileDto.setAefMapIds(aefMapIds);

        TrustfulVerifierConfig trustfulVerifierConfig = new TrustfulVerifierConfig();
        UserVerifierDto trustfulUserVerifier = new UserVerifierDto();
        trustfulUserVerifier.setApplicationId(eventApplication.getId());
        trustfulUserVerifier.setName("Trustful verifier");
        trustfulUserVerifier.setPluginClassName(trustfulVerifierConfig.getPluginClassName());
        trustfulUserVerifier.setPluginTypeName(trustfulVerifierConfig.getPluginTypeName());
        RawSchema rawSchema = new RawSchema(trustfulVerifierConfig.getPluginConfigSchema().toString());
        DefaultRecordGenerationAlgorithm<RawData> algotithm =
                new DefaultRecordGenerationAlgorithmImpl<>(rawSchema, new RawDataFactory());
        RawData rawData = algotithm.getRootData();
        trustfulUserVerifier.setJsonConfiguration(rawData.getRawData());
        trustfulUserVerifier = client.editUserVerifierDto(trustfulUserVerifier);
        sdkProfileDto.setDefaultVerifierToken(trustfulUserVerifier.getVerifierToken());

        logger.info("Finished loading 'Event Demo Application' data.");
    }

}
