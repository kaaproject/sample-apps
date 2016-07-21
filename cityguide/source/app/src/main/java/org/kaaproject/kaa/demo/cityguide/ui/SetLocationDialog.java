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

package org.kaaproject.kaa.demo.cityguide.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import org.kaaproject.kaa.demo.cityguide.AvailableArea;
import org.kaaproject.kaa.demo.cityguide.R;
import org.kaaproject.kaa.demo.cityguide.kaa.KaaManager;

import java.util.List;

/**
 * The implementation of the {@link Dialog} class. Used to display a view with the current location input.
 */
public class SetLocationDialog extends Dialog {

    private KaaManager manager;
    private SetLocationCallback callback;

    public SetLocationDialog(KaaManager manager, Context context, SetLocationCallback callback) {
        super(context);

        this.manager = manager;
        this.callback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_set_location);

        setTitle(R.string.action_set_location);

        final Spinner mSelectAreaSpinner = (Spinner) findViewById(R.id.selectAreaSpinner);
        final ArrayAdapter<String> mAreasAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item);
        mSelectAreaSpinner.setAdapter(mAreasAdapter);

        final Spinner mSelectCitySpinner = (Spinner) findViewById(R.id.selectCitySpinner);
        final ArrayAdapter<String> mCitiesAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item);
        mSelectCitySpinner.setAdapter(mCitiesAdapter);

        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                String area = (String) mSelectAreaSpinner.getSelectedItem();
                if (area != null && area.length() == 0) {
                    area = null;
                }
                String city = (String) mSelectCitySpinner.getSelectedItem();
                if (city != null && city.length() == 0) {
                    city = null;
                }
                callback.onLocationSelected(area, city);
            }
        });

        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // update areas
        updateAreasSpinner(mAreasAdapter);
        int position = 0;
        String currentArea = manager.getArea();
        if (currentArea != null) {
            position = mAreasAdapter.getPosition(currentArea);
        }
        mSelectAreaSpinner.setSelection(position);

        // update cities
        updateCitiesSpinner(mSelectAreaSpinner, mCitiesAdapter);
        position = 0;
        String currentCity = manager.getCity();
        if (currentCity != null) {
            position = mCitiesAdapter.getPosition(currentCity);
        }

        mSelectCitySpinner.setSelection(position);
        mSelectAreaSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                updateCitiesSpinner(mSelectAreaSpinner, mCitiesAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateCitiesSpinner(mSelectAreaSpinner, mCitiesAdapter);
            }
        });
    }

    private void updateAreasSpinner(ArrayAdapter<String> mAreasAdapter) {
        mAreasAdapter.clear();
        mAreasAdapter.add("");

        List<AvailableArea> availableAreas = manager.getAvailableAreas();
        for (AvailableArea area : availableAreas) {
            mAreasAdapter.add(area.getName());
        }
    }

    private void updateCitiesSpinner(Spinner mSelectAreaSpinner, ArrayAdapter<String> mCitiesAdapter) {
        mCitiesAdapter.clear();
        mCitiesAdapter.add("");

        String areaName = (String) mSelectAreaSpinner.getSelectedItem();
        if (areaName == null || areaName.length() <= 0) {
            return;
        }

        List<AvailableArea> availableAreas = manager.getAvailableAreas();
        for (AvailableArea area : availableAreas) {
            if (area.getName().equals(areaName)) {
                mCitiesAdapter.addAll(area.getAvailableCities());
                break;
            }
        }
    }

    public interface SetLocationCallback {
        void onLocationSelected(String area, String city);
    }

}
