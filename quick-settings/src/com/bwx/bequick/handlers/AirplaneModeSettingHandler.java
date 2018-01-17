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

package com.bwx.bequick.handlers;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.provider.Settings;

import com.bwx.bequick.Constants;
import com.bwx.bequick.MainSettingsActivity;
import com.bwx.bequick.R;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingHandler;

public class AirplaneModeSettingHandler extends SettingHandler {

	class AirplaneModeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateState();
		}
	}
	
	// cache
	private AirplaneModeReceiver mReceiver; 
	
	public AirplaneModeSettingHandler(Setting setting) {
		super(setting);
	}

	@Override
	public void activate(MainSettingsActivity activity) {
		mActivity = activity;
		if (mReceiver == null) mReceiver = new AirplaneModeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        activity.registerReceiver(mReceiver, filter);
		
		// update state
		updateState();
	}

	@Override
	public void deactivate() {
		mActivity.unregisterReceiver(mReceiver);
	}

	@Override
	public void onSelected(int buttonIndex) {
		Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
		mActivity.startActivity(intent);
	}

	@Override
	public void onSwitched(final boolean isSwitched) {
		
		if (isSwitched) {

			SharedPreferences prefs = mActivity.getApp().getPreferences();
			boolean noConfirm = prefs.getBoolean(Constants.PREF_NO_CONFIRM_AIRMODE, false);

			if (noConfirm) {
				setAirMode(true);
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
				builder.setTitle(R.string.airmode_title)
				.setMessage(R.string.msg_switch_to_air_mode)
				.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						setAirMode(isSwitched);
					}
				}).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create().show();
				
			}
			
		} else {
			setAirMode(isSwitched);
		}
		
	}

	private void setAirMode(boolean enabled) {
		// update setting
		Settings.System.putInt(mActivity.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, enabled ? 1 : 0);
		// notify change
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", enabled);
		mActivity.sendBroadcast(intent);
	}
	
	@Override
	public void onValueChanged(int value) {
		// do nothing
	}

	private void updateState() {
		int state = Settings.System.getInt(mActivity.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
		boolean isEnabled = state == 1;
		Setting setting = mSetting;
		setting.checked = isEnabled;
		setting.descr = mActivity.getString(isEnabled ? R.string.txt_status_turned_on : R.string.txt_status_turned_off);
		setting.updateView();
	}

}
