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

#import "ChatClientManager.h"

#define USER_EXTERNAL_ID    @"user@email.com"
#define USER_ACCESS_TOKEN   @"token"


@implementation ChatClientManager


+ (id)sharedManager
{
    static ChatClientManager *sharedMyManager = nil;
    @synchronized(self) {
        if (sharedMyManager == nil)
            sharedMyManager = [[self alloc] init];
    }
    return sharedMyManager;
}

- (id)init
{
    self = [super init];
    if(self)
    {
        [self initializeKaa];
        addedRooms = [NSMutableArray array];
        joinedRooms = [self defaultRoomsList].mutableCopy;
        [addedRooms addObject:@"test"];
    }
    return self;
}


- (void)initializeKaa
{
    //Create a Kaa client with the Kaa default context.
    self.kaaClient = [KaaClientFactory clientWithContext:[[DefaultKaaPlatformContext alloc] init] stateDelegate:self];
    [self.kaaClient setProfileContainer:self];
    
    // Start the Kaa client and connect it to the Kaa server.
    [self.kaaClient start];
    
    //Attaching new user
    [self.kaaClient attachUserWithId:USER_EXTERNAL_ID accessToken:USER_ACCESS_TOKEN delegate:self];
}


- (KAAEmptyData *)getProfile {
    return [[KAAEmptyData alloc] init];
}

- (void)addLog:(NSString *)text {
    NSLog(@"%@", text);
}


- (NSArray *)roomsList
{
    return [[self defaultRoomsList] arrayByAddingObjectsFromArray:addedRooms];
}

- (NSArray *)defaultRoomsList
{
    return @[@"Living", @"Guest"];
}

- (NSArray *)messagesForRoom:(NSString *)roomName
{
    return @[@"Hi there", @"wsup", @"how are you?"];
}

- (void)addRoom:(NSString *)name
{
    if(name.isEmpty == NO)
    {
        [addedRooms addObject:name];
        [self notifyListUpdated];
    }
}

- (void)deleteRoom:(NSString *)room
{
    [addedRooms removeObject:room];
    [self notifyListUpdated];
}

- (void)joinRoom:(NSString *)room
{
    [joinedRooms addObject:room];
    [self notifyListUpdated];
}

- (void)leaveRoom:(NSString *)room
{
    [joinedRooms removeObject:room];
    [self notifyListUpdated];
}

- (void)notifyListUpdated
{
    [[NSNotificationCenter defaultCenter] postNotificationName:RoomsListUpdated object:nil];
}

- (BOOL)canRemoveRoom:(NSString *)room
{
    return [addedRooms containsObject:room];
}

- (void)sendMessage:(NSString *)message room:(NSString *)roomName
{
    
}

- (BOOL)isJoinedRoom:(NSString *)room
{
    return [joinedRooms containsObject:room];
}



@end
