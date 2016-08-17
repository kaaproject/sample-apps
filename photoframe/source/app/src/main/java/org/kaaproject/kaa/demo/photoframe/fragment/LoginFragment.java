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

package org.kaaproject.kaa.demo.photoframe.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import org.greenrobot.eventbus.Subscribe;
import org.kaaproject.kaa.demo.photoframe.R;
import org.kaaproject.kaa.demo.photoframe.communication.Events;

/**
 * The implementation of the {@link BaseFragment} class.
 * Used to display the login view.
 */
public class LoginFragment extends BaseFragment implements TextWatcher, OnClickListener {

    private EditText mUsernameInput;
    private EditText mPasswordInput;
    private Button mLoginButton;

    public LoginFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        setupBaseViews(rootView);

        mUsernameInput = (EditText) rootView.findViewById(R.id.usernameInput);
        mPasswordInput = (EditText) rootView.findViewById(R.id.passwordInput);
        mLoginButton = (Button) rootView.findViewById(R.id.loginButton);

        mUsernameInput.addTextChangedListener(this);
        mPasswordInput.addTextChangedListener(this);
        mLoginButton.setOnClickListener(this);

        if (!manager.isKaaStarted()) {
            showWaitView();
        } else {
            showContentView();
        }

        return rootView;
    }

    @Subscribe
    public void onEvent(Events.KaaStartedEvent kaaStarted) {
        showContentView();
    }

    @Subscribe
    public void onEvent(Events.UserAttachEvent kaaStarted) {
        new DevicesFragment().move(getActivity());
    }

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

    @Override
    public void onClick(View v) {
        showWaitView();
        manager.login(mUsernameInput.getText().toString(), "dummy");
        closeKeyboard(v);
    }

    private void closeKeyboard(View rootView) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
    }

    public String getTitle() {
        return getString(R.string.please_login);
    }

    @Override
    protected boolean displayHomeAsUp() {
        return false;
    }

    @Override
    public String getFragmentTag() {
        return LoginFragment.class.getSimpleName();
    }

}
