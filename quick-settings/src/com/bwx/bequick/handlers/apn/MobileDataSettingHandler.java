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

package com.bwx.bequick.handlers.apn;

import static com.bwx.bequick.Constants.*;
import static com.bwx.bequick.handlers.apn.ApnControl.*;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.bwx.bequick.MainSettingsActivity;
import com.bwx.bequick.R;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingHandler;

/**
 * APN-based mobile data switch implementation
 * 
 * @author sergej@beworx.com
 */
public class MobileDataSettingHandler extends SettingHandler {

	private static final String TAG = "bwx.MobileData";

	private SharedPreferences mPrefs;
	
	public MobileDataSettingHandler(Setting setting) {
		super(setting);
	}

	@Override
	public void activate(MainSettingsActivity activity) throws Exception {
		mActivity = activity;
		mPrefs = activity.getApp().getPreferences();
		updateState(ApnControl.getApnState(activity, mPrefs));
	}

	@Override
	public void deactivate() {
		// do nothing
	}

	@Override
	public void onSelected(int buttonIndex) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setClassName("com.android.phone", "com.android.phone.Settings");
		try {
			mActivity.startActivity(intent);
		} catch (Exception e) {
			Log.e(TAG, "", e);
			mActivity.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS)); // fallback
		}
	}

	@Override
	public void onSwitched(boolean enabled) {

		if (!enabled && ApnControl.getApnState(mActivity, mPrefs) == STATE_MMS_ONLY) {
			
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setIcon(R.drawable.ic_dialog_menu_generic).setTitle(R.string.txt_apn_control)
				.setMessage(R.string.msg_mms_only)
				.setPositiveButton(R.string.btn_disable,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						mPrefs.edit().putBoolean(PREF_DISABLE_MMS, true).commit();
						setApnState(false);
					}
				})
				.setNeutralButton(R.string.btn_calcel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
				.create().show();
			
			return;
		}
		
		setApnState(enabled);
	}

	private void setApnState(boolean enabled) {
		ApnControl.setApnState(mActivity, mPrefs, enabled);
	
		if (enabled) {
			Toast.makeText(mActivity, R.string.msg_enabling_mobile_data, Toast.LENGTH_SHORT).show();
		}
	
		updateState(enabled ? STATE_ON : STATE_OFF);
		
		boolean wasShown = mPrefs.getBoolean(PREF_MOBILE_DISABLE_MSG_OK, false);
		if (!wasShown) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setIcon(R.drawable.ic_dialog_menu_generic).setTitle(R.string.txt_apn_control).setMessage(
					R.string.msg_disabled_warning).setNeutralButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mPrefs.edit().putBoolean(PREF_MOBILE_DISABLE_MSG_OK, true).commit();
							dialog.dismiss();
						}
					}).create().show();
		}
	}
	
	private void updateState(int state) {
		Setting setting = mSetting;
		
		boolean disableMMS = shouldDisableMms(mPrefs);
		String on = getString(R.string.txt_on);
		String off = getString(R.string.txt_off);
		
		switch (state) {
			case STATE_OFF:
				setting.checked = false; 
				setting.enabled = true;
				setting.descr = getString(R.string.txt_apn_on_off_status, off, disableMMS ? off : on);
				break;
			case STATE_ON: 
			case STATE_MMS_ONLY:
				setting.checked = true;
				setting.enabled = true;
				setting.descr = getString(R.string.txt_apn_on_off_status, on, on);
				break;
			case STATE_NO_APNS:
			default:
				setting.checked = false; // keep it enabled that users don't worry
				setting.enabled = false;
				setting.descr = getString(R.string.txt_no_apn);
				break;
		}
		
		setting.updateView();
	}

	@Override
	public void onValueChanged(int value) {
		// do nothing
	}

}
