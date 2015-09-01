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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.admin.SdkPropertiesDto;
import org.kaaproject.kaa.common.dto.event.*;
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

    private static final String GPIO_MASTER_ID = "gpio_master";
    private static final String GPIO_SLAVE_ID = "gpio_slave";

    private static final String REMOTE_CONTROL_ECF_NAME = "Remote Control Event Class Family";

    private static Map<String, ApplicationEventAction> defaultMasterAefMap =
            new HashMap<>();
    static {
        defaultMasterAefMap.put("org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoRequest", ApplicationEventAction.SOURCE);
        defaultMasterAefMap.put("org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoResponse", ApplicationEventAction.SINK);
        defaultMasterAefMap.put("org.kaaproject.kaa.examples.gpiocontrol.GPIOToggleRequest", ApplicationEventAction.SOURCE);
    }

    private static Map<String, ApplicationEventAction> defaultSlaveAefMap =
            new HashMap<>();
    static {
        defaultSlaveAefMap.put("org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoRequest", ApplicationEventAction.SINK);
        defaultSlaveAefMap.put("org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoResponse", ApplicationEventAction.SOURCE);
        defaultSlaveAefMap.put("org.kaaproject.kaa.examples.gpiocontrol.GPIOToggleRequest", ApplicationEventAction.SINK);
    }

    private Map<String, SdkPropertiesDto> projectsSdkMap = new HashMap<>();


    public GPIOcontrolDemoBuilder() {
        super("demo/gpiocontrol");
    }
    
    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {
        
        logger.info("Loading 'GPIO control Demo Application' data...");
        
        loginTenantAdmin(client);

        Map<String, EventClassFamilyDto> ecfMap = new HashMap<>();
        ecfMap.put(REMOTE_CONTROL_ECF_NAME,
                addEventClassFamily(client,
                        REMOTE_CONTROL_ECF_NAME,
                        "org.kaaproject.kaa.examples.gpiocontrol",
                        "RemoteControlECF",
                        "remoteControlECF.json"));

        ApplicationDto GPIOcontrolApplicationMaster = new ApplicationDto();
        GPIOcontrolApplicationMaster.setName("GPIO control master");
        GPIOcontrolApplicationMaster = client.editApplication(GPIOcontrolApplicationMaster);

        ApplicationDto GPIOcontrolApplicationSlave = new ApplicationDto();
        GPIOcontrolApplicationSlave.setName("GPIO control slave");
        GPIOcontrolApplicationSlave = client.editApplication(GPIOcontrolApplicationSlave);

        loginTenantDeveloper(client);

        configureMasterApp(client, GPIOcontrolApplicationMaster.getId(), ecfMap);
        configureSlaveApp(client, GPIOcontrolApplicationSlave.getId(), ecfMap);

        logger.info("Finished loading 'GPIO control Demo Application' data.");
    }

    private void configureMasterApp(AdminClient client,
                                         String applicationId,
                                         Map<String, EventClassFamilyDto> ecfMap) throws Exception {
        SdkPropertiesDto sdkProperties = createSdkProperties(client, applicationId, true);

        List<String> aefMapIds = new ArrayList<>();

        aefMapIds.add(createAefMap(client,
                applicationId,
                ecfMap.get(REMOTE_CONTROL_ECF_NAME),
                defaultMasterAefMap));

        sdkProperties.setAefMapIds(aefMapIds);

        projectsSdkMap.put(GPIO_MASTER_ID, sdkProperties);
    }

    private void configureSlaveApp(AdminClient client,
                                    String applicationId,
                                    Map<String, EventClassFamilyDto> ecfMap) throws Exception {
        SdkPropertiesDto sdkProperties = createSdkProperties(client, applicationId, true);

        List<String> aefMapIds = new ArrayList<>();

        aefMapIds.add(createAefMap(client,
                applicationId,
                ecfMap.get(REMOTE_CONTROL_ECF_NAME),
                defaultSlaveAefMap));

        sdkProperties.setAefMapIds(aefMapIds);

        projectsSdkMap.put(GPIO_SLAVE_ID, sdkProperties);
    }

    private EventClassFamilyDto addEventClassFamily(AdminClient client,
                                                    String name, String namespace, String className, String resource) throws Exception {
        EventClassFamilyDto eventClassFamily = new EventClassFamilyDto();
        eventClassFamily.setName(name);
        eventClassFamily.setNamespace(namespace);
        eventClassFamily.setClassName(className);
        eventClassFamily = client.editEventClassFamily(eventClassFamily);
        client.addEventClassFamilySchema(eventClassFamily.getId(), getResourcePath(resource));
        return eventClassFamily;
    }

    private SdkPropertiesDto createSdkProperties(AdminClient client,
                                                 String applicationId,
                                                 boolean createVerifier) throws Exception {
        SdkPropertiesDto sdkKey = new SdkPropertiesDto();
        sdkKey.setApplicationId(applicationId);
        sdkKey.setProfileSchemaVersion(1);
        sdkKey.setConfigurationSchemaVersion(1);
        sdkKey.setNotificationSchemaVersion(1);
        sdkKey.setLogSchemaVersion(1);
        if (createVerifier) {
            sdkKey.setDefaultVerifierToken(createTrustfulVerifier(client, applicationId));
        }
        return sdkKey;
    }

    private String createTrustfulVerifier(AdminClient client, String applicationId) throws Exception {
        TrustfulVerifierConfig trustfulVerifierConfig = new TrustfulVerifierConfig();
        UserVerifierDto trustfulUserVerifier = new UserVerifierDto();
        trustfulUserVerifier.setApplicationId(applicationId);
        trustfulUserVerifier.setName("Trustful verifier");
        trustfulUserVerifier.setPluginClassName(trustfulVerifierConfig.getPluginClassName());
        trustfulUserVerifier.setPluginTypeName(trustfulVerifierConfig.getPluginTypeName());
        RawSchema rawSchema = new RawSchema(trustfulVerifierConfig.getPluginConfigSchema().toString());
        DefaultRecordGenerationAlgorithm<RawData> algotithm =
                new DefaultRecordGenerationAlgorithmImpl<>(rawSchema, new RawDataFactory());
        RawData rawData = algotithm.getRootData();
        trustfulUserVerifier.setJsonConfiguration(rawData.getRawData());
        trustfulUserVerifier = client.editUserVerifierDto(trustfulUserVerifier);
        return trustfulUserVerifier.getVerifierToken();
    }

    private String createAefMap(AdminClient client,
                                String applicationId,
                                EventClassFamilyDto ecf,
                                Map<String, ApplicationEventAction> actionsMap) throws Exception {
        List<EventClassDto> eventClasses =
                client.getEventClassesByFamilyIdVersionAndType(ecf.getId(), 1, EventClassType.EVENT);

        ApplicationEventFamilyMapDto aefMap = new ApplicationEventFamilyMapDto();
        aefMap.setApplicationId(applicationId);
        aefMap.setEcfId(ecf.getId());
        aefMap.setEcfName(ecf.getName());
        aefMap.setVersion(1);

        List<ApplicationEventMapDto> eventMaps = new ArrayList<>(eventClasses.size());
        for (EventClassDto eventClass : eventClasses) {
            ApplicationEventMapDto eventMap = new ApplicationEventMapDto();
            eventMap.setEventClassId(eventClass.getId());
            eventMap.setFqn(eventClass.getFqn());
            eventMap.setAction(actionsMap.get(eventClass.getFqn()));
            eventMaps.add(eventMap);
        }

        aefMap.setEventMaps(eventMaps);
        aefMap = client.editApplicationEventFamilyMap(aefMap);

        return aefMap.getId();
    }

    @Override
    protected boolean isMultiApplicationProject() {
        return true;
    }

    @Override
    protected Map<String, SdkPropertiesDto> getProjectsSdkMap() {
        return projectsSdkMap;
    }


}
