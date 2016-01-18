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

#import <Foundation/Foundation.h>
#import "Kaa/Kaa.h"

@interface ConcreteStateDelegate : NSObject <KaaClientStateDelegate>

@end

@interface KaaProvider : NSObject <UserAttachDelegate>

@property (nonatomic, strong) volatile id<KaaClient> kaaClient;

+ (id<KaaClient>)getClient;
- (void)attachUser;
+ (void)setUpEventDelegate:(id <RemoteControlECFDelegate>)delegate;
+ (void)sendDeviceInfoRequestToAll;

@end
