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

package org.kaaproject.kaa.demo.notification;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class ImageCache {

    private final Map<String, Bitmap> imageMap;

    private static final String DEFAULT_IMAGE_KEY = "default";
    private static final String TAG = KaaNotificationApp.class.getSimpleName();
    public static final ImageCache cache = new ImageCache();

    private ImageCache() {
        imageMap = Collections.synchronizedMap(new WeakHashMap<String, Bitmap>());
        Bitmap bitmap = BitmapFactory.decodeResource(KaaNotificationApp.getContext().getResources(),
                R.drawable.default_image);
        imageMap.put(DEFAULT_IMAGE_KEY, bitmap);
    }

    public Bitmap getImage(String imageUrl) {
        Bitmap bitmap;
        if (!imageMap.containsKey(imageUrl)) {
            try {
                URL url = new URL(imageUrl);
                bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                imageMap.put(imageUrl, bitmap);
            } catch (IOException e) {
                bitmap = imageMap.get(DEFAULT_IMAGE_KEY);
                Log.e(TAG, "Unable to get image by URL because of " + e.getClass().getSimpleName() + ":" + e.getMessage());
            }
        } else {
            return imageMap.get(imageUrl);
        }
        return bitmap;
    }
}
