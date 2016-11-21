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
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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

    private final LayoutInflater mLayoutInflater;
    private PlayInfo mPlayInfo;

    public AlbumsAdapter(Context context, List<AlbumInfo> albums) {
        super(context, R.layout.album_list_item, albums);

        mLayoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final AlbumInfo albumInfo = getItem(position);

        final ViewHolder holder;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.album_list_item, null);

            holder = new ViewHolder();
            holder.contentView = convertView;
            holder.albumTitleView = (TextView) convertView.findViewById(R.id.album_title);
            holder.imageCountView = (TextView) convertView.findViewById(R.id.image_count);
            holder.nowPlayingView = (TextView) convertView.findViewById(R.id.now_playing);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.bind(albumInfo, mPlayInfo);

        return convertView;
    }

    public void setPlayInfo(PlayInfo playInfo) {
        mPlayInfo = playInfo;
    }

    private static class ViewHolder {
        View contentView;
        TextView albumTitleView;
        TextView imageCountView;
        TextView nowPlayingView;

        void bind(AlbumInfo albumInfo, PlayInfo playInfo) {

            final Context context = contentView.getContext();

            albumTitleView.setText(albumInfo.getTitle());

            String imageCountText = context.getString(
                    R.string.view_holder_album_image_count, albumInfo.getImageCount());
            imageCountView.setText(imageCountText);

            if (playInfo != null && playInfo.getCurrentAlbumInfo() != null &&
                    playInfo.getCurrentAlbumInfo().getBucketId().equals(albumInfo.getBucketId())) {

                nowPlayingView.setVisibility(View.VISIBLE);
                contentView.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.highlighted_text_material_light));
            } else {
                nowPlayingView.setVisibility(View.GONE);
                contentView.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }
}
