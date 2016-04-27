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

package org.kaaproject.kaa.examples.notification;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.examples.common.AbstractDemoBuilder;
import org.kaaproject.kaa.examples.common.KaaDemoBuilder;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KaaDemoBuilder
public class NotificationDemoBuilder extends AbstractDemoBuilder {

    private static final Logger logger = LoggerFactory.getLogger(NotificationDemoBuilder.class);
    
    private static final String NOTIFICATION_DEMO_JAVA_ID = "notification_demo_java";
    private static final String NOTIFICATION_DEMO_CPP_ID = "notification_demo_cpp";
    private static final String NOTIFICATION_DEMO_C_ID = "notification_demo_c";
    private static final String NOTIFICATION_DEMO_ANDROID_ID = "notification_demo_android";
    private static final String NOTIFICATION_DEMO_OBJC_ID = "notification_demo_objc";
    
    private static final Long NOTIFICATION_VERSION = 1L;
    private static final Date NOTIFICATION_EXPIRE_DATE = new Date(1900000000000L);

    private Map<String, SdkProfileDto> projectsSdkMap = new HashMap<>();
    
    public NotificationDemoBuilder() {
        super("demo/notification");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {
        SdkProfileDto sdkProfile = createNotificationApplication(client, "Notification demo",
                "notification_schema.avsc", "mandatory_notification.json", "optional_notification.json");

        projectsSdkMap.put(NOTIFICATION_DEMO_JAVA_ID, sdkProfile);
        projectsSdkMap.put(NOTIFICATION_DEMO_CPP_ID, sdkProfile);
        projectsSdkMap.put(NOTIFICATION_DEMO_C_ID, sdkProfile);
        projectsSdkMap.put(NOTIFICATION_DEMO_OBJC_ID, sdkProfile);

        sdkProfile = createNotificationApplication(client, "Android notification demo",
                NOTIFICATION_DEMO_ANDROID_ID + "/notification_schema.avsc",
                NOTIFICATION_DEMO_ANDROID_ID + "/mandatory_notification.json",
                NOTIFICATION_DEMO_ANDROID_ID + "/optional_notification.json");

        projectsSdkMap.put(NOTIFICATION_DEMO_ANDROID_ID, sdkProfile);
    }
    
    private SdkProfileDto createNotificationApplication(AdminClient client,
            String appName,
            String notificationSchemaRes,
            String mandatoryNotificationRes,
            String optionalNotificationRes) throws Exception {


        SdkProfileDto sdkProfileDto = new SdkProfileDto();
        logger.info("Loading '{} application' data...", appName);

        loginTenantAdmin(client);

        ApplicationDto notificationApplication = new ApplicationDto();
        notificationApplication.setName(appName);
        notificationApplication = client.editApplication(notificationApplication);

        sdkProfileDto.setApplicationId(notificationApplication.getId());
        sdkProfileDto.setApplicationToken(notificationApplication.getApplicationToken());
        sdkProfileDto.setProfileSchemaVersion(0);
        sdkProfileDto.setConfigurationSchemaVersion(1);
        sdkProfileDto.setLogSchemaVersion(1);

        loginTenantDeveloper(client);

        logger.info("Creating notification schema...");
        NotificationSchemaDto notificationSchemaDto = new NotificationSchemaDto();
        notificationSchemaDto.setApplicationId(notificationApplication.getId());
        notificationSchemaDto.setName("Notification schema");
        notificationSchemaDto.setDescription("Notification schema of a sample notification");
        notificationSchemaDto = client.createNotificationSchema(notificationSchemaDto, getResourcePath(notificationSchemaRes));
        sdkProfileDto.setNotificationSchemaVersion(notificationSchemaDto.getVersion());
        logger.info("Notification schema was created.");

        logger.info("Getting base endpoint group");
        EndpointGroupDto baseEndpointGroup = null;
        List<EndpointGroupDto> endpointGroups = client.getEndpointGroupsByAppToken(notificationApplication.getApplicationToken());
        if (endpointGroups.size() == 1 && endpointGroups.get(0).getWeight() == 0) {
            baseEndpointGroup = endpointGroups.get(0);
        }

        if (baseEndpointGroup == null) {
            throw new RuntimeException("Can't get default endpoint group for notification demo application!");
        }

        logger.info("Base endpoint group was successfully gotten");

        TopicDto mandatoryTopic = new TopicDto();
        mandatoryTopic.setApplicationId(notificationApplication.getId());
        mandatoryTopic.setName("Sample mandatory topic");
        mandatoryTopic.setType(TopicTypeDto.MANDATORY);
        mandatoryTopic.setDescription("Sample mandatory topic to demonstrate notifications API");
        logger.info("Creating mandatory topic: {}", mandatoryTopic);
        mandatoryTopic = client.createTopic(mandatoryTopic);
        client.addTopicToEndpointGroup(baseEndpointGroup, mandatoryTopic);
        logger.info("Mandatory topic {} was created", mandatoryTopic);

        NotificationDto mandatoryNotification = new NotificationDto();
        mandatoryNotification.setApplicationId(notificationApplication.getId());
        mandatoryNotification.setSchemaId(notificationSchemaDto.getId());
        mandatoryNotification.setType(NotificationTypeDto.USER);
        mandatoryNotification.setExpiredAt(NOTIFICATION_EXPIRE_DATE);
        mandatoryNotification.setTopicId(mandatoryTopic.getId());
        client.sendNotification(mandatoryNotification, getResourcePath(mandatoryNotificationRes));

        TopicDto optionalTopic = new TopicDto();
        optionalTopic.setApplicationId(notificationApplication.getId());
        optionalTopic.setName("Sample optional topic");
        optionalTopic.setType(TopicTypeDto.OPTIONAL);
        optionalTopic.setDescription("Sample optional topic to demonstrate notifications API");
        logger.info("Creating optional topic: {}", optionalTopic);
        optionalTopic = client.createTopic(optionalTopic);
        client.addTopicToEndpointGroup(baseEndpointGroup, optionalTopic);
        logger.info("Optional topic {} was created", optionalTopic);

        NotificationDto optionalTopicNotification = new NotificationDto();
        optionalTopicNotification.setApplicationId(notificationApplication.getId());
        optionalTopicNotification.setSchemaId(notificationSchemaDto.getId());
        optionalTopicNotification.setType(NotificationTypeDto.USER);
        optionalTopicNotification.setExpiredAt(NOTIFICATION_EXPIRE_DATE);
        optionalTopicNotification.setTopicId(optionalTopic.getId());
        logger.info("Creating notification for optional topic: {}", optionalTopicNotification.toString());
        client.sendNotification(optionalTopicNotification, getResourcePath(optionalNotificationRes));
        logger.info("Notification for optional topic was created");

        logger.info("Finished loading '{} application' data...", appName);

        return sdkProfileDto;
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
