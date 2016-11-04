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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.kaaproject.kaa.demo.events.EventsDemoApp;
import org.kaaproject.kaa.demo.events.R;
import org.kaaproject.kaa.demo.events.utils.KaaChatManager;
import org.kaaproject.kaa.examples.event.Chat;
import org.kaaproject.kaa.examples.event.ChatEvent;
import org.kaaproject.kaa.examples.event.Message;

public class MainActivity extends AppCompatActivity implements Chat.Listener {

    Toolbar mToolbar;

    FloatingActionButton mFloatingActionButton;
    RecyclerView mRecyclerView;

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
                        showDialog(MainActivity.this, R.string.activity_main_edit_nickname_hint,
                                new OnTextInputListener() {
                                    @Override
                                    public void onTextInput(String text) {
                                        EventsDemoApp.app(MainActivity.this).newUsername(text);
                                        mToolbar.setTitle(text);
                                    }
                                });
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

                showDialog(MainActivity.this, R.string.activity_main_new_chat_hint,
                        new OnTextInputListener() {
                            @Override
                            public void onTextInput(String text) {
                                mKaaChatManager.createChatRoom(text);
                                mChatAdapter.notifyDataSetChanged();
                            }
                        });
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

    static void showDialog(Context context,
                           @StringRes int hint,
                           @NonNull final OnTextInputListener callback) {
        final View promptsView =
                LayoutInflater.from(context).inflate(R.layout.dialog_input_text, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        alertDialogBuilder.setView(promptsView);

        final TextInputLayout textInputLayout =
                (TextInputLayout) promptsView.findViewById(R.id.input_text_layout);
        textInputLayout.setHint(context.getString(hint));

        final EditText userInputEditText = (EditText) promptsView
                .findViewById(R.id.input_text);

        final DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case Dialog.BUTTON_POSITIVE:
                        callback.onTextInput(userInputEditText.getText().toString());
                        //fallthrough
                    case Dialog.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        alertDialogBuilder
                .setPositiveButton(R.string.input_text_dialog_ok, clickListener)
                .setNegativeButton(R.string.input_text_dialog_cancel, clickListener);

        final AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    interface OnTextInputListener {
        void onTextInput(String text);
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
