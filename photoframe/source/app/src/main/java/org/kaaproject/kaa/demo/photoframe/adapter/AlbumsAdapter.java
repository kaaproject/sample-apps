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
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.kaaproject.kaa.demo.photoframe.AlbumInfo;
import org.kaaproject.kaa.demo.photoframe.PlayInfo;
import org.kaaproject.kaa.demo.photoframe.R;

import java.util.List;

/**
 * The implementation of the {@link ArrayAdapter} class. Used as an adapter class for the albums list view.
 * Provides list item views with the information about remote device albums.
 */
public class AlbumsAdapter extends ArrayAdapter<AlbumInfo> {

    private Context mContext;
    private PlayInfo playInfo;

    public AlbumsAdapter(Context context, PlayInfo playInfo, List<AlbumInfo> albums) {
        super(context, R.layout.album_list_item, albums);

        mContext = context;
        this.playInfo = playInfo;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AlbumInfo albumInfo = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.album_list_item, null);

            holder = new ViewHolder();
            holder.albumTitleView = (TextView) convertView.findViewById(R.id.albumTitle);
            holder.imageCountView = (TextView) convertView.findViewById(R.id.imageCount);
            holder.nowPlayingView = (TextView) convertView.findViewById(R.id.nowPlaying);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.albumTitleView.setText(albumInfo.getTitle());

        String imageCountText = mContext.getString(R.string.image_count_pattern, albumInfo.getImageCount());
        holder.imageCountView.setText(imageCountText);


        if (playInfo != null && playInfo.getCurrentAlbumInfo() != null &&
                playInfo.getCurrentAlbumInfo().getBucketId().equals(albumInfo.getBucketId())) {

            holder.nowPlayingView.setVisibility(View.VISIBLE);
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.highlighted_text_material_light));
        } else {
            holder.nowPlayingView.setVisibility(View.GONE);
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }

    private class ViewHolder {
        TextView albumTitleView;
        TextView imageCountView;
        TextView nowPlayingView;

    }
}
