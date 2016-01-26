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

#import "AppDelegate.h"
#import "ViewController.h"
#import "ProfileCommon.h"

@interface AppDelegate () <KaaClientStateDelegate, ProfileContainer>

@end

@implementation AppDelegate


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    self.kaaClient = [Kaa clientWithContext:[[DefaultKaaPlatformContext alloc] init] stateDelegate:self];
    [self.kaaClient setProfileContainer:self];
    return YES;
}

- (void)applicationWillResignActive:(UIApplication *)application {
    self.isClientStarted = NO;
    [self.kaaClient pause];
    [[ViewController getLocationManager] stopUpdatingLocation];
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
    [self.kaaClient resume];
    [[ViewController getLocationManager] startUpdatingLocation];
    self.isClientStarted = YES;
}

#pragma mark - ClientState delegate

- (void)onStarted {
    NSLog(@"KaaClient started");
    self.isClientStarted = YES;
}

-(void)onStartFailureWithException:(NSException *)exception {
    NSLog(@"START FAILURE %@", exception);

}

- (void)onPaused {
    NSLog(@"KaaClient paused");
}

-(void)onPauseFailureWithException:(NSException *)exception {
    NSLog(@"PAUSE FAILURE %@", exception);

}

- (void)onResume {
    NSLog(@"KaaClient resumed");
}

-(void)onResumeFailureWithException:(NSException *)exception {
    NSLog(@"RESUME FAILURE %@", exception);

}

- (void)onStopped {
    NSLog(@"KaaClient stopped");
}

-(void)onStopFailureWithException:(NSException *)exception {
    NSLog(@"STOP FAILURE %@", exception);
}


#pragma mark - ProfileContainer

- (KAAEmptyData *)getProfile {
    return [[KAAEmptyData alloc] init];
}


@end
