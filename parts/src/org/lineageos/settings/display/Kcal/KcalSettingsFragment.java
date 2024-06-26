/*
 * Copyright (C) 2020 ArrowOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.display;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;

import org.lineageos.settings.R;
import org.lineageos.settings.utils.FileUtils;
import org.lineageos.settings.display.KcalUtils;

public class KcalSettingsFragment extends PreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "KcalSettings";

    private SharedPreferences mSharedPrefs;

    private SwitchPreferenceCompat mKcalSwitchPreference;
    private SeekBarPreference mRedColorSlider;
    private SeekBarPreference mGreenColorSlider;
    private SeekBarPreference mBlueColorSlider;
    private SeekBarPreference mSaturationSlider;
    private SeekBarPreference mContrastSlider;
    private Preference mResetButton;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.kcal_settings);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        mKcalSwitchPreference = (SwitchPreferenceCompat) findPreference("kcal_enable");
        mResetButton = (Preference) findPreference("reset_default_button");

        // Check if the node exists and enable / disable the preference depending on the case
        if (KcalUtils.isKcalSupported()) {
            configurePreferences();
            KcalUtils.writeCurrentSettings(mSharedPrefs);
            configurePreferences();
        } else {
            mKcalSwitchPreference.setEnabled(false);
            mKcalSwitchPreference.setSummary(getString(R.string.kcal_not_supported));
            mResetButton.setEnabled(false);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()){
            case "kcal_enable":
                KcalUtils.writeConfigToNode(KcalUtils.KCAL_ENABLE_NODE, 0, (Boolean) newValue ? 1 : 0);
                break;
            case "red_slider":
                KcalUtils.writeConfigToNode(KcalUtils.KCAL_RGB_NODE, 1, (Integer) newValue);
                mRedColorSlider.setSummary(String.valueOf(newValue));
                break;
            case "green_slider":
                KcalUtils.writeConfigToNode(KcalUtils.KCAL_RGB_NODE, 2, (Integer) newValue);
                mGreenColorSlider.setSummary(String.valueOf(newValue));
                break;
            case "blue_slider":
                KcalUtils.writeConfigToNode(KcalUtils.KCAL_RGB_NODE, 3, (Integer) newValue);
                mBlueColorSlider.setSummary(String.valueOf(newValue));
                break;
            case "saturation_slider":
                KcalUtils.writeConfigToNode(KcalUtils.KCAL_SATURATION_NODE, 0, (Integer) newValue);
                mSaturationSlider.setSummary(String.valueOf(newValue));
                break;
            case "contrast_slider":
                KcalUtils.writeConfigToNode(KcalUtils.KCAL_CONTRAST_NODE, 0, (Integer) newValue);
                mContrastSlider.setSummary(String.valueOf(newValue));
                break;
        }
        return true;
    }

    // Configure the switches, preferences and sliders
    private void configurePreferences() {
        mKcalSwitchPreference.setEnabled(true);
        mKcalSwitchPreference.setOnPreferenceChangeListener(this);
        mKcalSwitchPreference = (SwitchPreferenceCompat) findPreference("kcal_enable");
        mResetButton = (Preference) findPreference("reset_default_button");

        // Set the preference so it resets all the other preference's values, and applies the configuration on click
        mResetButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences.Editor editor = mSharedPrefs.edit();
                editor.clear();
                editor.commit();
                getPreferenceScreen().removeAll();
                addPreferencesFromResource(R.xml.kcal_settings);
                configurePreferences();
                KcalUtils.writeCurrentSettings(mSharedPrefs);
                configurePreferences();
                return true;
            }
        });


        mRedColorSlider = (SeekBarPreference) findPreference("red_slider");
        configureSlider(mRedColorSlider, this);

        mGreenColorSlider = (SeekBarPreference) findPreference("green_slider");
        configureSlider(mGreenColorSlider, this);

        mBlueColorSlider = (SeekBarPreference) findPreference("blue_slider");
        configureSlider(mBlueColorSlider, this);

        mSaturationSlider = (SeekBarPreference) findPreference("saturation_slider");
        configureSlider(mSaturationSlider, this);
        mSaturationSlider.setMin(224);

        mContrastSlider = (SeekBarPreference) findPreference("contrast_slider");
        configureSlider(mContrastSlider, this);
        mContrastSlider.setMin(224);
    }


    // Configure the given SeekBarPreference with the given configuration
    private void configureSlider(SeekBarPreference slider, OnPreferenceChangeListener listener) {
        slider.setOnPreferenceChangeListener(listener);
        slider.setSummary(String.valueOf(slider.getValue()));
        slider.setUpdatesContinuously(true);
        slider.setMin(1);
    }
}
