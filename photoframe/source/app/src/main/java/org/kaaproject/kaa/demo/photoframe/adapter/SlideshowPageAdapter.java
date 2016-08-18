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
import android.widget.LinearLayout;

import org.kaaproject.kaa.demo.photoframe.R;
import org.kaaproject.kaa.demo.photoframe.image.ProgressImageView;

/**
 * The implementation of the {@link PagerAdapter} class. Used as an adapter class for the images slideshow view.
 * Provides image views with the screen nails fetched via the cursor from {@link MediaStore}
 * for the requested album identified by bucketId.
 */
public class SlideshowPageAdapter extends PagerAdapter {

    private Context context;
    private Cursor mCursor;
    private int mDataIndex;

    public SlideshowPageAdapter(Context context, String bucketId) {
        this.context = context;

        mCursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media.DATA},
                MediaStore.Images.Media.BUCKET_ID + "=? ",
                new String[]{bucketId}, null);

        mDataIndex = mCursor.getColumnIndex(MediaStore.MediaColumns.DATA);
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
        LayoutInflater mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = mLayoutInflater.inflate(R.layout.slide_item, container, false);

        ProgressImageView imageView = (ProgressImageView) itemView.findViewById(R.id.item_imageView);

        mCursor.moveToPosition(position);
        String imagePath = mCursor.getString(mDataIndex);

        imageView.setImage(imagePath);

        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

}
