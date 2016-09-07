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

#import <Foundation/Foundation.h>


static NSString *RoomsListUpdated = @"RoomsListUpdated";
static NSString *MessagesListUpdated = @"MessagesListUpdated";


@import Kaa;


@interface ChatClientManager : NSObject <KaaClientStateDelegate, UserAttachDelegate, ProfileContainer>
{
    NSMutableArray *addedRooms;
    NSMutableArray *joinedRooms;

}

@property (nonatomic, strong) id<KaaClient> kaaClient;


+ (id)sharedManager;
- (void)sendMessage:(NSString *)message room:(NSString *)roomName;
- (NSArray *)roomsList;
- (BOOL)isJoinedRoom:(NSString *)room;
- (BOOL)canRemoveRoom:(NSString *)room;
- (void)addRoom:(NSString *)room;
- (void)deleteRoom:(NSString *)room;
- (void)joinRoom:(NSString *)room;
- (void)leaveRoom:(NSString *)room;


- (NSArray *)messagesForRoom:(NSString *)roomName;

@end
