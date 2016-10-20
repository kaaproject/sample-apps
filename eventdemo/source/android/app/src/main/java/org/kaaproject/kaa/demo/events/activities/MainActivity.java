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

package org.kaaproject.kaa.demo.events.activities;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.kaaproject.kaa.demo.events.EventsDemoApp;
import org.kaaproject.kaa.demo.events.R;
import org.kaaproject.kaa.demo.events.dialogfragments.InputTextDialogFragment;
import org.kaaproject.kaa.demo.events.utils.KaaChatManager;
import org.kaaproject.kaa.examples.event.Chat;
import org.kaaproject.kaa.examples.event.ChatEvent;
import org.kaaproject.kaa.examples.event.Message;

public class MainActivity extends AppCompatActivity
        implements InputTextDialogFragment.InputTextDialogListener, Chat.Listener {

    Toolbar mToolbar;

    FloatingActionButton mFloatingActionButton;
    RecyclerView mRecyclerView;

    private static final String NICKNAME_HINT = "Input new nickname";
    private static final String CHAT_HINT = "Input chat name";

    private KaaChatManager mKaaChatManager;
    private ChatAdapter mChatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mKaaChatManager = EventsDemoApp.app(this).getKaaChatManager();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mToolbar.setTitle(EventsDemoApp.app(this).username());

        mToolbar.inflateMenu(R.menu.menu_main_activity);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.edit_nickname:
                        InputTextDialogFragment.newInstance(NICKNAME_HINT)
                                .show(getSupportFragmentManager(), InputTextDialogFragment.TAG);
                        break;
                    default:
                        return false;
                }

                return true;
            }
        });

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputTextDialogFragment.newInstance(CHAT_HINT)
                        .show(getSupportFragmentManager(), InputTextDialogFragment.TAG);
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.chats);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mChatAdapter = new ChatAdapter(mKaaChatManager);
        mRecyclerView.setAdapter(mChatAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mChatAdapter.notifyDataSetChanged();
        mKaaChatManager.addChatListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mKaaChatManager.removeChatListener(this);
    }

    @Override
    public void onFinishInputTextDialog(String inputText, String hintText) {
        switch (hintText) {
            case NICKNAME_HINT:
                EventsDemoApp.app(this).newUsername(inputText);
                mToolbar.setTitle(inputText);
                break;
            case CHAT_HINT:
                mKaaChatManager.createChatRoom(inputText);
                mChatAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    @Override
    public void onEvent(final ChatEvent chatEvent, String s) {
        switch (chatEvent.getEventType()) {
            case CREATE:
            case DELETE:
                mChatAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    @Override
    public void onEvent(Message message, String s) {
        // messages are ignored
    }

    /**
     * Adapter for the chat list
     */
    static final class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

        private final KaaChatManager mKaaChatManager;

        ChatAdapter(KaaChatManager kaaChatManager) {
            mKaaChatManager = kaaChatManager;
        }

        @Override
        public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ChatViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false));
        }

        @Override
        public void onBindViewHolder(ChatViewHolder holder, int position) {
            final String chatName = mKaaChatManager.getChats().get(position);
            ((TextView) holder.itemView).setText(chatName);
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mKaaChatManager.deleteChatRoom(chatName);
                    notifyDataSetChanged();
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mKaaChatManager.getChats().size();
        }

        static final class ChatViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener {

            ChatViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                ChatScreenActivity.open(v.getContext(), ((TextView) itemView).getText().toString());
            }
        }
    }
}
