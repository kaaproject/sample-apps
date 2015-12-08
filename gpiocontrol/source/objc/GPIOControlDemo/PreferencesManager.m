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

#import "PreferencesManager.h"
#define USER_EXTERNAL_ID @"user_external_id"
#define USER_ACCESS_TOKEN @"user_access_token"


@implementation PreferencesManager

+ (void) setUserAccessToken:(NSString *)token {
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    [userDefaults setObject:token forKey:USER_ACCESS_TOKEN];
}

+ (NSString *)getUserAccessToken {
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    NSString *string = [userDefaults objectForKey:USER_ACCESS_TOKEN];
    return string;
}
+ (void) setUserExternalId:(NSString *)extId {
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    [userDefaults setObject:extId forKey:USER_EXTERNAL_ID];
}
+ (NSString *) getUserExternalId {
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    NSString *string = [userDefaults objectForKey:USER_EXTERNAL_ID];
    return string;
}

@end
