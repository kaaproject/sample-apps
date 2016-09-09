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

/*
 *  Class ChatClientManager is a Singletone class.
 *  It's responsible for the chat logic: sending messages,
 *  creating and removing chat rooms.
 *
 *  Handles Kaa client initialization and delegate methods
 */

@interface ChatClientManager : NSObject <KaaClientStateDelegate, UserAttachDelegate, ProfileContainer,ChatDelegate>
{
    NSMutableDictionary *messages;
}

@property (nonatomic, strong) id<KaaClient> kaaClient;
@property (nonatomic, strong) Chat *chatEventFamily;

@property (nonatomic, strong) NSMutableArray *rooms;
@property (nonatomic, strong) NSArray *defaultRooms;

+ (ChatClientManager *)sharedManager;

- (NSArray *)messagesForRoom:(NSString *)roomName;
- (void)sendMessage:(NSString *)message room:(NSString *)roomName;
- (void)createRoom:(NSString *)name onlyLocal:(BOOL)onlyLocal;
- (void)deleteRoom:(NSString *)room onlyLocal:(BOOL)onlyLocal;

@end
