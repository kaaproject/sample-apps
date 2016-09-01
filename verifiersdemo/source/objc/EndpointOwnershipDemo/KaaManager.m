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

@interface ConcreteClientStateDelegate () <KaaClientStateDelegate>

@end

@implementation ConcreteClientStateDelegate

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

@interface KaaManager ()

@property (nonatomic, strong) volatile id<KaaClient> kaaClient;

@end

@implementation KaaManager

+ (KaaManager *)sharedInstance {
    static KaaManager *manager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        manager = [[KaaManager alloc] init];
        manager.kaaClient = [KaaClientFactory clientWithContext:[[DefaultKaaPlatformContext alloc] init] stateDelegate:[[ConcreteClientStateDelegate alloc] init]];
    });
    return manager;
}

@end
