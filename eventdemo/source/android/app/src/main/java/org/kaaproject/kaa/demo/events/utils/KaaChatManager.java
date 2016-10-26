/**
 * Copyright 2014-2016 CyberVision, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.demo.events.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.demo.events.R;
import org.kaaproject.kaa.examples.event.Chat;
import org.kaaproject.kaa.examples.event.ChatEvent;
import org.kaaproject.kaa.examples.event.ChatEventType;
import org.kaaproject.kaa.examples.event.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Modified code from the Java event demo
 */
public final class KaaChatManager {

    private static final String KEYS_DIR = "keys_for_java_event_demo";

    private final Context mContext;
    private final List<String> mChatList = new ArrayList<>();

    // default rooms
    {
        mChatList.add("Living room");
        mChatList.add("Guest room");
    }

    // to send events on main thread
    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());

    private KaaClient mKaaClient;
    private Chat mChatEventFamily;

    private final List<Chat.Listener> mChatListeners = new ArrayList<>();

    public KaaChatManager(Context ctx) {
        mContext = ctx;
    }

    /**
     * Startup current endpoint and init event listeners for receiving chat user messages and
     * events to manage chat list
     *
     * @throws IOException
     */
    public void start(@Nullable final Runnable successCallback) throws IOException {

        // Setup working directory for endpoint
        final KaaClientProperties endpointProperties = new KaaClientProperties();
        endpointProperties.setWorkingDirectory(KEYS_DIR);

        // Create the Kaa desktop context for the application
        final AndroidKaaPlatformContext androidKaaPlatformContext =
                new AndroidKaaPlatformContext(mContext, endpointProperties);

        // Create a Kaa client and add a listener which creates a log record
        // as soon as the Kaa client is started.
        mKaaClient = Kaa.newClient(androidKaaPlatformContext, new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {
                toast(mContext.getString(R.string.kaa_manager_client_started));

                //Obtain the event family factory.
                final EventFamilyFactory eventFamilyFactory = mKaaClient.getEventFamilyFactory();
                //Obtain the concrete event family.
                mChatEventFamily = eventFamilyFactory.getChat();

                // Add event listeners to the family factory.
                mChatEventFamily.addListener(new Chat.Listener() {

                    @Override
                    public void onEvent(final Message messageEvent, final String senderId) {

                        mMainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                for (Chat.Listener listener : mChatListeners) {
                                    listener.onEvent(messageEvent, senderId);
                                }
                            }
                        });
                    }

                    @Override
                    public void onEvent(final ChatEvent chatEvent, final String senderId) {

                        boolean fireCallback = false; // fire callback only if something happened

                        final String chatName = chatEvent.getChatName().trim();
                        if (chatEvent.getEventType() == ChatEventType.CREATE) {
                            if (createChatLocal(chatName)) {
                                toast(mContext.getString(R.string.kaa_manager_chat_created, chatName));
                                fireCallback = true;
                            }
                        }

                        if (chatEvent.getEventType() == ChatEventType.DELETE) {
                            if (deleteChatLocal(chatName)) {
                                toast(mContext.getString(R.string.kaa_manager_chat_deleted, chatName));
                                fireCallback = true;
                            }
                        }

                        if (fireCallback) {
                            mMainThreadHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    for (Chat.Listener listener : mChatListeners) {
                                        listener.onEvent(chatEvent, senderId);
                                    }
                                }
                            });
                        }
                    }
                });

                if (successCallback != null) {
                    successCallback.run();
                }
            }

            @Override
            public void onStopped() {
                toast(mContext.getString(R.string.kaa_manager_client_stopped));
            }
        }, true);

        //Start the Kaa client and connect it to the Kaa server.
        mKaaClient.start();
    }

    /**
     * Attach endpoint to specified user.
     * Only endpoints attached to the same user can do events exchange among themselves
     *
     * @param userAccessToken user access token that allows to do endpoint attach to this user
     * @param userId          user ID
     */
    public void attachToUser(String userAccessToken,
                             final String userId,
                             @Nullable final UserAttachCallback callback) {
        // Attach the endpoint to the user
        // This demo application uses a trustful verifier, therefore
        // any user credentials sent by the endpoint are accepted as valid.
        mKaaClient.attachUser(userId, userAccessToken, new UserAttachCallback() {
            @Override
            public void onAttachResult(UserAttachResponse response) {
                toast(mContext.getString(R.string.kaa_manager_attach_to_user_result, response.getResult()));
                if (response.getResult() == SyncResponseResultType.SUCCESS) {
                    toast(mContext.getString(R.string.kaa_manager_attach_to_user_success, userId));
                } else {
                    toast(mContext.getString(R.string.kaa_manager_attach_to_user_failed, userId));
                    toast(mContext.getString(R.string.kaa_manager_attach_to_user_failed_response, response));
                    toast(mContext.getString(R.string.kaa_manager_attach_to_user_note));
                }

                if (callback != null) {
                    callback.onAttachResult(response);
                }
            }
        });
    }

    /**
     * Subscribe listener to the events
     *
     * @param listener
     */
    public void addChatListener(Chat.Listener listener) {
        mChatListeners.add(listener);
    }

    /**
     * Unsubscribe listener from the events
     *
     * @param listener
     */
    public void removeChatListener(Chat.Listener listener) {
        mChatListeners.remove(listener);
    }

    /**
     * Prints list of all chats for current endpoint.
     */
    public List<String> getChats() {
        return mChatList;
    }

    /**
     * Method delegate {@link Chat#sendEventToAll(Message)}
     *
     * @param message
     * @see Chat
     */
    public void sendEventToAll(Message message) {
        mChatEventFamily.sendEventToAll(message);
    }

    /**
     * Creates new chat with name that user will specify.
     */
    public void createChatRoom(String chatName) {
        if (createChatLocal(chatName)) {
            toast(mContext.getString(R.string.kaa_manager_chat_created, chatName));
            mChatEventFamily.sendEventToAll(new ChatEvent(chatName, ChatEventType.CREATE));
        } else {
            toast(mContext.getString(R.string.kaa_manager_chat_exists, chatName));
        }
    }

    /**
     * Deletes existing chat by the chat name that user will specify.
     */
    public void deleteChatRoom(String chatName) {
        if (deleteChatLocal(chatName)) {
            toast(mContext.getString(R.string.kaa_manager_chat_deleted, chatName));
            mChatEventFamily.sendEventToAll(new ChatEvent(chatName, ChatEventType.DELETE));
        }
    }

    private boolean createChatLocal(String chatName) {
        if (!mChatList.contains(chatName)) {
            mChatList.add(chatName);
            return true;
        } else {
            return false;
        }
    }

    private boolean deleteChatLocal(String chatName) {
        return mChatList.remove(chatName);
    }

    private void toast(final String toast) {
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
