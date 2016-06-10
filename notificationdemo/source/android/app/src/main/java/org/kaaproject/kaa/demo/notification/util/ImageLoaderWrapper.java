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

package org.kaaproject.kaa.demo.notification.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.kaaproject.kaa.demo.notification.R;

import java.io.IOException;
import java.net.URL;

/**
 * Simple class for downloading image. Use default image, if can't download from server
 * Use Universal Image Loader lib. @see <a href="https://github.com/nostra13/Android-Universal-Image-Loader">more...</a>
 */
public class ImageLoaderWrapper {

    public static void loadBitmap(final Context context, final ImageView imageView, String imageUrl) {
        ImageLoader.getInstance().displayImage(imageUrl, imageView, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                loadDefaultImage(context, imageView);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                imageView.setImageBitmap(loadedImage);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
            }
        });
    }

    private static void loadDefaultImage(Context context, ImageView imageView) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_image);
        imageView.setImageBitmap(bitmap);
    }
}
