/*
 * Copyright (C) 2010 beworx.com
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

package com.bwx.bequick.preferences;

import static com.bwx.bequick.Constants.PREFS_COMMON;
import static com.bwx.bequick.Constants.PREF_LIGHT_SENSOR;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.Settings;
import android.widget.Toast;

import com.bwx.bequick.R;
import com.bwx.bequick.SettingsApplication;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.handlers.BrightnessSettingHandler;

public class BrightnessPrefs extends PreferenceActivity implements OnPreferenceClickListener, OnClickListener {

	//private static final String TAG = "QuickSettingsPreferences";
	
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	PreferenceManager manager = getPreferenceManager();
    	manager.setSharedPreferencesName(PREFS_COMMON); // configure preferences
    	addPreferencesFromResource(R.layout.prefs_brightness);
    	
    	CheckBoxPreference checkbox = (CheckBoxPreference) findPreference(PREF_LIGHT_SENSOR);
    	checkbox.setOnPreferenceClickListener(this);
    	
    }

	private SettingsApplication getApp() {
		return (SettingsApplication) getApplication();
	}

	public void onClick(DialogInterface dialog, int which) {
		if (which == -1) {
			
			// enable auto brightness
			CheckBoxPreference checkbox = (CheckBoxPreference) findPreference(PREF_LIGHT_SENSOR);
			checkbox.setChecked(true);
			
			enableAutobrightness();
		}
	}

	private void enableAutobrightness() {
		Setting setting = getApp().getSetting(Setting.BRIGHTNESS);
		setting.enabled = true;
		setting.removeRenderer();
	}
	
	public boolean onPreferenceClick(Preference preference) {

		CheckBoxPreference checkbox = (CheckBoxPreference) preference;
		
		if (checkbox.isChecked()) {
			
			if (hasLightSensor(this)) {
				enableAutobrightness();
			} else {
				// ask user if they really want to use it w/o a light sensor
				checkbox.setChecked(false);
				AlertDialog dialog = new AlertDialog.Builder(this)
					.setIcon(R.drawable.ic_dialog_menu_generic)
					.setTitle(R.string.txt_brightness)
					.setMessage(R.string.msg_enable_autobrightness)
					.setPositiveButton(R.string.btn_yes, this)
					.setNegativeButton(R.string.btn_no, this)
					.create();
				dialog.show();
			}
			
		} else {
			
			// was unchecked
			Setting setting = getApp().getSetting(Setting.BRIGHTNESS);
			setting.enabled = setting.checked = false;
			setting.descr = null;
			setting.removeRenderer();

			int mode = Settings.System.getInt(getContentResolver(), BrightnessSettingHandler.MODE_KEY, BrightnessSettingHandler.MODE_UNSUPPORTED);
	    	if (BrightnessSettingHandler.MODE_AUTO == mode) {
	    		Settings.System.putInt(getContentResolver(), BrightnessSettingHandler.MODE_KEY, BrightnessSettingHandler.MODE_MANUAL);
	    		Toast.makeText(this, R.string.txt_autobrightness_disabled, Toast.LENGTH_SHORT).show();
	    	}			
		}		
		return true;
	}
		
	public static boolean hasLightSensor(Context context) {
		boolean supported = false;
		SensorManager sensorService = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		if (sensorService != null) {
			List<Sensor> lightSensors = sensorService.getSensorList(Sensor.TYPE_LIGHT);
			supported = lightSensors != null && lightSensors.size() > 0;
		}
		return supported;
	}
	
}
