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

package org.kaaproject.kaa.examples.cityguide;

import java.util.List;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.examples.common.AbstractDemoBuilder;
import org.kaaproject.kaa.examples.common.KaaDemoBuilder;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KaaDemoBuilder
public class CityGuideDemoBuilder extends AbstractDemoBuilder {
    
    private static final Logger LOG = LoggerFactory.getLogger(CityGuideDemoBuilder.class);
    
    public CityGuideDemoBuilder() {
        super("demo/cityguide");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client)
            throws Exception {
        
        LOG.info("Loading 'City Guide Demo Application' data...");
        
        loginTenantAdmin(client);
        
        ApplicationDto cityGuideApplication = new ApplicationDto();
        cityGuideApplication.setName("City guide");
        cityGuideApplication = client.editApplication(cityGuideApplication);
        
        sdkProfileDto.setApplicationId(cityGuideApplication.getId());
        sdkProfileDto.setApplicationToken(cityGuideApplication.getApplicationToken());
        sdkProfileDto.setNotificationSchemaVersion(1);
        sdkProfileDto.setLogSchemaVersion(1);

        loginTenantDeveloper(client);
        
        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setApplicationId(cityGuideApplication.getId());
        configurationSchema.setName("City guide configuration schema");
        configurationSchema.setDescription("Configuration schema describing cities and places used by city guide application");
        configurationSchema = client.createConfigurationSchema(configurationSchema, getResourcePath("city_guide.avsc"));
        sdkProfileDto.setConfigurationSchemaVersion(configurationSchema.getVersion());
        
        CTLSchemaDto profileCtlSchema = client.saveCTLSchemaWithAppToken(getResourceAsString("city_guide_profile.avsc"), cityGuideApplication.getTenantId(),
                cityGuideApplication.getApplicationToken());
        
        EndpointProfileSchemaDto profileSchema = new EndpointProfileSchemaDto();
        profileSchema.setApplicationId(cityGuideApplication.getId());
        profileSchema.setName("City guide profile schema");
        profileSchema.setDescription("Profile schema describing city guide application profile");
        profileSchema.setCtlSchemaId(profileCtlSchema.getId());
        
        profileSchema = client.saveProfileSchema(profileSchema);
        sdkProfileDto.setProfileSchemaVersion(profileSchema.getVersion());
        
        EndpointGroupDto baseEndpointGroup = null;
        List<EndpointGroupDto> endpointGroups = client.getEndpointGroupsByAppToken(cityGuideApplication.getApplicationToken());
        if (endpointGroups.size() == 1 && endpointGroups.get(0).getWeight() == 0) {
            baseEndpointGroup = endpointGroups.get(0);
        }
        if (baseEndpointGroup == null) {
            throw new RuntimeException("Can't get default endpoint group for city guide application!");
        }
        
        ConfigurationDto baseConfiguration = new ConfigurationDto();
        baseConfiguration.setApplicationId(cityGuideApplication.getId());
        baseConfiguration.setEndpointGroupId(baseEndpointGroup.getId());
        baseConfiguration.setSchemaId(configurationSchema.getId());
        baseConfiguration.setSchemaVersion(configurationSchema.getVersion());
        baseConfiguration.setDescription("Base city guide configuration");
        baseConfiguration.setBody(FileUtils.readResource(getResourcePath("city_guide_data_all.json")));
        baseConfiguration.setStatus(UpdateStatus.INACTIVE);
        baseConfiguration = client.editConfiguration(baseConfiguration);
        client.activateConfiguration(baseConfiguration.getId());
        
        // Atlanta group
        
        EndpointGroupDto atlantaEndpointGroup = new EndpointGroupDto();
        atlantaEndpointGroup.setApplicationId(cityGuideApplication.getId());
        atlantaEndpointGroup.setName("North America/Atlanta");
        atlantaEndpointGroup.setDescription("Atlanta endpoint group");
        atlantaEndpointGroup.setWeight(1);
        
        atlantaEndpointGroup = client.editEndpointGroup(atlantaEndpointGroup);
        
        ConfigurationDto atlantaConfiguration = new ConfigurationDto();
        atlantaConfiguration.setApplicationId(cityGuideApplication.getId());
        atlantaConfiguration.setEndpointGroupId(atlantaEndpointGroup.getId());
        atlantaConfiguration.setSchemaId(configurationSchema.getId());
        atlantaConfiguration.setSchemaVersion(configurationSchema.getVersion());
        atlantaConfiguration.setDescription("City guide configuration for Atlanta city");
        atlantaConfiguration.setBody(FileUtils.readResource(getResourcePath("city_guide_data_atlanta.json")));
        atlantaConfiguration.setStatus(UpdateStatus.INACTIVE);
        atlantaConfiguration = client.editConfiguration(atlantaConfiguration);
        client.activateConfiguration(atlantaConfiguration.getId());
        
        ProfileFilterDto atlantaProfileFilter = new ProfileFilterDto();
        atlantaProfileFilter.setApplicationId(cityGuideApplication.getId());
        atlantaProfileFilter.setEndpointGroupId(atlantaEndpointGroup.getId());
        atlantaProfileFilter.setEndpointProfileSchemaId(profileSchema.getId());
        atlantaProfileFilter.setEndpointProfileSchemaVersion(profileSchema.getVersion());
        atlantaProfileFilter.setDescription("Profile filter for Atlanta city");
        atlantaProfileFilter.setBody(FileUtils.readResource(getResourcePath("city_guide_filter_atlanta.json")));
        atlantaProfileFilter.setStatus(UpdateStatus.INACTIVE);
        atlantaProfileFilter = client.editProfileFilter(atlantaProfileFilter);
        client.activateProfileFilter(atlantaProfileFilter.getId());
        
        // Amsterdam group
        
        EndpointGroupDto amsterdamEndpointGroup = new EndpointGroupDto();
        amsterdamEndpointGroup.setApplicationId(cityGuideApplication.getId());
        amsterdamEndpointGroup.setName("Europe/Amsterdam");
        amsterdamEndpointGroup.setDescription("Amsterdam endpoint group");
        amsterdamEndpointGroup.setWeight(2);
        
        amsterdamEndpointGroup = client.editEndpointGroup(amsterdamEndpointGroup);
        
        ConfigurationDto amsterdamConfiguration = new ConfigurationDto();
        amsterdamConfiguration.setApplicationId(cityGuideApplication.getId());
        amsterdamConfiguration.setEndpointGroupId(amsterdamEndpointGroup.getId());
        amsterdamConfiguration.setSchemaId(configurationSchema.getId());
        amsterdamConfiguration.setSchemaVersion(configurationSchema.getVersion());
        amsterdamConfiguration.setDescription("City guide configuration for Amsterdam city");
        amsterdamConfiguration.setBody(FileUtils.readResource(getResourcePath("city_guide_data_amsterdam.json")));
        amsterdamConfiguration.setStatus(UpdateStatus.INACTIVE);
        amsterdamConfiguration = client.editConfiguration(amsterdamConfiguration);
        client.activateConfiguration(amsterdamConfiguration.getId());
        
        ProfileFilterDto amsterdamProfileFilter = new ProfileFilterDto();
        amsterdamProfileFilter.setApplicationId(cityGuideApplication.getId());
        amsterdamProfileFilter.setEndpointGroupId(amsterdamEndpointGroup.getId());
        amsterdamProfileFilter.setEndpointProfileSchemaId(profileSchema.getId());
        amsterdamProfileFilter.setEndpointProfileSchemaVersion(profileSchema.getVersion());
        amsterdamProfileFilter.setDescription("Profile filter for Amsterdam city");
        amsterdamProfileFilter.setBody(FileUtils.readResource(getResourcePath("city_guide_filter_amsterdam.json")));
        amsterdamProfileFilter.setStatus(UpdateStatus.INACTIVE);
        amsterdamProfileFilter = client.editProfileFilter(amsterdamProfileFilter);
        client.activateProfileFilter(amsterdamProfileFilter.getId());
        
        LOG.info("Finished loading 'City Guide Demo Application' data.");
    }

}
