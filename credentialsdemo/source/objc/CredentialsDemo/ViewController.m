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

#define LOGS_TO_SEND_COUNT 5

@interface ViewController () <KaaClientStateDelegate, ProfileContainer>

@property (nonatomic, weak) IBOutlet UITextView *logTextView;

@property (nonatomic, strong) id<KaaClient> kaaClient;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    [self addLogWithText:@"CredentialsDemo started"];

    //Create a Kaa client with the Kaa default context.
    self.kaaClient = [KaaClientFactory clientWithContext:[[DefaultKaaPlatformContext alloc] init] stateDelegate:self];
    
    [self.kaaClient setProfileContainer:self];

    // Start the Kaa client and connect it to the Kaa server.
    [self.kaaClient start];
}

- (void)onStarted {
    [self addLogWithText:@"Kaa client started"];
}

- (KAAProfileEmptyData *)getProfile {
    return [[KAAProfileEmptyData alloc] init];
}

- (void)addLogWithText:(NSString *) text {
    NSLog(@"%@", text);
    dispatch_async(dispatch_get_main_queue(), ^{
        self.logTextView.text = [NSString stringWithFormat:@"%@%@\n", self.logTextView.text, text];
    });
}

@end
