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

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.kaaproject.kaa.demo.events.EventsDemoApp;
import org.kaaproject.kaa.demo.events.R;
import org.kaaproject.kaa.demo.events.utils.KaaChatManager;
import org.kaaproject.kaa.examples.event.Chat;
import org.kaaproject.kaa.examples.event.ChatEvent;
import org.kaaproject.kaa.examples.event.ChatEventType;
import org.kaaproject.kaa.examples.event.Message;

public class ChatScreenActivity extends AppCompatActivity implements Chat.Listener {

    private static final String ARGS_CHAT_NAME = "args_chat_name";

    Toolbar mToolbar;
    View mButtonSend;
    TextView mTextChatLogs;
    EditText mEditTextInputMessage;

    private static final int COLOR_OTHER = 0xff004600;
    private static final int COLOR_YOU = 0xff322a61;

    private KaaChatManager mKaaChatManager;
    private String mChatName;

    /**
     * Open chat with name
     *
     * @param ctx
     * @param chatName
     */
    public static void open(Context ctx, String chatName) {
        ctx.startActivity(new Intent(ctx, ChatScreenActivity.class)
                .putExtra(ARGS_CHAT_NAME, chatName));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        mKaaChatManager = EventsDemoApp.app(this).getKaaChatManager();

        mChatName = getIntent().getExtras().getString(ARGS_CHAT_NAME);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mChatName);
        }

        mButtonSend = findViewById(R.id.send);
        mTextChatLogs = (TextView) findViewById(R.id.simple_chat_logs);
        mEditTextInputMessage = (EditText) findViewById(R.id.input_chat_message);

        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String entry = mEditTextInputMessage.getText().toString();
                mEditTextInputMessage.setText("");

                appendChatEntry("You: " + entry, COLOR_YOU);

                // send event to the subscribers
                mKaaChatManager.sendEventToAll(
                        new Message(mChatName,
                                EventsDemoApp.app(ChatScreenActivity.this)
                                        .username() + ": " + entry));
            }
        });

        mTextChatLogs.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTextChatLogs.setIncludeFontPadding(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mKaaChatManager.addChatListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mKaaChatManager.removeChatListener(this);
    }

    @Override
    protected void onDestroy() {
        mKaaChatManager.sendEventToAll(new Message(
                mChatName, "chat info: " + EventsDemoApp.app(ChatScreenActivity.this)
                .username() + " has left the chat."));
        super.onDestroy();
    }

    @Override
    public void onEvent(ChatEvent chatEvent, String s) {
        final String chatName = chatEvent.getChatName().trim();

        if (chatEvent.getEventType() == ChatEventType.DELETE) {
            if (chatName.equals(mChatName)) {
                finish();
            }
        }
    }

    @Override
    public void onEvent(Message message, String s) {
        if (mChatName.equals(message.getChatName())) {
            appendChatEntry(message.getMessage(), COLOR_OTHER);
        }
    }

    void appendChatEntry(final String entry, int color) {

        final SpannableString coloredEntry = new SpannableString("\n" + entry);
        coloredEntry.setSpan(new ForegroundColorSpan(color),
                1, entry.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mTextChatLogs.append(coloredEntry);
    }
}
