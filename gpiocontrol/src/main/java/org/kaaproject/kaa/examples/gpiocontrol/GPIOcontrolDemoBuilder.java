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

package org.kaaproject.kaa.examples.gpiocontrol;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@KaaDemoBuilder
public class GPIOcontrolDemoBuilder extends AbstractDemoBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(GPIOcontrolDemoBuilder.class);

    private static final String GPIO_MASTER_ANDROID_ID = "gpiocontrol_demo_android_master";
    private static final String GPIO_MASTER_OBJC_ID = "gpiocontrol_demo_objc_master";
    private static final String GPIO_C_ID = "gpiocontrol_demo_c_slave";
    private static final String GPIO_CPP_ID = "gpiocontrol_demo_cpp_slave";
    private static final String GPIO_ARTIK5_ID = "gpiocontrol_demo_artik5_slave";

    private static final String REMOTE_CONTROL_ECF_NAME = "Remote Control Event Class Family";

    private static Map<String, ApplicationEventAction> defaultMasterAefMap =
            new HashMap<>();

    static {
        defaultMasterAefMap.put("org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoRequest", ApplicationEventAction.SOURCE);
        defaultMasterAefMap.put("org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoResponse", ApplicationEventAction.SINK);
        defaultMasterAefMap.put("org.kaaproject.kaa.examples.gpiocontrol.GpioToggleRequest", ApplicationEventAction.SOURCE);
    }

    private static Map<String, ApplicationEventAction> defaultSlaveAefMap =
            new HashMap<>();

    static {
        defaultSlaveAefMap.put("org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoRequest", ApplicationEventAction.SINK);
        defaultSlaveAefMap.put("org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoResponse", ApplicationEventAction.SOURCE);
        defaultSlaveAefMap.put("org.kaaproject.kaa.examples.gpiocontrol.GpioToggleRequest", ApplicationEventAction.SINK);
    }

    private Map<String, SdkProfileDto> projectsSdkMap = new HashMap<>();

    public GPIOcontrolDemoBuilder() {
        super("demo/gpiocontrol");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

        LOG.info("Loading 'GPIO control Demo Application' data...");

        loginTenantAdmin(client);

        ApplicationDto GPIOcontrolApplicationMaster = new ApplicationDto();
        GPIOcontrolApplicationMaster.setName("GPIO control master");
        GPIOcontrolApplicationMaster = client.editApplication(GPIOcontrolApplicationMaster);

        ApplicationDto GPIOcontrolApplicationSlave = new ApplicationDto();
        GPIOcontrolApplicationSlave.setName("GPIO control slave");
        GPIOcontrolApplicationSlave = client.editApplication(GPIOcontrolApplicationSlave);

        Map<String, EventClassFamilyDto> ecfMap = new HashMap<>();
        ecfMap.put(REMOTE_CONTROL_ECF_NAME,
                addEventClassFamily(client, GPIOcontrolApplicationMaster.getTenantId(),
                        REMOTE_CONTROL_ECF_NAME,
                        "org.kaaproject.kaa.examples.gpiocontrol",
                        "RemoteControlECF",
                        "remoteControlECF.json"));

        loginTenantDeveloper(client);

        configureMasterApp(client, GPIOcontrolApplicationMaster.getId(), GPIOcontrolApplicationMaster.getApplicationToken(), ecfMap);

        configureServerSideProfileForSlaveApp(client, GPIOcontrolApplicationSlave);
        configureSlaveApp(client, GPIOcontrolApplicationSlave.getId(), GPIOcontrolApplicationSlave.getApplicationToken(), ecfMap);

        LOG.info("Finished loading 'GPIO control Demo Application' data.");
    }

    private void configureServerSideProfileForSlaveApp(AdminClient client, ApplicationDto GPIOcontrolApplicationSlave) throws Exception {
        LOG.info("GPIO control Demo: Creating server profile schema for GPIO Slave settings...");
        CTLSchemaDto serverProfileCtlSchema = saveCTLSchemaWithAppToken(client, "gpio_slave_settings_server_profile.avsc", GPIOcontrolApplicationSlave);
        ServerProfileSchemaDto serverProfileSchemaDto = new ServerProfileSchemaDto();
        serverProfileSchemaDto.setApplicationId(GPIOcontrolApplicationSlave.getId());
        serverProfileSchemaDto.setName("GPIOSlaveSettings");
        serverProfileSchemaDto.setVersion(serverProfileCtlSchema.getVersion());
        serverProfileSchemaDto.setCtlSchemaId(serverProfileCtlSchema.getId());
        serverProfileSchemaDto.setDescription("GPIO Controller server side settings");
        serverProfileSchemaDto = client.saveServerProfileSchema(serverProfileSchemaDto);
        LOG.info("GPIO Slave settings schema was created: [{}]", serverProfileSchemaDto);
    }

    private void configureMasterApp(AdminClient client,
                                    String applicationId,
                                    String applicationToken,
                                    Map<String, EventClassFamilyDto> ecfMap) throws Exception {
        SdkProfileDto sdkProfile = createSdkProfile(client, applicationId, applicationToken, true);

        List<String> aefMapIds = new ArrayList<>();

        aefMapIds.add(createAefMap(client,
                applicationId,
                ecfMap.get(REMOTE_CONTROL_ECF_NAME),
                defaultMasterAefMap));

        sdkProfile.setAefMapIds(aefMapIds);

        projectsSdkMap.put(GPIO_MASTER_ANDROID_ID, sdkProfile);
        projectsSdkMap.put(GPIO_MASTER_OBJC_ID, sdkProfile);
    }

    private void configureSlaveApp(AdminClient client,
                                   String applicationId,
                                   String applicationToken,
                                   Map<String, EventClassFamilyDto> ecfMap) throws Exception {
        SdkProfileDto sdkProfile = createSdkProfile(client, applicationId, applicationToken, true);

        List<String> aefMapIds = new ArrayList<>();

        aefMapIds.add(createAefMap(client,
                applicationId,
                ecfMap.get(REMOTE_CONTROL_ECF_NAME),
                defaultSlaveAefMap));

        sdkProfile.setAefMapIds(aefMapIds);

        projectsSdkMap.put(GPIO_C_ID, sdkProfile);
        projectsSdkMap.put(GPIO_CPP_ID, sdkProfile);
        projectsSdkMap.put(GPIO_ARTIK5_ID, sdkProfile);
    }

    private EventClassFamilyDto addEventClassFamily(AdminClient client, String tenantId,
                                                    String name, String namespace, String className, String resource) throws Exception {
        EventClassFamilyDto eventClassFamily = new EventClassFamilyDto();
        eventClassFamily.setName(name);
        eventClassFamily.setNamespace(namespace);
        eventClassFamily.setClassName(className);
        eventClassFamily = client.editEventClassFamily(eventClassFamily);
        addEventClassFamilyVersion(eventClassFamily, client, tenantId, resource);
        return eventClassFamily;
    }

    private SdkProfileDto createSdkProfile(AdminClient client,
                                           String applicationId,
                                           String applicationToken,
                                           boolean createVerifier) throws Exception {
        SdkProfileDto sdkKey = new SdkProfileDto();
        sdkKey.setApplicationId(applicationId);
        sdkKey.setApplicationToken(applicationToken);
        sdkKey.setProfileSchemaVersion(0);
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
    protected Map<String, SdkProfileDto> getProjectsSdkMap() {
        return projectsSdkMap;
    }


}
