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

static const int32_t temperatureLowerLimit = 25;
static const int32_t temperatureUpperLimit = 35;

@interface ViewController () <KaaClientStateDelegate, ProfileContainer, ConfigurationDelegate>

@property (nonatomic, weak) IBOutlet UITextView *logTextView;

@property (nonatomic, strong) id<KaaClient> kaaClient;
@property (nonatomic, weak) NSTimer *logTimer;
@property (nonatomic, strong) NSMutableDictionary *bucketRunnersDictionary;
@property (nonatomic, strong) NSOperationQueue *bucketRunnersQueue;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    [self addLogWithText:@"DataCollectionDemo started"];

    // Create a Kaa client with the Kaa default context.
    self.kaaClient = [KaaClientFactory clientWithContext:[[DefaultKaaPlatformContext alloc] init] stateDelegate:self];
    
    // Set a custom strategy for uploading logs.
    // The default strategy uploads logs after either a threshold logs count
    // or a threshold logs size has been reached.
    // The following custom strategy uploads every log record as soon as it is created.
    [self.kaaClient setLogUploadStrategy:[[RecordCountLogUploadStrategy alloc] initWithCountThreshold:1]];
    
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *configurationPath = [[paths objectAtIndex:0] stringByAppendingPathComponent:@"savedconfig.cfg"];
    
    // Persist configuration in a local storage to avoid downloading it each time the Kaa client is started.
    [self.kaaClient setConfigurationStorage:[SimpleConfigurationStorage storageWithPath:configurationPath]];
    
    // Add a listener which displays the Kaa client configuration each time it is updated.
    [self.kaaClient addConfigurationDelegate:self];
    
    [self.kaaClient setProfileContainer:self];

    // Start the Kaa client and connect it to the Kaa server.
    [self.kaaClient start];
    
    self.bucketRunnersDictionary = [NSMutableDictionary dictionary];
    self.bucketRunnersQueue = [[NSOperationQueue alloc] init];
    
    // Schedules timer to generate logs with delay, which was set in configuration.
    [self repeatedTimerWithTimeInterval:1];
    [self repeatedTimerWithTimeInterval:[self.kaaClient getConfiguration].samplePeriod];
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

- (KAAProfileEmptyData *)getProfile {
    return [[KAAProfileEmptyData alloc] init];
}

#pragma mark - ConfigurationDelegate

- (void)onConfigurationUpdate:(KAAConfigurationConfiguration *)configuration {
    [self addLogWithText:[NSString stringWithFormat:@"Configuration update received. New log threshold is %d", configuration.samplePeriod]];
    dispatch_async(dispatch_get_main_queue(), ^{
        // Schedules the new log timer with updated threshold.
        [self repeatedTimerWithTimeInterval:configuration.samplePeriod];
    });
}

#pragma mark - Supporting methods

- (void)repeatedTimerWithTimeInterval:(NSTimeInterval)timeInterval {
    if (timeInterval <= 0) {
        [self addLogWithText:[NSString stringWithFormat:@"Sample period value %f in updated configuration is wrong, so ignore it.", timeInterval]];
    } else {
        if (self.logTimer) {
            [self.logTimer invalidate];
            self.logTimer = nil;
        }
        self.logTimer = [NSTimer scheduledTimerWithTimeInterval:timeInterval target:self selector:@selector(generateAndSendLogRecord) userInfo:nil repeats:YES];
    }
}

- (void)generateAndSendLogRecord {
    KAALoggingDataCollection *log = [[KAALoggingDataCollection alloc] init];
    log.temperature = (arc4random() % (temperatureUpperLimit - temperatureLowerLimit)) + temperatureLowerLimit;
    log.timeStamp = CACurrentMediaTime() * 1000;
    [self addLogWithText:[NSString stringWithFormat:@"Log sent with temperature: %d, timestamp: %lld", log.temperature, log.timeStamp]];
    
    self.bucketRunnersDictionary[@(log.timeStamp)] = [self.kaaClient addLogRecord:log];
    [self getBuckerInfoForRecordWithTimeStamp:log.timeStamp];
}
 
- (void)getBuckerInfoForRecordWithTimeStamp:(int64_t)timeStamp {
    @try {
        [self.bucketRunnersQueue addOperationWithBlock:^{
            BucketRunner *runner = self.bucketRunnersDictionary[@(timeStamp)];
            BucketInfo *bucketInfo = [runner getValue];
            int64_t timeSpent = bucketInfo.scheduledBucketRunnerTimestamp - timeStamp + bucketInfo.bucketDeliveryDuration;
            [self addLogWithText:[NSString stringWithFormat:@"Received log record delivery info. Bucket id [%d], delivery time [%lld ms]", bucketInfo.bucketId, timeSpent]];
        }];
    } @catch (NSException *exception) {
        [self addLogWithText:@"Exception was caught while waiting for callback future"];
    }
}

- (void)addLogWithText:(NSString *) text {
    NSLog(@"%@", text);
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        self.logTextView.text = [NSString stringWithFormat:@"%@%@\n", self.logTextView.text, text];
    }];
}

@end
