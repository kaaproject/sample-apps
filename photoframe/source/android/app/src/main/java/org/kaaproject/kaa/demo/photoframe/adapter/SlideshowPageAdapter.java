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

package org.kaaproject.kaa.demo.photoframe.adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import org.kaaproject.kaa.demo.photoframe.R;

import java.io.File;

/**
 * The implementation of the {@link PagerAdapter} class. Used as an adapter class for the images slideshow view.
 * Provides image views with the screen nails fetched via the cursor from {@link MediaStore}
 * for the requested album identified by bucketId.
 */
public class SlideshowPageAdapter extends PagerAdapter {

    private final Context mContext;
    private final Cursor mCursor;
    private final int mDataIndex;

    private final LayoutInflater mLayoutInflater;

    public SlideshowPageAdapter(Context context, String bucketId) {
        mContext = context;

        mCursor = mContext.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media.DATA},
                MediaStore.Images.Media.BUCKET_ID + "=? ",
                new String[]{bucketId}, null);

        if (mCursor == null) {
            throw new NullPointerException("Cursor is null");
        }
        mDataIndex = mCursor.getColumnIndex(MediaStore.MediaColumns.DATA);

        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final View itemView = mLayoutInflater.inflate(R.layout.slide_item, container, false);

        final ImageView imageView = (ImageView) itemView.findViewById(R.id.item_image_view);

        mCursor.moveToPosition(position);
        final String imagePath = mCursor.getString(mDataIndex);

        Picasso.with(mContext).load(new File(imagePath)).into(imageView);

        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
