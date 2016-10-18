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

#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <time.h>

#include <kaa_error.h>
#include <kaa_context.h>
#include <platform/kaa_client.h>
#include <utilities/kaa_log.h>
#include <utilities/kaa_mem.h>
#include <kaa_user.h>
#include <gen/kaa_chat.h>

#include <pthread.h>


#define KAA_USER_ID            "userid"
#define KAA_USER_ACCESS_TOKEN  "token"
#define KAA_STRING_MAX_LENGTH  100
#define QUIT_MESSAGE           "/quit"

#define CHAT_EVENT_FQN     "org.kaaproject.kaa.examples.event.ChatEvent"
#define CHAT_MESSAGE_FQN   "org.kaaproject.kaa.examples.event.Message"


char current_room[KAA_STRING_MAX_LENGTH];
pthread_mutex_t lock;

kaa_list_t *lst;

static kaa_client_t *kaa_client = NULL;

static bool rooms_equal(char *node_1, char *node_2)
{
    return !strncmp(node_1, node_2, strnlen(node_2, KAA_STRING_MAX_LENGTH));
}

kaa_error_t kaa_on_event_listeners(void *context, const kaa_endpoint_id listeners[], size_t listeners_count)
{
    (void)context;
    (void)listeners;
    printf("%zu event listeners received\n", listeners_count);

    return KAA_ERR_NONE;
}

kaa_error_t kaa_on_event_listeners_failed(void *context)
{
    (void)context;
    printf("Kaa Demo event listeners not found\n");
    return KAA_ERR_NONE;
}

/*
 * Callback-s which receive endpoint attach status.
 */
kaa_error_t kaa_on_attached(void *context, const char *user_external_id, const char *endpoint_access_token)
{
    (void)context;
    printf("Kaa Demo attached to user %s, access token %s\n", user_external_id, endpoint_access_token);
    return KAA_ERR_NONE;
}


kaa_error_t kaa_on_detached(void *context, const char *endpoint_access_token)
{
    (void)context;
    printf("Kaa Demo detached from user access token %s\n", endpoint_access_token);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_on_attach_success(void *context)
{
    (void)context;

    printf("Kaa Demo attach success\n");

    const char *fqns[] = { CHAT_EVENT_FQN, CHAT_MESSAGE_FQN };

    kaa_event_listeners_callback_t listeners_callback = { NULL,
        &kaa_on_event_listeners,
        &kaa_on_event_listeners_failed };

    kaa_error_t error_code = kaa_event_manager_find_event_listeners(kaa_client_get_context(kaa_client)->event_manager, fqns, 2, &listeners_callback);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client_get_context(kaa_client)->logger, error_code, "Failed to find event listeners");
    }

    return error_code;
}

kaa_error_t kaa_on_attach_failed(void *context, user_verifier_error_code_t error_code, const char *reason)
{
    (void)context;

    printf("Kaa Demo attach failed: error %d, reason '%s'\n", error_code, (reason ? reason : "null"));

    return KAA_ERR_NONE;
}

void kaa_on_change_chat(void *context, kaa_chat_chat_event_t *event, kaa_endpoint_id_p source)
{
    (void)context;
    (void)source;

    if (event->event_type == ENUM_CHAT_EVENT_TYPE_CREATE) {
        pthread_mutex_lock(&lock);
        kaa_list_push_back(lst, event->chat_name->data);
        pthread_mutex_unlock(&lock);
    } else {
        pthread_mutex_lock(&lock);
        kaa_list_remove_first(kaa_list_begin(&lst), &rooms_equal, event->chat_name->data, NULL);
        pthread_mutex_unlock(&lock);
    }
}

void kaa_on_receive_message(void *context, kaa_chat_message_t *event, kaa_endpoint_id_p source)
{
    if (!strncmp(current_room, event->chat_name->data, strnlen(current_room, KAA_STRING_MAX_LENGTH))) {
        puts(event->message->data);
    }
}

void search_room(char *room_name, size_t room_name_length)
{
    pthread_mutex_lock(&lock);
    kaa_list_node_t *room = kaa_list_find_next(kaa_list_begin(lst), &rooms_equal, room_name);
    pthread_mutex_unlock(&lock);
    if (!room) {
        printf("Can`t find room with this name\n");
        return;
    }
    printf("Room was found. Join to the room. Put /quit for leave this room.\n");
    strncpy(current_room, room_name, strnlen(room_name, KAA_STRING_MAX_LENGTH));
    char message[KAA_STRING_MAX_LENGTH];
    fflush(stdout);
    if (fgets(message, KAA_STRING_MAX_LENGTH, stdin) == NULL) {
        printf("Failed input a message\n");
    } else {
        while (strncmp(message, QUIT_MESSAGE, strnlen(QUIT_MESSAGE, KAA_STRING_MAX_LENGTH))) {
            int len = strnlen(message, KAA_STRING_MAX_LENGTH);
            message[len - 1] = '\0';
            kaa_chat_message_t *create_message = kaa_chat_message_create();
            create_message->chat_name = kaa_string_copy_create(current_room);
            create_message->message = kaa_string_copy_create(message);
            kaa_event_manager_send_kaa_chat_message(kaa_client_get_context(kaa_client)->event_manager, create_message, NULL);
            fgets(message, KAA_STRING_MAX_LENGTH, stdin);
        }
    printf("Leave from room %s\n", current_room);
    memset(current_room, 0, KAA_STRING_MAX_LENGTH);
    }
}

void command_join()
{
    printf("Enter a chat room name:\n");
    char room_name[KAA_STRING_MAX_LENGTH];
    while (getchar() != '\n') {
    }
    if (fgets(room_name, KAA_STRING_MAX_LENGTH, stdin) == NULL) {
        printf("Failed input a room name\n");
    } else {
        room_name[strnlen(room_name, KAA_STRING_MAX_LENGTH) - 1] = '\0';
        search_room(room_name, strnlen(room_name, KAA_STRING_MAX_LENGTH));
    }
    menu();
}

void command_create()
{
    printf("Enter chat room name:\n");
    char room_name[KAA_STRING_MAX_LENGTH];
    while(getchar() != '\n') {
    }
    if (fgets(room_name, KAA_STRING_MAX_LENGTH, stdin) == NULL) {
        printf("Failed input a room name\n");
    } else {
        room_name[strnlen(room_name, KAA_STRING_MAX_LENGTH) - 1] = '\0';

        kaa_chat_chat_event_t *create_room = kaa_chat_chat_event_create();
        create_room->chat_name = kaa_string_copy_create(room_name);
        create_room->event_type = ENUM_CHAT_EVENT_TYPE_CREATE;

        kaa_event_manager_send_kaa_chat_chat_event(kaa_client_get_context(kaa_client)->event_manager, create_room, NULL);

        pthread_mutex_lock(&lock);
        kaa_list_push_back(lst, room_name);
        pthread_mutex_unlock(&lock);

        create_room->destroy(create_room);

        printf("Room %s was successfully created.\n", room_name);
    }
    menu();
}

void command_delete()
{
    printf("Enter chat room name:");
    char room_name[KAA_STRING_MAX_LENGTH];
    while(getchar() != '\n') {
    }
    if (fgets(room_name, KAA_STRING_MAX_LENGTH, stdin) == NULL) {
        printf("Failed input a room name.\n");
    } else {
        room_name[strnlen(room_name, KAA_STRING_MAX_LENGTH) - 1] = '\0';

        kaa_chat_chat_event_t *delete_room = kaa_chat_chat_event_create();
        delete_room->chat_name = kaa_string_copy_create(room_name);
        delete_room->event_type = ENUM_CHAT_EVENT_TYPE_DELETE;

        kaa_event_manager_send_kaa_chat_chat_event(kaa_client_get_context(kaa_client)->event_manager, delete_room, NULL);

        pthread_mutex_lock(&lock);
        kaa_list_remove_first(kaa_list_begin(&lst), &rooms_equal, room_name, NULL);
        pthread_mutex_unlock(&lock);

        delete_room->destroy(delete_room);
        printf("Room %s was successfully deleted.\n", room_name);
    }
    menu();
}

static void print_room(char *room_name, void *context)
{
    (void)context;
    printf("%s\n", room_name);
}

void print_rooms()
{
    printf("Available rooms:\n");
    pthread_mutex_lock(&lock);
    kaa_list_for_each(kaa_list_begin(lst), kaa_list_back(lst), &print_room, NULL);
    pthread_mutex_unlock(&lock);
    menu();
}

void print_help()
{
    printf("Available commands:\n");
    printf("1. Join room\n");
    printf("2. Create room\n");
    printf("3. Delete room\n");
    printf("4. List available rooms\n");
    printf("5. Exit application\n");
}

void menu()
{
    print_help();

    int answer;
    scanf("%d", &answer);
    switch(answer) {
        case 1:
            command_join();
            break;
        case 2:
            command_create();
            break;
        case 3:
            command_delete();
            break;
        case 4:
            print_rooms();
            break;
        case 5:
            printf("Event demo stopped\n");
            kaa_list_destroy(lst, &kaa_data_destroy);
            return EXIT_SUCCESS;
        default:
            printf("Wrong command syntax\n");
            menu();
            break;
    }
}

int main(/*int argc, char *argv[]*/)
{
    printf("Event demo started\n");

    /**
     * Initialize Kaa client.
     */
    kaa_error_t error_code = kaa_client_create(&kaa_client, NULL);
    if (error_code) {
        printf("Failed create Kaa client, error code %d\n");
        return EXIT_FAILURE;
    }

    kaa_attachment_status_listeners_t listeners = { NULL,
        &kaa_on_attached,
        &kaa_on_detached,
        &kaa_on_attach_success,
        &kaa_on_attach_failed };

    error_code = kaa_user_manager_set_attachment_listeners(kaa_client_get_context(kaa_client)->user_manager, &listeners);
    if (error_code) {
        printf ("Failed set attachment listeners, error code %d\n", error_code);
        kaa_client_destroy(kaa_client);
        return EXIT_FAILURE;
    }

    error_code = kaa_user_manager_default_attach_to_user(kaa_client_get_context(kaa_client)->user_manager,
        KAA_USER_ID,
        KAA_USER_ACCESS_TOKEN);
    if (error_code) {
        printf("Failed default attach to user, error_code %d\n", error_code);
        kaa_client_destroy(kaa_client);
        return EXIT_FAILURE;
    }

    error_code = kaa_event_manager_set_kaa_chat_chat_event_listener(kaa_client_get_context(kaa_client)->event_manager
                                                                                                      , &kaa_on_change_chat
                                                                                                      , NULL);
    if (error_code) {
        printf("Failed set chat listener, error_code %d\n", error_code);
        kaa_client_destroy(kaa_client);
        return EXIT_FAILURE;
    }

    error_code = kaa_event_manager_set_kaa_chat_message_listener(kaa_client_get_context(kaa_client)->event_manager
                                                                                                        , &kaa_on_receive_message
                                                                                                        , NULL);
    if (error_code) {
        printf("Failed set message listener, error code %d\n",  error_code);
        kaa_client_destroy(kaa_client);
        return EXIT_FAILURE;
    }

    /**
     * Obtain and display Endpoint Key Hash.
     */
    const uint8_t *endpoint_key_hash = NULL;
    size_t endpoint_key_hash_length = 0;

    ext_get_sha1_base64_public(&endpoint_key_hash, &endpoint_key_hash_length);

    printf("Endpoint Key Hash: %.*s\n", (int)endpoint_key_hash_length, endpoint_key_hash);
 
    if (pthread_mutex_init(&lock, NULL) != 0) {
        printf("Mutex init failed\n");
        return EXIT_FAILURE;
    }
    lst = kaa_list_create();
    pthread_t menu_thread;
    pthread_create(&menu_thread, NULL, &menu, NULL);

    /**
     * Start Kaa client main loop.
     */
    error_code = kaa_client_start(kaa_client, NULL, NULL, 0);
    if (error_code) {
        printf ("Failed to start Kaa main loop, error code %d\n", error_code);
        kaa_client_destroy(kaa_client);
        return EXIT_FAILURE;
    }

    pthread_join(menu_thread, NULL);
    pthread_mutex_destroy(&lock);

    /**
     * Destroy Kaa client.
     */
    kaa_client_destroy(kaa_client);

    printf("Event demo stopped\n");

    return EXIT_SUCCESS;
}
