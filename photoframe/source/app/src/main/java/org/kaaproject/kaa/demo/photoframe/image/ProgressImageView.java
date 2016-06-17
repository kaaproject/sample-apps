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

package org.kaaproject.kaa.demo.photoframe.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.kaaproject.kaa.demo.photoframe.R;

import java.io.File;

/**
 * Custom view. Show progress bar, while image is loading
 */
public class ProgressImageView extends FrameLayout {

    private ImageView image;
    private ProgressBar progress;
    private int defaultImage = R.drawable.ic_launcher;

    public ProgressImageView(Context context) {
        this(context, null, 0);
    }

    public ProgressImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        FrameLayout frame = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.progress_image_view, this);
        image = (ImageView) frame.findViewById(R.id.progress_image_view);
        progress = (ProgressBar) frame.findViewById(R.id.progress_bar);

        if (!isInEditMode()) progress.setVisibility(GONE);
        setDefaultImage();
    }

    public void setImage(String uriStr) {
        setImage(Uri.fromFile(new File(uriStr)));
    }

    public void setImage(Uri uri) {
        if (isInEditMode()) return;
        ImageLoader.getInstance().displayImage(uri.toString(), image, UILConfiguration.getOptions(), new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                progress.setVisibility(VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                progress.setVisibility(GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                progress.setVisibility(GONE);
                image.setImageBitmap(loadedImage);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                progress.setVisibility(GONE);
            }
        });
    }

    private void setDefaultImage() {
        image.setImageResource(defaultImage);
    }

}
