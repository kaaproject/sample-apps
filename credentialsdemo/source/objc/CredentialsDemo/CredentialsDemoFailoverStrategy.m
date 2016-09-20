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

#import "CredentialsDemoFailoverStrategy.h"

@interface CredentialsDemoFailoverStrategy ()

@property (nonatomic) int64_t noConnectivityRetryPeriod;

@end

@implementation CredentialsDemoFailoverStrategy

- (FailoverDecision *)decisionOnFailoverStatus:(FailoverStatus)status {
    switch (status) {
        case FailoverStatusBootstrapServersNotAvailable:
            return [[FailoverDecision alloc] initWithFailoverAction:FailoverActionRetry
                                                        retryPeriod:self.bootstrapServersRetryPeriod
                                                           timeUnit:self.timeUnit];
        case FailoverStatusCurrentBootstrapServerNotAvailable:
            return [[FailoverDecision alloc] initWithFailoverAction:FailoverActionUseNextBootstrap
                                                        retryPeriod:self.bootstrapServersRetryPeriod
                                                           timeUnit:self.timeUnit];
        case FailoverStatusNoOperationsServersReceived:
            return [[FailoverDecision alloc] initWithFailoverAction:FailoverActionUseNextBootstrap
                                                        retryPeriod:self.bootstrapServersRetryPeriod
                                                           timeUnit:self.timeUnit];
        case FailoverStatusOperationsServersNotAvailable:
            return [[FailoverDecision alloc] initWithFailoverAction:FailoverActionRetry
                                                        retryPeriod:self.operationsServersRetryPeriod
                                                           timeUnit:self.timeUnit];
        case FailoverStatusNoConnectivity:
            return [[FailoverDecision alloc] initWithFailoverAction:FailoverActionRetry
                                                        retryPeriod:self.noConnectivityRetryPeriod
                                                           timeUnit:self.timeUnit];
        case FailoverStatusEndpointCredentialsRevoked:
            NSLog(@"Credentials were rejected. Client can't register on cluster");
            return [[FailoverDecision alloc] initWithFailoverAction:FailoverActionNoop];
            
        case FailoverStatusEndpointVerificationFailed:
            return [[FailoverDecision alloc] initWithFailoverAction:FailoverActionRetry];
        default:
            return [[FailoverDecision alloc] initWithFailoverAction:FailoverActionNoop];
    }
}

@end
