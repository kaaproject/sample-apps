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
#include <assert.h>
#include <kaa/kaa_error.h>
#include <kaa/kaa_context.h>
#include <kaa/platform/kaa_client.h>
#include <kaa/utilities/kaa_log.h>
#include <kaa/utilities/kaa_mem.h>
#include <kaa/gen/kaa_remote_control_ecf.h>
#include <extensions/user/kaa_user.h>
    
#define KAA_USER_ID                       "kaademo"
#define KAA_USER_ACCESS_TOKEN             "kaademo"
#define KAA_MAX_NUMBER_BYTES_COMMAND      20
#define KAA_MAX_NUMBER_ACCESS_TOKEN       40

// All commands which support.
#define KAA_COMMAND_TOGGLE "toggle"
#define KAA_COMMAND_STATUS "status"

#define demo_printf                printf
#define demo_scanf                 scanf

// Static variables.
static kaa_client_t *kaa_client = NULL;

// Static functions.
static void kaa_device_info_response(
    void *context, 
    kaa_remote_control_ecf_device_info_response_t *event, 
    kaa_endpoint_id_p source)
{
    (void)context;
    (void)source;
    uint32_t size;
    kaa_remote_control_ecf_gpio_status_t* gpio_status;
    kaa_list_node_t * node;
    
    size = kaa_list_get_size(event->gpio_status);    
    node = kaa_list_begin(event->gpio_status);

    for (uint32_t i = 0; i < size; i++) {
        gpio_status  = kaa_list_get_data(node);
        node = kaa_list_next(node);
        demo_printf("Status : %d, ID : %d\r\n", gpio_status->status, gpio_status->id);
    }
}
  
static void kaa_send_kaa_remote_control_ecf_gpio_toggle_request(void *context)
{
    (void)context;
    int id;
    int state;
    int size;
    kaa_remote_control_ecf_gpio_toggle_request_t *event;
    static const char *type = "test";
    
    size = demo_scanf("%d %d", &id, &state);

    if (size <= 0) {
        demo_printf("Failed set values\r\n");
        return;
    }

    event = kaa_remote_control_ecf_gpio_toggle_request_create();
    assert(event);
    event->gpio = kaa_remote_control_ecf_gpio_status_create();
    assert(event->gpio);
    event->gpio->id = id;
    event->gpio->status = state;
    event->gpio->type = kaa_string_copy_create(type);
    kaa_event_manager_send_kaa_remote_control_ecf_gpio_toggle_request(
        kaa_client_get_context(kaa_client)->event_manager,
        event, NULL);
    event->destroy(event);
}

static void kaa_send_kaa_remote_control_ecf_device_info_request(void *context)
{
    (void)context;
    kaa_remote_control_ecf_device_info_request_t *event = kaa_remote_control_ecf_device_info_request_create();
    assert(event);
    kaa_event_manager_send_kaa_remote_control_ecf_device_info_request(
        kaa_client_get_context(kaa_client)->event_manager,
        event, 
        NULL);
    event->destroy(event);
}

static void kaa_user_callback(void *context)
{
    char command[KAA_MAX_NUMBER_BYTES_COMMAND];
    int size;
    demo_printf(">");
    size = demo_scanf("%19s", command);
    
    if (size <= 0) {
        demo_printf("Failed set values\r\n");
        return;
    }
    // Check a command.
    if (!strncmp(command, KAA_COMMAND_TOGGLE, strlen(KAA_COMMAND_TOGGLE))) {
        kaa_send_kaa_remote_control_ecf_gpio_toggle_request(context);
    } else if (!strncmp(command, KAA_COMMAND_STATUS, strlen(KAA_COMMAND_STATUS))) {
        kaa_send_kaa_remote_control_ecf_device_info_request(context);
    } else {
        demo_printf("An incorrect command ..\r\n");
    }
}

int main(void)
{
    int size;
    char endpoint_access_token[KAA_MAX_NUMBER_ACCESS_TOKEN];
    kaa_error_t error_code = KAA_ERR_GENERIC;

    demo_printf("GPIO demo started\r\n");
    demo_printf("Set endpoint access token: ");
    size = demo_scanf("%39s", endpoint_access_token);   

    if (size <= 0) {
        demo_printf("Failed set value: %i\r\n", KAA_ERR_GENERIC);
        return error_code;
    }
	
    // Initialize Kaa client.
    error_code = kaa_client_create(&kaa_client, NULL);
    if (error_code) {
        demo_printf("Failed to create client context: %i\r\n", error_code);
        return error_code;
    }
    
    error_code = kaa_user_manager_default_attach_to_user(kaa_client_get_context(kaa_client)->user_manager, KAA_USER_ID, KAA_USER_ACCESS_TOKEN);
    if (error_code) {
        demo_printf("Failed attach to user: %i\r\n", error_code);
        return error_code;
    }

    error_code = kaa_user_manager_attach_endpoint(kaa_client_get_context(kaa_client)->user_manager, endpoint_access_token, NULL);
    if (error_code) {
        demo_printf("Failed attach to endpoint: %i\r\n", error_code);
        return error_code;
    }
    
    error_code = kaa_event_manager_set_kaa_remote_control_ecf_device_info_response_listener(
        kaa_client_get_context(kaa_client)->event_manager, 
        kaa_device_info_response, 
        NULL);
    if (error_code) {
        demo_printf("Failed attach a response listener: %i\r\n", error_code);
        return error_code;
    }

    // Start Kaa client main loop.
    error_code = kaa_client_start(kaa_client, kaa_user_callback, (void *)kaa_client, 1);
    if (error_code) {
        demo_printf("Unable to start Kaa client: %i\r\n", error_code);
        return error_code;
    }

    // Destroy Kaa client.
    kaa_client_destroy(kaa_client);
    demo_printf("GPIO demo stopped\r\n");

    return error_code;
}

