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

package org.kaaproject.kaa.demo.notification.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.kaaproject.kaa.demo.notification.R;

import java.io.IOException;
import java.net.URL;

public class ImageCache {
    // TODO: UIL or Picasso
    public static Bitmap loadBitmap(Context context, String imageUrl) {
        Bitmap bitmap;
        try {
            URL url = new URL(imageUrl);
            bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());

        } catch (IOException e) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_image);
            Log.e(NotificationConstants.TAG, "Unable to get image by URL because of " + e.getClass().getSimpleName() + ":" + e.getMessage());
        }
        return bitmap;
    }
}
