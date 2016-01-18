//
//  AppDelegate.m
//  EventDemo
//
//  Created by Anton Bohomol on 10/30/15.
//  Copyright © 2015 CYBERVISION INC. All rights reserved.
//

#import "AppDelegate.h"
#import <Kaa/Kaa.h>
#import "KaaProvider.h"

@interface AppDelegate ()

@end

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    NSUserDefaults *theDefaults = [NSUserDefaults standardUserDefaults];
    NSInteger times = [theDefaults integerForKey:@"hasRun"];
    [theDefaults setInteger:times forKey:@"hasRun"];
    [theDefaults synchronize];
    return YES;
}

- (void)applicationWillResignActive:(UIApplication *)application {
    [[KaaProvider getClient] pause];
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
    [[KaaProvider getClient] resume];
}

@end
