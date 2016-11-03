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

#include <target.h>

#include <kaa_error.h>
#include <platform/kaa_client.h>
#include <utilities/kaa_log.h>
#include <kaa_notification_manager.h>

static kaa_client_t *kaa_client = NULL;

enum color {RED, YELLOW, GREEN};

static const char *color_to_str(enum color code_color)
{
    switch(code_color) {
        case RED:
            return "CodeRed";
            break;
        case YELLOW:
            return "CodeYellow";
            break;
        case GREEN:
            return "CodeGreen";
            break;
        default:
            return "Incorrect value";
    }
}

#define INPUT_LENGTH 8

static int read_topic_id(void)
{
    char input[INPUT_LENGTH+1];
    int input_length = 0;
    while (input_length < INPUT_LENGTH) {
        input[input_length] = getchar();
        if (input[input_length] == '\n') {
            break;
        }
        input_length++;
    }

    input[input_length] = '\0';
    return atoi(input);
}

static void on_notification(void *context, uint64_t *topic_id, kaa_notification_t *notification)
{
    (void)context;
    if (notification->alert_message) {
        kaa_string_t *message = (kaa_string_t *)notification->alert_message;
        demo_printf("Notification for topic id '%lu' received\r\n", *topic_id);
        demo_printf("Notification body: %s\r\n", message->data);
        demo_printf("Message alert type: %s\r\n", color_to_str(notification->alert_type));
    } else {
        demo_printf("Error: Received notification's body is null\r\n");
    }
}

static void show_topics(kaa_list_t *topics)
{
    if (!topics || !kaa_list_get_size(topics)) {
        demo_printf("Topic list is empty");
        return;
    }

    kaa_list_node_t *it = kaa_list_begin(topics);
    while (it) {
        kaa_topic_t *topic = (kaa_topic_t *)kaa_list_get_data(it);
        demo_printf("Topic: id '%lu', name: %s, type: ", topic->id, topic->name);
        if (topic->subscription_type == MANDATORY_SUBSCRIPTION) {
            demo_printf("MANDATORY\r\n");
        } else {
            demo_printf("OPTIONAL\r\n");
        }
        it = kaa_list_next(it);
    }
}

void on_topics_received(void *context, kaa_list_t *topics)
{
    demo_printf("Topic list was updated\r\n");
    show_topics(topics);

    demo_printf("Type topic ID in order to subscribe on one:");
    size_t topic_id = read_topic_id();

    kaa_error_t err = KAA_ERR_NONE;
    kaa_client_t *client = (kaa_client_t *)context;
    kaa_list_node_t *it = kaa_list_begin(topics);
    while (it) {
        kaa_topic_t *topic = (kaa_topic_t *) kaa_list_get_data(it);
        if (topic->subscription_type == OPTIONAL_SUBSCRIPTION && topic->id == topic_id) {
            demo_printf("Subscribing to optional topic '%lu'\r\n", topic->id);
            err = kaa_subscribe_to_topic(kaa_client_get_context(client)->notification_manager, &topic->id, false);
            if (err) {
                demo_printf("Failed to subscribe.\r\n");
            }
        }
        it = kaa_list_next(it);
    }
    err = kaa_sync_topic_subscriptions(kaa_client_get_context(kaa_client)->notification_manager);
    if (err) {
        demo_printf("Failed to sync subscriptions\r\n");
    }
}

int main(void)
{
    /*
     * Initialise a board
     */
    int ret = target_initialize();
    if (ret < 0) {
        /* Note, that if console initialization failed, you will not see this message */
        demo_printf("Failed to initialise a target\r\n");
        return EXIT_FAILURE;
    }

    demo_printf("Notification demo started\r\n");

    /*
     * Initialize Kaa client.
     */
    kaa_error_t error_code = kaa_client_create(&kaa_client, NULL);
    if (error_code) {
        demo_printf("Failed create Kaa client %d\r\n", error_code);
        return EXIT_FAILURE;
    }

    kaa_topic_listener_t topic_listener = { &on_topics_received, kaa_client };
    kaa_notification_listener_t notification_listener = { &on_notification, kaa_client };

    uint32_t topic_listener_id = 0;
    uint32_t notification_listener_id = 0;

    error_code = kaa_add_topic_list_listener(kaa_client_get_context(kaa_client)->notification_manager
                                           , &topic_listener
                                           , &topic_listener_id);
    if (error_code) {
        demo_printf("Failed add topic listener %d\r\n", error_code);
        kaa_client_destroy(kaa_client);
        return EXIT_FAILURE;
    }

    error_code = kaa_add_notification_listener(kaa_client_get_context(kaa_client)->notification_manager
                                             , &notification_listener
                                             , &notification_listener_id);
    if (error_code) {
        demo_printf("Failed add notification listener %d\r\n", error_code);
        kaa_client_destroy(kaa_client);
        return EXIT_FAILURE;
    }

    /*
     * Start Kaa client main loop.
     */
    error_code = kaa_client_start(kaa_client, NULL, NULL, 0);
    if (error_code) {
        demo_printf("Failed to start Kaa main loop %d\r\n", error_code);
        kaa_client_destroy(kaa_client);
        return EXIT_FAILURE;
    }

    /*
     * Destroy Kaa client.
     */
    kaa_client_destroy(kaa_client);

    demo_printf("Notification demo stopped\r\n");
    return EXIT_SUCCESS;
}
