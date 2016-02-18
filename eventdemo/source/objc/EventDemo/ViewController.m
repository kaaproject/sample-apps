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
#import <Kaa/Kaa.h>

#define USER_EXTERNAL_ID    @"user@email.com"
#define USER_ACCESS_TOKEN   @"token"

@interface ViewController () <KaaClientStateDelegate, UserAttachDelegate, ThermostatEventClassFamilyDelegate, FindEventListenersDelegate, ProfileContainer>

@property (nonatomic, weak) IBOutlet UITextView *logTextView;

@property (nonatomic, strong) id<KaaClient> kaaClient;
@property (nonatomic, strong) ThermostatEventClassFamily *tecf;
@property (nonatomic, strong) EventFamilyFactory *eventFamilyFactory;

- (void)onUserAttached;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self addLogWithText:@"Event demo started"];
    
    //Create a Kaa client with the Kaa default context.
    self.kaaClient = [Kaa clientWithContext:[[DefaultKaaPlatformContext alloc] init] stateDelegate:self];
    [self.kaaClient setProfileContainer:self];
    
    // Start the Kaa client and connect it to the Kaa server.
    [self.kaaClient start];
    
    //Attaching new user
    [self.kaaClient attachUserWithId:USER_EXTERNAL_ID accessToken:USER_ACCESS_TOKEN delegate:self];
}

#pragma mark - Delegates methods

- (void)onAttachResult:(UserAttachResponse *)response {
    [self addLogWithText:[NSString stringWithFormat:@"Attach response: %i", response.result]];
    
    if (response.result == SYNC_RESPONSE_RESULT_TYPE_SUCCESS) {
        [self addLogWithText:@"Endpoint successfully attached!"];
        [self onUserAttached];
    } else {
        [self addLogWithText:@"Endpoint attach failed: event demo stopped"];
    }
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

- (void)onThermostatInfoRequest:(ThermostatInfoRequest *)event fromSource:(NSString *)source {
    [self addLogWithText:[NSString stringWithFormat:@"onThermostatInfoRequest event received! Sender: %@", source]];
    
    ThermostatInfo *info = [[ThermostatInfo alloc] init];
    info.degree = [KAAUnion unionWithBranch:KAA_UNION_INT_OR_NULL_BRANCH_0 data:@(-95)];
    info.targetDegree = [KAAUnion unionWithBranch:KAA_UNION_INT_OR_NULL_BRANCH_0 data:@(-96)];
    info.isSetManually = [KAAUnion unionWithBranch:KAA_UNION_BOOLEAN_OR_NULL_BRANCH_0 data:@(YES)];
    
    ThermostatInfoResponse *response = [[ThermostatInfoResponse alloc] init];
    response.thermostatInfo = [KAAUnion unionWithBranch:KAA_UNION_THERMOSTAT_INFO_OR_NULL_BRANCH_0 data:info];
    
    [self.tecf sendThermostatInfoResponse:response to:source];

}

- (void)onThermostatInfoResponse:(ThermostatInfoResponse *)event fromSource:(NSString *)source {
    [self addLogWithText:[NSString stringWithFormat:@"ThermostatInfoResponse event received! Thermostat info: %@, sender: %@", (ThermostatInfo *)event.thermostatInfo.data, source]];
}

- (void)onChangeDegreeRequest:(ChangeDegreeRequest *)event fromSource:(NSString *)source {
    [self addLogWithText:[NSString stringWithFormat:@"ChangeDegreeRequest event received! change temperature by %@ degrees, sender: %@", event.degree.data, source]];
}

- (void)onUserAttached {
    NSArray *listenerFQNs = [NSArray arrayWithObjects:[ThermostatInfoRequest FQN], [ChangeDegreeRequest FQN], nil];
    
    //Obtain the event family factory.
    self.eventFamilyFactory = [self.kaaClient getEventFamilyFactory];
    //Obtain the concrete event family.
    self.tecf = [self.eventFamilyFactory getThermostatEventClassFamily];
    
    // Broadcast the ChangeDegreeRequest event.
    ChangeDegreeRequest *changeDegree = [[ChangeDegreeRequest alloc] init];
    changeDegree.degree = [KAAUnion unionWithBranch:KAA_UNION_INT_OR_NULL_BRANCH_0 data:@(-97)];
    [self.tecf sendChangeDegreeRequestToAll:changeDegree];
    [self addLogWithText:@"Broadcast ChangeDegreeRequest sent"];
    
    // Add event listeners to the family factory.
    [self.tecf addDelegate:self];
    
    //Find all the listeners listening to the events from the FQNs list.
    [self.kaaClient findListenersForEventFQNs:listenerFQNs delegate:self];
}

- (void)onEventListenersReceived:(NSArray *)eventListeners {
    [self addLogWithText:[NSString stringWithFormat:@"%i event listeners received", (int)[eventListeners count]]];
    for (NSString *listener in eventListeners) {
        TransactionId *trxId = [self.eventFamilyFactory startEventsBlock];
        // Add a targeted events to the block.
        [self.tecf addThermostatInfoRequestToBlock:[[ThermostatInfoRequest alloc] init] withTransactionId:trxId target:listener];
        ChangeDegreeRequest *request = [[ChangeDegreeRequest alloc] init];
        request.degree = [KAAUnion unionWithBranch:KAA_UNION_INT_OR_NULL_BRANCH_0 data:@(-98)];
        [self.tecf addChangeDegreeRequestToBlock:request withTransactionId:trxId target:listener];
        
        // Send the added events in a batch.
        [self.eventFamilyFactory submitEventsBlockWithTransactionId:trxId];
        [self addLogWithText:[NSString stringWithFormat:@"ThermostatInfoRequest & ChangeDegreeRequest sent to endpoint with id [%@]", listener]];
        // Dismiss the event batch (if the batch was not submitted as shown in the previous line).
        //[self.eventFamilyFactory removeEventsBlock:trxId];
    }
}

- (void)onRequestFailed {
    [self addLogWithText:@"Request failed!"];
}

#pragma mark - Supporting methods

- (KAAEmptyData *)getProfile {
    return [[KAAEmptyData alloc] init];
}

- (void)addLogWithText:(NSString *)text {
    NSLog(@"%@", text);
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        self.logTextView.text = [NSString stringWithFormat:@"%@%@\n", self.logTextView.text, text];
    }];
}

@end
