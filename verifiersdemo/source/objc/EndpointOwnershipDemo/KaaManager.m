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

#import "KaaManager.h"

@interface KaaManager () <ProfileContainer, ConfigurationDelegate, KaaClientStateDelegate>

@property (nonatomic, strong) volatile id<KaaClient> kaaClient;
@property (nonatomic, strong) KAAConfigurationKaaVerifiersTokens *verifiersTokens;

@end

@implementation KaaManager

/**
 * Returns shared instance of KaaManager class.
 */

+ (KaaManager *)sharedInstance {
    static KaaManager *manager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        manager = [[KaaManager alloc] init];
        manager.kaaClient = [KaaClientFactory clientWithContext:[[DefaultKaaPlatformContext alloc] init] stateDelegate:manager];
        [manager.kaaClient setProfileContainer:manager];
    });
    return manager;
}

- (void)startKaaClient {
    if (self.kaaClient) {
        [self.kaaClient start];
    } else {
        self.kaaClient = [KaaClientFactory clientWithContext:[[DefaultKaaPlatformContext alloc] init] stateDelegate:self];
        [self.kaaClient start];
    }
    self.verifiersTokens = [self.kaaClient getConfiguration];
}

- (void)attachUser:(User *)user delegate:(id<UserAttachDelegate>)delegate {
    NSLog(@"Attaching user...");
    [self.kaaClient attachUserWithVerifierToken:[self getKaaVerifiersTokenForUser:user]
                                         userId:user.userId
                                    accessToken:user.token
                                       delegate:delegate];
}

- (void)assistedAttachWithAccessToken:(NSString *)tokenString delegate:(id<OnAttachEndpointOperationDelegate>)delegate {
    EndpointAccessToken *token = [[EndpointAccessToken alloc] initWithToken:tokenString];
    [self.kaaClient attachEndpointWithAccessToken:token delegate:delegate];
}

/**
 * Detach the endpoint from the user.
 */

- (void)detachEndpoitWithDelegate:(id<OnDetachEndpointOperationDelegate>)delegate {
    NSLog(@"Detaching endpoint with key hash %@", [self.kaaClient getEndpointKeyHash]);
    EndpointKeyHash *keyHash = [[EndpointKeyHash alloc] initWithKeyHash:[self.kaaClient getEndpointKeyHash]];
    [self.kaaClient detachEndpointWithKeyHash:keyHash delegate:delegate];
}

- (EventFamilyFactory *)getEventFamilyFactory {
    return [self.kaaClient getEventFamilyFactory];
}

- (VerifiersDemoEventClassFamily *)getEventClassFamily {
    return [[self.kaaClient getEventFamilyFactory] getVerifiersDemoEventClassFamily];
}

- (NSString *)getKaaVerifiersTokenForUser:(User *)user {
    switch (user.network) {
        case AuthorizedNetworkFacebook:
            return self.verifiersTokens.facebookKaaVerifierToken.data;
            break;
            
        case AuthorizedNetworkTwitter:
            return self.verifiersTokens.twitterKaaVerifierToken.data;
            break;
            
        case AuthorizedNetworkGoogle:
            return self.verifiersTokens.googleKaaVerifierToken.data;
            break;
            
        default:
            break;
    }
}

#pragma mark - ProfileContainer

- (KAAProfileEmptyData *)getProfile {
    return [[KAAProfileEmptyData alloc] init];
}

#pragma mark - ConfigurationDelegate

- (void)onConfigurationUpdate:(KAAConfigurationKaaVerifiersTokens *)configuration {
    self.verifiersTokens = configuration;
}

#pragma mark - KaaClientStateDelegate

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
