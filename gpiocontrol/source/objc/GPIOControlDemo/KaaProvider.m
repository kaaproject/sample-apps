/*
 * Copyright 2014-2015 CyberVision, Inc.
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

#import "KaaProvider.h"
#import "PreferencesManager.h"

@implementation ConcreteStateDelegate

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

@implementation KaaProvider

+ (id <KaaClient>)getClient {
    static id <KaaClient> kaaClient = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        kaaClient = [Kaa clientWithContext:[[DefaultKaaPlatformContext alloc] init] stateDelegate:[[ConcreteStateDelegate alloc] init]];
    });
    return kaaClient;
}

- (void)attachUser {
    id <KaaClient> client = [KaaProvider getClient];
    [client attachUserWithId:[PreferencesManager getUserExternalId] accessToken:[PreferencesManager getUserAccessToken] delegate:self];
}

+ (void)setUpEventDelegate:(id <RemoteControlECFDelegate>)delegate {
    id <KaaClient> client = [self getClient];
    EventFamilyFactory *eventFamilyFactory = [client getEventFamilyFactory];
    RemoteControlECF *ecf = [eventFamilyFactory getRemoteControlECF];
    
    [ecf addDelegate:delegate];
    [ecf sendDeviceInfoRequestToAll:[[DeviceInfoRequest alloc] init]];
}

+ (void)sendDeviceInfoRequestToAll {
    id <KaaClient> client = [self getClient];
    EventFamilyFactory *eventFamilyFactory = [client getEventFamilyFactory];
    RemoteControlECF *ecf = [eventFamilyFactory getRemoteControlECF];
    
    [ecf sendDeviceInfoRequestToAll:[[DeviceInfoRequest alloc] init]];
}

- (void)onAttachResult:(UserAttachResponse *)response {
    NSLog(@"User attach resultType: %u", response.result);
}

@end
