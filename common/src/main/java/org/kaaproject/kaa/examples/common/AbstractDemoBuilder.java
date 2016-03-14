/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.examples.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.iharder.Base64;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.admin.SdkTokenDto;
import org.kaaproject.kaa.common.dto.admin.TenantUserDto;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventAction;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.examples.common.projects.Bundle;
import org.kaaproject.kaa.examples.common.projects.Project;
import org.kaaproject.kaa.examples.common.projects.ProjectsConfig;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDemoBuilder implements DemoBuilder {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDemoBuilder.class);

    private static final String KAA_ADMIN_USER_PARAM = "kaaAdminUser";
    private static final String KAA_ADMIN_PASSWORD_PARAM = "kaaAdminPassword";

    private static final String TENANT_ADMIN_USER_PARAM = "tenantAdminUser";
    private static final String TENANT_ADMIN_PASSWORD_PARAM = "tenantAdminPassword";

    private static final String TENANT_DEVELOPER_USER_PARAM = "tenantDeveloperUser";
    private static final String TENANT_DEVELOPER_PASSWORD_PARAM = "tenantDeveloperPassword";
    
    private static final String DEFAULT_KAA_ADMIN_USER = "kaa";
    private static final String DEFAULT_KAA_ADMIN_PASSWORD = "kaa123";

    private static final String DEFAULT_TENANT_ADMIN_USER = "admin";
    private static final String DEFAULT_TENANT_ADMIN_PASSWORD = "admin123";

    private static final String DEFAULT_TENANT_DEVELOPER_USER = "devuser";
    private static final String DEFAULT_TENANT_DEVELOPER_PASSWORD = "devuser123";
    
    private static final String KAA_ADMIN_USERNAME_VAR = "\\$\\{kaa_admin_username\\}";
    private static final String KAA_ADMIN_PASSWORD_VAR = "\\$\\{kaa_admin_password\\}";

    private static final String TENANT_ADMIN_USERNAME_VAR = "\\$\\{tenant_admin_username\\}";
    private static final String TENANT_ADMIN_PASSWORD_VAR = "\\$\\{tenant_admin_password\\}";

    private static final String TENANT_DEVELOPER_USERNAME_VAR = "\\$\\{tenant_developer_username\\}";
    private static final String TENANT_DEVELOPER_PASSWORD_VAR = "\\$\\{tenant_developer_password\\}";
    
    private static final String PROJECTS_XML = "projects.xml";
    
    private static final String ICON_PNG = "icon.png";

    private static boolean usersCreated = false;
    
    public static String kaaAdminUser = DEFAULT_KAA_ADMIN_USER;
    public static String kaaAdminPassword = DEFAULT_KAA_ADMIN_PASSWORD;
    
    public static String tenantAdminUser = DEFAULT_TENANT_ADMIN_USER;
    public static String tenantAdminPassword = DEFAULT_TENANT_ADMIN_PASSWORD;
    
    public static String tenantDeveloperUser = DEFAULT_TENANT_DEVELOPER_USER;
    public static String tenantDeveloperPassword = DEFAULT_TENANT_DEVELOPER_PASSWORD;
    
    private final String resourcesPath;
    protected final SdkProfileDto sdkProfileDto;
    private ProjectsConfig projectConfigs;
    
    public static void updateCredentialsFromArgs(String[] args) {
        logger.info("Updating credentials information from arguments...");
        for (String arg : args) {
            if (arg != null && arg.length()>0) {
                String[] params = arg.split("=");
                if (params != null && params.length==2) {
                    String name = params[0];
                    String value = params[1];
                    if (name != null && value != null && value.length()>0) {
                        if (KAA_ADMIN_USER_PARAM.equals(name)) {
                            kaaAdminUser = value;
                        }
                        else if (KAA_ADMIN_PASSWORD_PARAM.equals(name)) {
                            kaaAdminPassword = value;
                        }
                        else if (TENANT_ADMIN_USER_PARAM.equals(name)) {
                            tenantAdminUser = value;
                        }
                        else if (TENANT_ADMIN_PASSWORD_PARAM.equals(name)) {
                            tenantAdminPassword = value;
                        }
                        else if (TENANT_DEVELOPER_USER_PARAM.equals(name)) {
                            tenantDeveloperUser = value;
                        }
                        else if (TENANT_DEVELOPER_PASSWORD_PARAM.equals(name)) {
                            tenantDeveloperPassword = value;
                        }
                    }
                }
            }
        }
        logger.info("Credentials information updated:");
        logger.info("{} = {}", KAA_ADMIN_USER_PARAM, kaaAdminUser);
        logger.info("{} = {}", KAA_ADMIN_PASSWORD_PARAM, kaaAdminPassword);
        logger.info("{} = {}", TENANT_ADMIN_USER_PARAM, tenantAdminUser);
        logger.info("{} = {}", TENANT_ADMIN_PASSWORD_PARAM, tenantAdminPassword);
        logger.info("{} = {}", TENANT_DEVELOPER_USER_PARAM, tenantDeveloperUser);
        logger.info("{} = {}", TENANT_DEVELOPER_PASSWORD_PARAM, tenantDeveloperPassword);
    }

    public static String updateCredentialsInfo(String template) {
        return template.replaceAll(KAA_ADMIN_USERNAME_VAR, kaaAdminUser)
                .replaceAll(KAA_ADMIN_PASSWORD_VAR, kaaAdminPassword)
                .replaceAll(TENANT_ADMIN_USERNAME_VAR, tenantAdminUser)
                .replaceAll(TENANT_ADMIN_PASSWORD_VAR, tenantAdminPassword)
                .replaceAll(TENANT_DEVELOPER_USERNAME_VAR, tenantDeveloperUser)
                .replaceAll(TENANT_DEVELOPER_PASSWORD_VAR, tenantDeveloperPassword);
    }

    protected AbstractDemoBuilder(String resourcesPath) {
        this.resourcesPath = resourcesPath;
        this.sdkProfileDto = new SdkProfileDto();
    }
    
    @Override
    public void buildDemoApplication(AdminClient client) throws Exception {
        logger.info("Demo application build started...");
        createUsers(client);
        buildDemoApplicationImpl(client);
        projectConfigs = loadProjectConfigs();
        logger.info("Demo application build finished.");
        
        Map<SdkTokenDto, String> sdkProfiles = new HashMap<>(); 
        for (Project projectConfig : projectConfigs.getProjects()) {
            String iconBase64 = loadIconBase64(projectConfig.getId());
            projectConfig.setIconBase64(iconBase64);
            SdkProfileDto sdkProfileDto = this.sdkProfileDto;
            if (isMultiApplicationProject()) {
                Map<String, SdkProfileDto> projectsSdkMap = getProjectsSdkMap();
                sdkProfileDto = projectsSdkMap.get(projectConfig.getId());
            }
            SdkTokenDto sdkProfileToken = sdkProfileDto.toSdkTokenDto();
            String sdkProfileId = sdkProfiles.get(sdkProfileToken);
            if (sdkProfileId == null) {
                loginTenantDeveloper(client);
                sdkProfileDto = client.createSdkProfile(sdkProfileDto);
                logger.info("Resulting sdk profile: {}", sdkProfileDto);
                sdkProfileId = sdkProfileDto.getId();
                sdkProfiles.put(sdkProfileToken, sdkProfileId);
            }
            projectConfig.setSdkProfileId(sdkProfileId);
        }
        for (Bundle bundle : projectConfigs.getBundles()) {
            String iconBase64 = loadIconBase64(bundle.getId());
            bundle.setIconBase64(iconBase64);
        }
    }

    @Override
    public ProjectsConfig getProjectConfigs() {
        return projectConfigs;
    }
    
    protected abstract void buildDemoApplicationImpl(AdminClient client) throws Exception;
    
    protected String getResourcePath(String resource) {
        return resourcesPath + "/" + resource;
    }
    
    protected String getResourceAsString(String resource) throws IOException {
        return FileUtils.readResource(getResourcePath(resource));
    }

    protected boolean isMultiApplicationProject() {
        return false;
    }
    
    protected Map<String, SdkProfileDto> getProjectsSdkMap() {
        return null;
    }
    
    private void createUsers(AdminClient client) throws Exception {
        if (!usersCreated) {
            logger.info("Creating users...");
            client.createKaaAdmin(kaaAdminUser, kaaAdminPassword);
            loginKaaAdmin(client);
            createDemoTenant(client);
            loginTenantAdmin(client);
            createTenantDeveloper(client);
            usersCreated = true;
        }
    }

    private void createDemoTenant(AdminClient client) throws Exception {
        TenantUserDto tenant = new TenantUserDto();
        tenant.setTenantName("Demo Tenant");
        tenant.setAuthority(KaaAuthorityDto.TENANT_ADMIN);
        tenant.setUsername(tenantAdminUser);
        tenant.setMail("admin@demoproject.org");
        tenant.setFirstName("Tenant");
        tenant.setLastName("Admin");
        tenant = client.editTenant(tenant);
        
        if (StringUtils.isNotBlank(tenant.getTempPassword())) {
            client.clearCredentials();
            client.changePassword(tenant.getUsername(), tenant.getTempPassword(), tenantAdminPassword);
        }
    }
    
    private void createTenantDeveloper(AdminClient client) throws Exception {
        UserDto tenantDeveloper = new UserDto();
        tenantDeveloper.setAuthority(KaaAuthorityDto.TENANT_DEVELOPER);
        tenantDeveloper.setUsername(tenantDeveloperUser);
        tenantDeveloper.setMail("devuser@demoproject.org");
        tenantDeveloper.setFirstName("Tenant");
        tenantDeveloper.setLastName("Developer");
        tenantDeveloper = client.editUser(tenantDeveloper);
        
        if (StringUtils.isNotBlank(tenantDeveloper.getTempPassword())) {
            client.clearCredentials();
            client.changePassword(tenantDeveloper.getUsername(), tenantDeveloper.getTempPassword(), tenantDeveloperPassword);
        }
    }
    
    private ProjectsConfig loadProjectConfigs() throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance("org.kaaproject.kaa.examples.common.projects");
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        InputStream is = getClass().getClassLoader().getResourceAsStream(getResourcePath(PROJECTS_XML));
        return (ProjectsConfig)unmarshaller.unmarshal(is);
    }
    
    private String loadIconBase64(String projectId) throws IOException {
        String base64 = null;
        InputStream is = getClass().getClassLoader().getResourceAsStream(getResourcePath(projectId + "/" + ICON_PNG));
        if (is == null) {
            is = getClass().getClassLoader().getResourceAsStream(getResourcePath(ICON_PNG));
        }
        if (is != null) {
            byte[] data = IOUtils.toByteArray(is);
            base64 = Base64.encodeBytes(data);
        }
        return base64;
    }

    protected void loginKaaAdmin(AdminClient client) throws Exception {
        client.login(kaaAdminUser, kaaAdminPassword);
    }
    
    
    protected void loginTenantAdmin(AdminClient client) throws Exception {
        client.login(tenantAdminUser, tenantAdminPassword);
    }
    
    
    protected void loginTenantDeveloper(AdminClient client) throws Exception {
        client.login(tenantDeveloperUser, tenantDeveloperPassword);
    }
    
    protected static ApplicationEventFamilyMapDto mapEventClassFamily(AdminClient client, 
            ApplicationDto application, 
            EventClassFamilyDto eventClassFamily) throws Exception {
        List<EventClassDto> eventClasses = 
                client.getEventClassesByFamilyIdVersionAndType(eventClassFamily.getId(), 1, EventClassType.EVENT);

        ApplicationEventFamilyMapDto aefMap = new ApplicationEventFamilyMapDto();
        aefMap.setApplicationId(application.getId());
        aefMap.setEcfId(eventClassFamily.getId());
        aefMap.setEcfName(eventClassFamily.getName());
        aefMap.setVersion(1);
        
        List<ApplicationEventMapDto> eventMaps = new ArrayList<>(eventClasses.size());
        for (EventClassDto eventClass : eventClasses) {
            ApplicationEventMapDto eventMap = new ApplicationEventMapDto();
            eventMap.setEventClassId(eventClass.getId());
            eventMap.setFqn(eventClass.getFqn());
                eventMap.setAction(ApplicationEventAction.BOTH);
            eventMaps.add(eventMap);
        }
        
        aefMap.setEventMaps(eventMaps);
        aefMap = client.editApplicationEventFamilyMap(aefMap);
        return aefMap;
    }
    
}
