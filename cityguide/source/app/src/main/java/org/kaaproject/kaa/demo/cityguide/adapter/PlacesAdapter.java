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

package org.kaaproject.kaa.demo.cityguide.adapter;

import java.util.List;

import org.kaaproject.kaa.demo.cityguide.Place;
import org.kaaproject.kaa.demo.cityguide.R;
import org.kaaproject.kaa.demo.cityguide.image.ImageLoader;
import org.kaaproject.kaa.demo.cityguide.image.ImageLoader.ImageType;
import org.kaaproject.kaa.demo.cityguide.image.LoadingImageView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * The implementation of the {@link BaseAdapter} class. Used as an adapter class for the places list view.
 * Provides list item views containing a photo, name and description of each place.
 */
public class PlacesAdapter extends ArrayAdapter<Place> {

    private Context mContext;
    private ImageLoader mImageLoader;

    // TODO: move out imageLoader
    public PlacesAdapter(Context context, List<Place> places, ImageLoader imageLoader) {
        super(context, R.layout.place_list_item, places);

        mContext = context;
        mImageLoader = imageLoader;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Place place = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.place_list_item, null);

            holder = new ViewHolder();
            holder.photo = (LoadingImageView) convertView.findViewById(R.id.placePhoto);
            holder.name = (TextView) convertView.findViewById(R.id.placeName);
            holder.description = (TextView) convertView.findViewById(R.id.placeDesc);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        mImageLoader.loadImage(place.getPhotoUrl(), holder.photo, ImageType.THUMBNAIL);
        holder.name.setText(place.getTitle());
        holder.description.setText(place.getDescription());

        return convertView;
    }

    class ViewHolder {
        TextView name;
        TextView description;
        LoadingImageView photo;
    }
}
