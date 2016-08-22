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

#import "ViewController.h"

@import Kaa;

@interface ViewController () <KaaClientStateDelegate, NotificationTopicListDelegate, NotificationDelegate, ProfileContainer, UITextFieldDelegate>

@property (nonatomic, weak) IBOutlet UITextView *logTextView;
@property (weak, nonatomic) IBOutlet UITextField *topicIdTextField;
@property (nonatomic, strong) NSMutableAttributedString *log;

@property (nonatomic, strong) id<KaaClient> kaaClient;

@end

@implementation ViewController


- (void)viewDidLoad {
    [super viewDidLoad];
    self.log = [[NSMutableAttributedString alloc] init];
    self.topicIdTextField.delegate = self;

    [self addLogWithText:@"NotificationDemo started"];
    
    //Create a Kaa client with the Kaa default context.
    self.kaaClient = [KaaClientFactory clientWithContext:[[DefaultKaaPlatformContext alloc] init] stateDelegate:self];
    
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

#pragma mark - NotificationTopicListDelegate

- (void)onListUpdated:(NSArray *)list {
    [self addLogWithText:@"Topic list was updated"];
    [self showTopicList:list];
}

#pragma mark - NotificationDelegate

- (void)onNotification:(KAASecurityAlert *)notification withTopicId:(int64_t)topicId {
    [self addLogWithText:[NSString stringWithFormat:@"Notification for topicId %lld received", topicId]];
    switch (notification.alertType) {
        case ALERT_TYPE_CodeRed:
            [self addLogWithText:[NSString stringWithFormat:@"Notification body: %@", notification.alertMessage] textColor:[UIColor redColor]];
            break;
            
        case ALERT_TYPE_CodeGreen:
            [self addLogWithText:[NSString stringWithFormat:@"Notification body: %@", notification.alertMessage] textColor:[UIColor greenColor]];
            break;
            
        case ALERT_TYPE_CodeYellow:
            [self addLogWithText:[NSString stringWithFormat:@"Notification body: %@", notification.alertMessage] textColor:[UIColor yellowColor]];
            break;
            
        default:
            break;
    }
}

#pragma mark - KaaClientStateDelegate

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

#pragma mark - ProfileContainer

- (KAAEmptyData *)getProfile {
    return [[KAAEmptyData alloc] init];
}

#pragma mark - UITextFieldDelegate

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    @try {
        [self.kaaClient subscribeToTopicWithId:self.topicIdTextField.text.intValue];
        [self addLogWithText:[NSString stringWithFormat:@"Subscribed to optional topic with id %d", self.topicIdTextField.text.intValue]];
    } @catch (NSException *exception) {
        [self addLogWithText:[NSString stringWithFormat:@"Topic with id %d is unavaliable. Can't subscribe", self.topicIdTextField.text.intValue]];
    }
    [textField resignFirstResponder];
    return YES;
}

#pragma mark - Supporting methods

- (void)showTopicList:(NSArray *)topics {
    if (topics.count == 0) {
        [self addLogWithText:@"Topic list is empty"];
    } else {
        for (Topic *topic in topics) {
            [self addLogWithText:[NSString stringWithFormat:@"Topic id:%lld name:%@ type:%u", topic.id, topic.name, topic.subscriptionType]];
        }
    }
}

- (NSArray *)extractOptionalTopicIds:(NSArray *)topics {
    NSMutableArray *topicIds = [NSMutableArray array];
    for (Topic *topic in topics) {
        if (topic.subscriptionType == SUBSCRIPTION_TYPE_OPTIONAL_SUBSCRIPTION) {
            [topicIds addObject:@(topic.id)];
        }
    }
    return topicIds;
}

- (void)addLogWithText:(NSString *)text {
    [self addLogWithText:text textColor:nil];
}

- (void)addLogWithText:(NSString *)text textColor:(UIColor *)textColor {
    NSMutableAttributedString *newLog = [[NSMutableAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@\n", text]];
    [newLog addAttribute:NSFontAttributeName value:[UIFont systemFontOfSize:14.f] range:NSMakeRange(0, newLog.length)];
    UIColor *color = (textColor == nil) ? [UIColor whiteColor] : textColor;
    [newLog addAttribute:NSForegroundColorAttributeName value:color range:NSMakeRange(0, newLog.length)];
    [self.log appendAttributedString:newLog];
    NSLog(@"%@", text);
    dispatch_async(dispatch_get_main_queue(), ^{
        self.logTextView.attributedText = self.log;
    });
}

@end
