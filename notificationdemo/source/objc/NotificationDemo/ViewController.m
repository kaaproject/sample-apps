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

#import "ViewController.h"
#import <Kaa/Kaa.h>
#import "Kaa/NotificationTopicListDelegate.h"

@interface ViewController () <KaaClientStateDelegate, NotificationTopicListDelegate, NotificationDelegate, ProfileContainer>

@property (weak, nonatomic) IBOutlet UITextView *logTextView;

@property (nonatomic,strong) id<KaaClient> kaaClient;

@end

@implementation ViewController


- (void)viewDidLoad {
    [super viewDidLoad];

    [self addLogWithText:@"NotificationDemo started"];
    
    //Create a Kaa client with the Kaa default context.
    self.kaaClient = [Kaa clientWithContext:[[DefaultKaaPlatformContext alloc] init] stateDelegate:self];
    
    // A listener that listens to the notification topic list updates.
    [self.kaaClient addTopicListDelegate:self];
    
    // Add a notification listener that listens to all notifications.
    [self.kaaClient addNotificationDelegate:self];
    
    // Set up profile container, needed for ProfileManager
    [self.kaaClient setProfileContainer:self];
    
    // Start the Kaa client and connect it to the Kaa server.
    [self.kaaClient start];
    
    // Get available notification topics.
    NSArray *topics = [self.kaaClient getTopics];
    
    // List the obtained notification topics.
    [self showTopicList:topics];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - Delegate methods

- (void)onListUpdated:(NSArray *)list {
    [self addLogWithText:@"Topic list was updated"];
    [self showTopicList:list];
    @try {
        //Try to subscribe to all new optional topics, if any.
        NSArray *optionalTopics = [self extractOptionalTopicIds:list];
        for (NSString *optionalTopicId in optionalTopics) {
            [self addLogWithText:[NSString stringWithFormat:@"Subscribing to optional topic %@", optionalTopicId]];
        }
        [self.kaaClient subscribeToTopicsWithIDs:optionalTopics forceSync:YES];
    }
    @catch (NSException *exception) {
        [self addLogWithText:@"Topic is unavaliable. Can't subscribe"];
    }
}

- (void)onNotification:(KAASampleNotification *)notification withTopicId:(NSString *)topicId {
    [self addLogWithText:[NSString stringWithFormat:@"Notification for topicId %@ received", topicId]];
    [self addLogWithText:[NSString stringWithFormat:@"Notification body: %@", notification.message.data]];
}

- (void)onStarted {
    [self addLogWithText:@"Kaa client started"];
}

- (void)onStopped {
    [self addLogWithText:@"Kaa client stopped"];
}

- (void)onStartFailureWithException:(NSException *)exception {
    [self addLogWithText:[NSString stringWithFormat:@"START FAILURE: %@ : %@", exception.name, exception.reason]];
}

- (void)onPaused {
    [self addLogWithText:@"Client paused"];
}

- (void)onPauseFailureWithException:(NSException *)exception {
    [self addLogWithText:[NSString stringWithFormat:@"PAUSE FAILURE: %@ : %@", exception.name, exception.reason]];
}

- (void)onResume {
    [self addLogWithText:@"Client resumed"];
}

- (void)onResumeFailureWithException:(NSException *)exception {
    [self addLogWithText:[NSString stringWithFormat:@"RESUME FAILURE: %@ : %@", exception.name, exception.reason]];
}

- (void)onStopFailureWithException:(NSException *)exception {
    [self addLogWithText:[NSString stringWithFormat:@"STOP FAILURE: %@ : %@", exception.name, exception.reason]];
}

#pragma mark - Supporting methods

- (KAAEmptyData *)getProfile {
    return [[KAAEmptyData alloc] init];
}

- (void)showTopicList:(NSArray *)topics {
    if (topics.count == 0) {
        [self addLogWithText:@"Topic list is empty"];
    } else {
        for (Topic *topic in topics) {
            [self addLogWithText:[NSString stringWithFormat:@"%@ %@ %u", topic.id, topic.name, topic.subscriptionType]];
        }
    }
}

- (NSArray *)extractOptionalTopicIds:(NSArray *)topics {
    NSMutableArray *topicIds = [NSMutableArray array];
    for (Topic *t in topics) {
        if (t.subscriptionType == SUBSCRIPTION_TYPE_OPTIONAL_SUBSCRIPTION) {
            [topicIds addObject:t.id];
        }
    }
    return topicIds;
}

- (void) addLogWithText:(NSString *) text {
    NSLog(@"%@", text);
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        if ([self.logTextView.text isEqualToString:@""]) {
            self.logTextView.text = [NSString stringWithFormat:@"%@", text];
        } else {
            self.logTextView.text = [NSString stringWithFormat:@"%@\n%@", self.logTextView.text, text];
        }
    }];
}


@end
