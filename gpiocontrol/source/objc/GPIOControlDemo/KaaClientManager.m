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

#import "KaaClientManager.h"

#define USER_EXTERNAL_ID    @"user@email.com"
#define USER_ACCESS_TOKEN   @"token"



@import Kaa;

@interface KaaClientManager () <UserAttachDelegate, KaaClientStateDelegate, ProfileContainer>

@property (nonnull, strong) KAAProfileEmptyData *profile;

@end

@implementation KaaClientManager

+ (KaaClientManager *)sharedManager {
    static KaaClientManager *sharedMyManager = nil;
    @synchronized(self) {
        if (sharedMyManager == nil)
            sharedMyManager = [[self alloc] init];
    }
    return sharedMyManager;
}

- (id)init {
    self = [super init];
    if (self) {
        [self initializeKaa];
    }
    return self;
}

- (void)initializeKaa {
    self.kaaClient = [KaaClientFactory clientWithContext:[[DefaultKaaPlatformContext alloc] init] stateDelegate:self];
    self.profile = [[KAAProfileEmptyData alloc] init];
    [self.kaaClient setProfileContainer:self];
    [self.kaaClient start];
    
    // Attach user
    [self.kaaClient attachUserWithId:USER_EXTERNAL_ID
                         accessToken:USER_ACCESS_TOKEN
                            delegate:self];
}

- (void)sendDeviceInfoRequestToAll:(id <RemoteControlECFDelegate>)delegate {
    EventFamilyFactory *eventFamilyFactory = [self.kaaClient getEventFamilyFactory];
    RemoteControlECF *ecf = [eventFamilyFactory getRemoteControlECF];
    [ecf addDelegate:delegate];

    [ecf sendRemoteControlECFDeviceInfoRequestToAll:[[RemoteControlECFDeviceInfoRequest alloc] init]];
}

- (void)attachEndpoint:(NSString *)endpointId delegate:(id<OnAttachEndpointOperationDelegate>)delegate {
     [self.kaaClient attachEndpointWithAccessToken:[[EndpointAccessToken alloc]
                                                    initWithToken:endpointId]
                                          delegate:delegate];
}

- (void)detachEndpoint:(NSString *)endpointId delegate:(id<OnDetachEndpointOperationDelegate>)delegate {
    EndpointKeyHash *keyHash = [[EndpointKeyHash alloc] initWithKeyHash:endpointId];
    [self.kaaClient detachEndpointWithKeyHash:keyHash delegate:delegate];
}

- (KAAProfileEmptyData *)getProfile
{
    return self.profile;
}

- (void)onAttachResult:(UserAttachResponse *)response {
    NSLog(@"User attach resultType: %u", response.result);
}

- (void)onStarted{
    NSLog(@"Kaa client started");
}
- (void)onStartFailureWithException:(NSException *)exception {
    NSLog(@"Kaa client startup failure. %@", exception);
}
- (void)onPaused {
    NSLog(@"Kaa client paused");
}
- (void)onPauseFailureWithException:(NSException *)exception {
    NSLog(@"Kaa client pause failure. %@", exception);
}
- (void)onResume{
    NSLog(@"Kaa client resumed");
}
- (void)onResumeFailureWithException:(NSException *)exception {
    NSLog(@"Kaa client resume failure. %@", exception);
}
- (void)onStopped {
    NSLog(@"Kaa client stopped");
}
- (void)onStopFailureWithException:(NSException *)exception {
    NSLog(@"Kaa client stop failure. %@", exception);
}


@end
