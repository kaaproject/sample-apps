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

#define LOGS_TO_SEND_COUNT 5

@interface ViewController () <KaaClientStateDelegate, ProfileContainer>

@property (nonatomic, weak) IBOutlet UITextView *logTextView;

@property (nonatomic, strong) id<KaaClient> kaaClient;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    [self addLogWithText:@"DataCollectionDemo started"];

    //Create a Kaa client with the Kaa default context.
    self.kaaClient = [Kaa clientWithContext:[[DefaultKaaPlatformContext alloc] init] stateDelegate:self];

    // Set a custom strategy for uploading logs.
    // The default strategy uploads logs after either a threshold logs count
    // or a threshold logs size has been reached.
    // The following custom strategy uploads every log record as soon as it is created.
    [self.kaaClient setLogUploadStrategy:[[RecordCountLogUploadStrategy alloc] initWithCountThreshold:1]];
    [self.kaaClient setProfileContainer:self];

    // Start the Kaa client and connect it to the Kaa server.
    [self.kaaClient start];

    // Send logs in a loop.
    NSArray *logs = [self generateLogs:LOGS_TO_SEND_COUNT];

    [self addLogWithText:[NSString stringWithFormat:@"Record size: %ld", (long)[self getLogRecordSize:logs[0]]]];

    // Collect log record delivery runners.
    NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];

    for (KAALogData *log in logs) {
        dictionary[@(log.timeStamp)] = [self.kaaClient addLogRecord:log];
        [self addLogWithText:[NSString stringWithFormat:@"Log sent: loglevel: %u, tag: %@, message: %@, timestamp: %lld",
                              log.level, log.tag, log.message, log.timeStamp]];
    }

    for (NSNumber *timeKey in dictionary.allKeys) {
        @try {
            [[[NSOperationQueue alloc] init] addOperationWithBlock:^{
                BucketRunner *runner = dictionary[timeKey];
                BucketInfo *bucketInfo = [runner getValue];
                int64_t timeSpent = bucketInfo.scheduledBucketTimestamp - [timeKey longLongValue] + bucketInfo.bucketDeliveryDuration;
                [self addLogWithText:[NSString stringWithFormat:@"Received log record delivery info. Bucket id [%d], delivery time [%lld ms]", bucketInfo.bucketId, timeSpent]];
            }];
        }
        @catch (NSException *exception) {
            [self addLogWithText:@"Exception was caught while waiting for callback future"];
        }
    }
}

- (NSInteger)getLogRecordSize:(KAALogData *)record {
    AvroBytesConverter *converter = [[AvroBytesConverter alloc] init];
    NSData *serializedLogRecod = [converter toBytes:record];
    return serializedLogRecod.length;
}

- (NSArray *)generateLogs:(int)logCount {
    NSMutableArray *logs = [NSMutableArray arrayWithCapacity:logCount];
    for (int i = 0; i < logCount; i++) {
        KAALogData *log = [[KAALogData alloc] init];
        log.level = LEVEL_KAA_INFO;
        log.tag = @"iOS";
        log.message = [NSString stringWithFormat:@"MESSAGE_%d", i];
        log.timeStamp = CACurrentMediaTime() * 1000;
        [logs addObject:log];
    }
    return logs;
}


#pragma mark - Delegate methods

- (void)onStarted {
    [self addLogWithText:@"Kaa client started"];
}

- (KAAEmptyData *)getProfile {
    return [[KAAEmptyData alloc] init];
}

- (void)addLogWithText:(NSString *) text {
    NSLog(@"%@", text);
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        self.logTextView.text = [NSString stringWithFormat:@"%@%@\n", self.logTextView.text, text];
    }];
}


@end
