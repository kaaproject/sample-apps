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


+ (ChatClientManager *)sharedManager //Singletone
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
        self.defaultRooms = @[@"Living", @"Guest"];
        self.rooms = [NSMutableArray arrayWithArray:_defaultRooms];
        messages = [NSMutableDictionary dictionary];
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
    [self.kaaClient attachUserWithId:USER_EXTERNAL_ID
                         accessToken:USER_ACCESS_TOKEN delegate:self];
}

#pragma mark - Kaa Delegates methods

- (void)onStarted
{
    [self addLog:@"Kaa client started"];
}

- (void)onAttachResult:(UserAttachResponse *)response
{    
    if (response.result == SYNC_RESPONSE_RESULT_TYPE_SUCCESS)
    {
        [self addLog:@"Endpoint successfully attached!"];
        [self onUserAttached];
    }
    else
    {
        [self addLog:@"Endpoint attach failed: event demo stopped"];
    }
}


- (void)onChatEvent:(ChatEvent *)event fromSource:(NSString *)source
{
    [self addLog:@"Got chat event"];
    if(event.EventType == CHAT_EVENT_TYPE_CREATE)
    {   //Handling CREATE chat event
        [self createRoom:event.ChatName onlyLocal:YES];
    }
    else if(event.EventType == CHAT_EVENT_TYPE_DELETE)
    {   //Handling DELETE chat event
        [self deleteRoom:event.ChatName onlyLocal:YES];
    }
}


- (void)onMessage:(Message *)event fromSource:(NSString *)source
{
    [self addLog:@"Got chat message"];
    [self saveMessage:event.Message toRoom:event.ChatName];
}


- (KAAEmptyData *)getProfile
{
    return [[KAAEmptyData alloc] init];
}

- (void)onUserAttached
{
    //Obtain the event family factory.
    EventFamilyFactory *eventFamilyFactory = [self.kaaClient getEventFamilyFactory];
    
    //Obtain the concrete event family.
    self.chatEventFamily = [eventFamilyFactory getChat];
    [self.chatEventFamily addDelegate:self];
}


- (void)createRoom:(NSString *)name onlyLocal:(BOOL)onlyLocal
{
    if(name.isEmpty == NO)
    {
        if(onlyLocal == NO)
        {
            //create and send CREATE chat event to the Kaa
            ChatEvent *addEvent = [[ChatEvent alloc] initWithChatName:name
                                                            EventType:CHAT_EVENT_TYPE_CREATE];
            [_chatEventFamily sendChatEventToAll:addEvent];
        }
        [_rooms addObject:name];
        [self notify:RoomsListUpdated];
    }
}

- (void)deleteRoom:(NSString *)room  onlyLocal:(BOOL)onlyLocal
{
    if([_rooms containsObject:room])
    {
        if(onlyLocal == NO)
        {
            //create and send DELETE chat event to the Kaa
            ChatEvent *addEvent = [[ChatEvent alloc] initWithChatName:room
                                                            EventType:CHAT_EVENT_TYPE_DELETE];
            [_chatEventFamily sendChatEventToAll:addEvent];
        }
        [_rooms removeObject:room];
        [self notify:RoomsListUpdated];
    }
}


- (void)sendMessage:(NSString *)message room:(NSString *)roomName
{
    [self saveMessage:message toRoom:roomName];
    //Creating Message event ovject
    Message *msg = [[Message alloc] initWithChatName:roomName Message:message];
    //Sending Message event to Kaa
    [self.chatEventFamily sendMessageToAll:msg];
}

#pragma mark - Chat helper methods

- (void)saveMessage:(NSString *)msg toRoom:(NSString *)room
{
    NSMutableArray *arr = [NSMutableArray arrayWithArray:messages[room]];
    [arr addObject:msg];
    messages[room] = arr;
    [self notify:MessagesListUpdated];
}

- (void)addLog:(NSString *)text
{
    NSLog(@"%@", text);
}

- (NSArray *)messagesForRoom:(NSString *)roomName
{
    return messages[roomName];
}

- (void)notify:(NSString *)name
{
    [[NSNotificationCenter defaultCenter] postNotificationName:name object:nil];
}


@end
