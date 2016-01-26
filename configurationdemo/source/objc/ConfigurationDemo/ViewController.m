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
#import "Kaa/SimpleConfigurationStorage.h"

@interface ViewController () <KaaClientStateDelegate, ConfigurationDelegate, ProfileContainer>

@property (weak, nonatomic) IBOutlet UITextView *logTextView;

@property (nonatomic, strong) id<KaaClient> kaaClient;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    [self addLogWithText:@"ConfigurationDemo started"];
    // Create the Kaa desktop context for the application.
    DefaultKaaPlatformContext *defaultKaaPlatfromContext = [[DefaultKaaPlatformContext alloc] init];
    
    // Create a Kaa client and add a listener which displays the Kaa client configuration
    // as soon as the Kaa client is started.
    self.kaaClient = [Kaa clientWithContext:defaultKaaPlatfromContext stateDelegate:self];
    
    // Persist configuration in a local storage to avoid downloading it each time the Kaa client is started.
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    [self.kaaClient setConfigurationStorage:[[SimpleConfigurationStorage alloc] initWithPlatformContext:defaultKaaPlatfromContext path:[documentsDirectory stringByAppendingPathComponent:@"savedconfig.cfg"]]];
    
    // Add a listener which displays the Kaa client configuration each time it is updated.
    [self.kaaClient addConfigurationDelegate:self];
    
    [self.kaaClient setProfileContainer:self];
    
    // Start the Kaa client and connect it to the Kaa server.
    [self.kaaClient start];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Delegate methods

- (void)onStarted {
    [self addLogWithText:@"Kaa client started"];
    [self displayConfiguration];
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

- (KAAEmptyData *)getProfile {
    return [[KAAEmptyData alloc] init];
}

- (void)onConfigurationUpdate:(KAASampleConfiguration *)configuration {
    [self addLogWithText:@"Configuration was updated"];
    [self displayConfiguration];
}

- (void)displayConfiguration {
    KAASampleConfiguration *configration = [self.kaaClient getConfiguration];
    NSArray *links = [configration AddressList].data;
    NSMutableString *confBody = [NSMutableString stringWithFormat:@"Configuration body :"];
    for (KAALink *link in links) {
        [confBody appendString:[NSString stringWithFormat:@"\n%@ - %@", link.label, link.url]];
    }
    [self addLogWithText:confBody];
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
