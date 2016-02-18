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

package org.kaaproject.kaa.examples.gpiocontol.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

import org.kaaproject.kaa.examples.gpiocontrol.R;

public class SnackbarsManager {
    public static void makeSnackBar(final Context context, String text) {

        SnackbarManager.show(
                Snackbar.with(context)
                        .text(text));
    }

    public static void makeSnackBarNoInet(final Context context) {
        SnackbarManager.show(
                Snackbar.with(context.getApplicationContext())
                        .text(context.getResources().getString(R.string.no_internet))
                        .actionLabel(context.getResources().getString(R.string.settings))
                        .actionColor(Color.WHITE)
                        .actionListener(new ActionClickListener() {
                            @Override
                            public void onActionClicked(Snackbar snackbar) {
                                context.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                            }
                        })
                        .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                , (Activity) context);
    }
}
