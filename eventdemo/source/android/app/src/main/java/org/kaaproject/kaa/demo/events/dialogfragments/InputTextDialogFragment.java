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

package org.kaaproject.kaa.demo.events.dialogfragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.kaaproject.kaa.demo.events.R;

/**
 * Util class to input text in dialog
 */
public final class InputTextDialogFragment extends DialogFragment {

    public static final String TAG = InputTextDialogFragment.class.getSimpleName();

    EditText mInputText;

    private static final String ARGS_HINT = "args_hint";

    private InputTextDialogListener mInputTextDialogListener;

    private final DialogInterface.OnClickListener mClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case Dialog.BUTTON_POSITIVE:
                    if (mInputTextDialogListener != null) {
                        mInputTextDialogListener
                                .onFinishInputTextDialog(mInputText.getText().toString(),
                                        mInputText.getHint().toString());
                    }
                    dismiss();
                    break;
                case Dialog.BUTTON_NEGATIVE:
                    dismiss();
                    break;
            }
        }
    };

    /**
     * Create new instance of the fragment with args
     *
     * @param hint hint has a second function,
     *             it's like a request code,
     *             to check return value from the
     *             {@link InputTextDialogListener#onFinishInputTextDialog(String, String)}
     * @return fragment
     */
    public static InputTextDialogFragment newInstance(String hint) {
        final InputTextDialogFragment inputTextDialogFragment = new InputTextDialogFragment();
        final Bundle args = new Bundle();

        args.putString(ARGS_HINT, hint);

        inputTextDialogFragment.setArguments(args);
        return inputTextDialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getContext();

        final View rootView = LayoutInflater.from(context).inflate(R.layout.dialog_input_text, null);
        mInputText = (EditText) rootView.findViewById(R.id.input_text);

        mInputText.setHint(getArguments().getString(ARGS_HINT));

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Input text");

        builder.setView(rootView);

        builder.setPositiveButton("Ok", mClickListener);

        builder.setNegativeButton("Cancel", mClickListener);

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        final Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof InputTextDialogListener) {
            mInputTextDialogListener = (InputTextDialogListener) parentFragment;
        }

        if (context instanceof InputTextDialogListener) {
            mInputTextDialogListener = (InputTextDialogListener) context;
        }

        if (mInputTextDialogListener == null) {
            throw new RuntimeException(parentFragment +
                    " or " + context + " must implement InputTextDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mInputTextDialogListener = null;
    }

    public interface InputTextDialogListener {
        void onFinishInputTextDialog(String inputText, String hintText);
    }
}
