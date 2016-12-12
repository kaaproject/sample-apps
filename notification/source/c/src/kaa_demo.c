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
#include <string.h>
#include <inttypes.h>
#include <time.h>

#define PROMPT_TIMEOUT 1

static kaa_client_t *kaa_client = NULL;
static kaa_list_t *topic_list = NULL;
time_t last_prompt;

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

static uint64_t read_topic_id(void)
{
    char input[INPUT_LENGTH+1];
    int input_length = 0;
    do {
        while (input_length < INPUT_LENGTH) {
            input[input_length] = getchar();
            if (input[input_length] == '\n') {
                break;
            }
            input_length++;
        }

        input[input_length] = '\0';
    } while (strlen(input) == 0);
    if (strcmp(input, "quit") == 0) {
        kaa_client_stop(kaa_client);
        kaa_client_destroy(kaa_client);
        exit(EXIT_SUCCESS);
    }
    return (unsigned long long)atoll(input);
}

static bool find_topic_predicate(void *data, void *context)
{
    kaa_topic_t *t = (kaa_topic_t *)data;
    return t->id == *(uint64_t*)context;
}

static void subscribe_to_topic(void *data, void *context)
{
    kaa_topic_t *topic = (kaa_topic_t *)data;
    uint64_t topic_id = *(uint64_t*)context;
    if (topic_id == topic->id) {
        if (topic->subscription_type == OPTIONAL_SUBSCRIPTION) {
            kaa_error_t err = kaa_subscribe_to_topic(kaa_client_get_context(kaa_client)->notification_manager, &topic->id, false);
            if (err) {
                demo_printf("Failed to subscribe.\r\n");
            } else {
                demo_printf("Subscribed to optional topic '%llu'\r\n", topic->id);
            }
        } else {
            demo_printf("Topic %llu is MANDATORY cannot subscribe\r\n", topic->id);
        }
        kaa_error_t err = kaa_sync_topic_subscriptions(kaa_client_get_context(kaa_client)->notification_manager);
        if (err) {
            demo_printf("Failed to sync subscriptions\r\n");
        }
    }
}

static void process_user_command()
{
    if (topic_list != NULL) {
        demo_printf("Enter topic id to subscribe to:\n");
    }
    demo_printf("Enter 'quit' to exit\n");
    uint64_t topic_id = read_topic_id();

    kaa_list_for_each(kaa_list_begin(topic_list), kaa_list_back(topic_list), subscribe_to_topic, &topic_id);
    last_prompt = time(NULL);
}

static void on_notification(void *context, uint64_t *topic_id, kaa_notification_t *notification)
{
    (void)context;
    kaa_list_node_t *t = kaa_list_find_next(kaa_list_begin(topic_list), find_topic_predicate, topic_id);
    kaa_topic_t *topic = kaa_list_get_data(t);
    if (notification->alert_message) {
        kaa_string_t *message = (kaa_string_t *)notification->alert_message;
        demo_printf("Notification for topic id '%llu', name '%s' received\r\n", *topic_id, topic->name);
        demo_printf("Notification body: %s\r\n", message->data);
        demo_printf("Message alert type: %s\r\n", color_to_str(notification->alert_type));
    } else {
        demo_printf("Error: Received notification's body is null\r\n");
    }

    if (topic && topic->subscription_type == OPTIONAL_SUBSCRIPTION) {
        process_user_command();
    }
}

static void show_topics(void *data, void *context)
{
    kaa_topic_t *topic = (kaa_topic_t *)data;
    demo_printf("Topic: id '%llu', name: %s, type: ", topic->id, topic->name);
    if (topic->subscription_type == MANDATORY_SUBSCRIPTION) {
        demo_printf("MANDATORY\r\n");
    } else {
        demo_printf("OPTIONAL\r\n");
    }
}

void timeout_prompt(void *context)
{
    if (time(NULL) - last_prompt > PROMPT_TIMEOUT) {
        process_user_command();
    }
}

void on_topics_received(void *context, kaa_list_t *topics)
{
    demo_printf("Topic list was updated\r\n");
    kaa_list_for_each(kaa_list_begin(topics), kaa_list_back(topics), show_topics, NULL);
    topic_list = topics;
    process_user_command();
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
    last_prompt = time(NULL);
    error_code = kaa_client_start(kaa_client, timeout_prompt, NULL, PROMPT_TIMEOUT);
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
