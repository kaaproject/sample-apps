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

package org.kaaproject.kaa.demo.cityguide.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.kaaproject.kaa.demo.cityguide.Area;
import org.kaaproject.kaa.demo.cityguide.R;

import java.util.List;

/**
 * The implementation of the {@link ArrayAdapter} class. Used as an adapter class for the areas list view.
 * Provides list item views containing name of each area.
 */
public class AreasAdapter extends ArrayAdapter<Area> {

    private Context mContext;

    public AreasAdapter(Context context, List<Area> areas) {
        super(context, R.layout.area_list_item, areas);

        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Area area = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.area_list_item, null);

            holder = new ViewHolder();
            holder.areaName = (TextView) convertView.findViewById(R.id.areaName);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.areaName.setText(area.getName());

        return convertView;
    }

    private class ViewHolder {
        TextView areaName;
    }
}
