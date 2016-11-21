/**
 * Copyright 2014-2016 CyberVision, Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.kaaproject.kaa.demo.photoframe.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.kaaproject.kaa.demo.photoframe.R;
import org.kaaproject.kaa.demo.photoframe.communication.Events;
import org.kaaproject.kaa.demo.photoframe.kaa.KaaManager;

public class LoginActivity extends BaseActivity {

    EditText mUsernameInput;
    EditText mPasswordInput;
    Button mLoginButton;

    private static final int REQUEST_READ_EXTERNAL_STORAGE = 22;

    private final TextWatcher mLoginTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            boolean valid = mUsernameInput.getText().length() > 0;
            valid &= mPasswordInput.getText().length() > 0;
            mLoginButton.setEnabled(valid);
        }
    };

    public static void logout(Activity activity) {
        activity.startActivity(new Intent(activity, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_login);

        setTitle(R.string.fragment_login_title);
        mUsernameInput = (EditText) findViewById(R.id.username_input);
        mPasswordInput = (EditText) findViewById(R.id.password_input);
        mLoginButton = (Button) findViewById(R.id.login_button);

        mUsernameInput.addTextChangedListener(mLoginTextWatcher);
        mPasswordInput.addTextChangedListener(mLoginTextWatcher);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getKaaManager().login(mUsernameInput.getText().toString(), "dummy");
                closeKeyboard(view);
            }
        });
        mLoginButton.setEnabled(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ_EXTERNAL_STORAGE);

        } else {
            startKaa();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startKaa();
                } else {
                    Toast.makeText(this,
                            R.string.activity_main_give_permission,
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void startKaa() {

        final KaaManager kaaManager = getKaaManager();

        if (!kaaManager.isInited()) {
            kaaManager.init(this);

            if (kaaManager.isUserAttached()) {
                DevicesActivity.start(this);
            }

            kaaManager.start();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onKaaStarted(Events.KaaStartedEvent event) {
        Toast.makeText(this, R.string.activity_main_kaa_started, Toast.LENGTH_SHORT).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Events.UserAttachEvent attachEvent) {

        final String errorMessage = attachEvent.getErrorMessage();
        if (errorMessage != null) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            return;
        }

        DevicesActivity.start(this);
    }

    private void closeKeyboard(View rootView) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
    }

}
