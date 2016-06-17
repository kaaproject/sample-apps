package org.kaaproject.kaa.demo.cityguide.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.kaaproject.kaa.demo.cityguide.R;

import java.net.URI;
import java.net.URISyntaxException;

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

        FrameLayout frame = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.progress_imageview, this);
        image = (ImageView) frame.findViewById(R.id.progress_image_view);
        progress = (ProgressBar) frame.findViewById(R.id.progress_bar);

        if (!isInEditMode()) progress.setVisibility(GONE);
        setDefaultImage();
    }

    public void setImage(int res) {
        image.setImageResource(res);
    }

    public void setImage(String uriStr) {
        try {
            URI uri = new URI(uriStr);
            setImage(uri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            setDefaultImage();
        }
    }

    public void setImage(URI uri) {
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
