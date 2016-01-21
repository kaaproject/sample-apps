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

#import "Device.h"
#import "EventGen.h"

@interface Device()

@end

@implementation Device

- (instancetype)initWithModel:(NSString *)model DeviceName:(NSString *)deviceName KaaEndpointId:(NSString *)endpointId andGPIOStatuses:(NSArray *)gpioStatuses {
    self = [super init];
    if (self) {
        self.gpioStatuses = [NSMutableDictionary dictionary];
        self.deviceName = deviceName;
        self.model = model;
        self.kaaEndpointId = endpointId;
        [self setGpioStatusesFromArray:gpioStatuses];
    }
    return self;
}

- (void)setGpioStatusesFromArray:(NSArray *)gpioStatusesArray {
    for (GpioStatus *status in gpioStatusesArray) {
        [self.gpioStatuses setObject:[NSNumber numberWithBool:status.status] forKey:[NSNumber numberWithInt:status.id]];
    }
}

- (NSArray *)getGpioStatuses {
    NSMutableArray *gpioStatuses = [NSMutableArray array];
    for (NSNumber *key in self.gpioStatuses.allKeys) {
        GpioStatus *status = [[GpioStatus alloc] init];
        status.id = key.intValue;
        status.status = [[self.gpioStatuses objectForKey:key] boolValue];
        [gpioStatuses addObject:status];
    }
    return gpioStatuses;
}

- (BOOL)isEqual:(id)object {
    if (!object) {
        return NO;
    }
    Device *device = object;
    if (self.model) {
        return [self.model isEqualToString:device.model] ? YES : NO;
    }
    if (self.deviceName) {
        return [self.deviceName isEqualToString:device.deviceName] ? YES : NO;
    }
    if (self.gpioStatuses) {
        return [self.gpioStatuses isEqualToDictionary:device.gpioStatuses] ? YES : NO;
    }
    return !(self.kaaEndpointId ? ![self.kaaEndpointId isEqualToString:device.kaaEndpointId] : device.kaaEndpointId != nil);
}

- (NSUInteger)hash {
    NSUInteger hash = self.model != nil ? [self.model hash] : 0;
    hash = hash * 31 + (self.deviceName != nil ? [self.deviceName hash] : 0);
    hash = hash * 31 + (self.gpioStatuses != nil ? [self.gpioStatuses hash] : 0);
    hash = hash * 31 + (self.kaaEndpointId != nil ? [self.kaaEndpointId hash] : 0);
    return hash;
}

@end
