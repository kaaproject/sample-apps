/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kaaproject.kaa.examples.gpiocontrol;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
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
public class GPIOcontrolDemoBuilder extends AbstractDemoBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(GPIOcontrolDemoBuilder.class);
    
    public GPIOcontrolDemoBuilder() {
        super("demo/gpiocontrol");
    }
    
    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {
        
        logger.info("Loading 'GPIO control Demo Application' data...");
        
        loginTenantAdmin(client);
        
        EventClassFamilyDto remoteControlECF = new EventClassFamilyDto();
        remoteControlECF.setName("Remote Control Event Class Family");
        remoteControlECF.setNamespace("org.kaaproject.kaa.examples.gpiocontrol");
        remoteControlECF.setClassName("RemoteControlECF");
        remoteControlECF = client.editEventClassFamily(remoteControlECF);
        client.addEventClassFamilySchema(remoteControlECF.getId(), getResourcePath("remoteControlECF.json"));
        
        ApplicationDto GPIOcontrolApplication = new ApplicationDto();
        GPIOcontrolApplication.setName("GPIO control");
        GPIOcontrolApplication = client.editApplication(GPIOcontrolApplication);
               
        sdkPropertiesDto.setApplicationId(GPIOcontrolApplication.getId());
        sdkPropertiesDto.setApplicationToken(GPIOcontrolApplication.getApplicationToken());
        sdkPropertiesDto.setProfileSchemaVersion(1);
        sdkPropertiesDto.setConfigurationSchemaVersion(1);
        sdkPropertiesDto.setNotificationSchemaVersion(1);
        sdkPropertiesDto.setLogSchemaVersion(1);

        loginTenantDeveloper(client);
        
        ApplicationEventFamilyMapDto remoteControlAefMap = mapEventClassFamily(client, GPIOcontrolApplication, remoteControlECF);

        List<String> aefMapIds = new ArrayList<>();
        aefMapIds.add(remoteControlAefMap.getId());
        sdkPropertiesDto.setAefMapIds(aefMapIds);
        
        TrustfulVerifierConfig trustfulVerifierConfig = new TrustfulVerifierConfig();        
        UserVerifierDto trustfulUserVerifier = new UserVerifierDto();
        trustfulUserVerifier.setApplicationId(GPIOcontrolApplication.getId());
        trustfulUserVerifier.setName("Trustful verifier");
        trustfulUserVerifier.setPluginClassName(trustfulVerifierConfig.getPluginClassName());
        trustfulUserVerifier.setPluginTypeName(trustfulVerifierConfig.getPluginTypeName());
        RawSchema rawSchema = new RawSchema(trustfulVerifierConfig.getPluginConfigSchema().toString());
        DefaultRecordGenerationAlgorithm<RawData> algotithm = 
                    new DefaultRecordGenerationAlgorithmImpl<>(rawSchema, new RawDataFactory());
        RawData rawData = algotithm.getRootData();
        trustfulUserVerifier.setJsonConfiguration(rawData.getRawData());        
        trustfulUserVerifier = client.editUserVerifierDto(trustfulUserVerifier);
        sdkPropertiesDto.setDefaultVerifierToken(trustfulUserVerifier.getVerifierToken());
        
        logger.info("Finished loading 'GPIO control Demo Application' data.");
    }

}
