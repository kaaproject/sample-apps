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

#define USER_EXTERNAL_ID    @"userid"
#define USER_ACCESS_TOKEN   @"token"

@interface ChatClientManager ()

@property (nonatomic, strong) NSMutableDictionary *messages;

@end

@implementation ChatClientManager


+ (ChatClientManager *)sharedManager { // Singletone
    static ChatClientManager *sharedMyManager = nil;
    @synchronized(self) {
        if (sharedMyManager == nil)
            sharedMyManager = [[self alloc] init];
    }
    return sharedMyManager;
}

- (id)init {
    self = [super init];
    if (self)
    {
        [self initializeKaa];
        self.defaultRooms = @[@"Living room", @"Guest room"];
        self.rooms = [NSMutableArray arrayWithArray:_defaultRooms];
        self.messages = [NSMutableDictionary dictionary];
    }
    return self;
}

- (void)initializeKaa {
    // Create a Kaa client with the Kaa default context.
    self.kaaClient = [KaaClientFactory
                      clientWithContext:[[DefaultKaaPlatformContext alloc] init]
                      stateDelegate:self];
    [self.kaaClient setProfileContainer:self];
    
    // Start the Kaa client and connect it to the Kaa server.
    [self.kaaClient start];
    
    // Attaching new user
    [self.kaaClient attachUserWithId:USER_EXTERNAL_ID
                         accessToken:USER_ACCESS_TOKEN
                            delegate:self];
}

#pragma mark - KaaClientStateDelegate

- (void)onStarted {
    NSLog(@"Kaa client started");
}

#pragma mark - UserAttachDelegate

- (void)onAttachResult:(UserAttachResponse *)response {
    if (response.result == SYNC_RESPONSE_RESULT_TYPE_SUCCESS) {
        NSLog(@"Endpoint successfully attached!");
        // Obtain the event family factory.
        EventFamilyFactory *eventFamilyFactory = [self.kaaClient getEventFamilyFactory];
        
        // Obtain the concrete event family.
        self.chatEventFamily = [eventFamilyFactory getChat];
        [self.chatEventFamily addDelegate:self];
    } else {
        NSLog(@"Endpoint attach failed: event demo stopped");
    }
}

#pragma mark - ChatDelegate

- (void)onKAAEventChatEvent:(KAAEventChatEvent *)event fromSource:(NSString *)source {
    NSLog(@"Got chat event");
    
    // Handling chat event
    switch (event.EventType) {
        case CHAT_EVENT_TYPE_CREATE:
            // Handling CREATE chat event
            [self createRoom:event.ChatName onlyLocal:YES];
            break;
            
        case CHAT_EVENT_TYPE_DELETE:
            // Handling CREATE chat event
            [self deleteRoom:event.ChatName onlyLocal:YES];
            break;
    }
}

- (void)onKAAEventMessage:(KAAEventMessage *)event fromSource:(NSString *)source {
    NSLog(@"Got chat message");
    [self saveMessage:event.Message toRoom:event.ChatName];
}

#pragma mark - ProfileContainer

- (KAAProfileEmptyData *)getProfile {
    return [[KAAProfileEmptyData alloc] init];
}

#pragma mark - Sending Kaa Events logic

- (void)createRoom:(NSString *)name onlyLocal:(BOOL)onlyLocal {
    if (name.isEmpty == NO) { // chat room name should not be empty
        if (onlyLocal == NO) { // Check if we don't need to save it only locally
            // create and send CREATE chat event to the Kaa
            KAAEventChatEvent *addEvent = [[KAAEventChatEvent alloc] initWithChatName:name
                                                            EventType:CHAT_EVENT_TYPE_CREATE];
            [self.chatEventFamily sendKAAEventChatEventToAll:addEvent];
        }
        [self.rooms addObject:name];
        [self notify:RoomsListUpdated];
    }
}

- (void)deleteRoom:(NSString *)room  onlyLocal:(BOOL)onlyLocal {
    if ([self.rooms containsObject:room]) {
        if (onlyLocal == NO) // Check if we don't need to save it only locally
        {
            // create and send DELETE chat event to the Kaa
            KAAEventChatEvent *addEvent = [[KAAEventChatEvent alloc] initWithChatName:room
                                                            EventType:CHAT_EVENT_TYPE_DELETE];
            [self.chatEventFamily sendKAAEventChatEventToAll:addEvent];
        }
        [self.rooms removeObject:room];
        [self notify:RoomsListUpdated];
    }
}

- (void)sendMessage:(NSString *)message room:(NSString *)roomName {
    [self saveMessage:message toRoom:roomName];
    // Creating Message event object
    KAAEventMessage *msg = [[KAAEventMessage alloc] initWithChatName:roomName Message:message];
    // Sending Message event to Kaa
    [self.chatEventFamily sendKAAEventMessageToAll:msg];
}

#pragma mark - Chat helper methods

- (void)saveMessage:(NSString *)msg toRoom:(NSString *)room {
    NSMutableArray *msgList = self.messages[room];
    if (msgList == nil) {
        msgList = [NSMutableArray array];
        self.messages[room] = msgList;
    }
    [msgList addObject:msg];
    [self notify:MessagesListUpdated];
}

- (NSArray *)messagesForRoom:(NSString *)roomName {
    return self.messages[roomName];
}

- (void)notify:(NSString *)name {
    [[NSNotificationCenter defaultCenter] postNotificationName:name object:nil];
}

@end
