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

import static com.bwx.bequick.Constants.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.bwx.bequick.MainSettingsActivity;
import com.bwx.bequick.R;
import com.bwx.bequick.fwk.Setting;

/**
 * Pattern handler for android before 2.2
 * @author sergej@beworx.com 
 */
public class UnlockPatternSettingHandler extends SystemPropertySettingHandler implements OnClickListener {

	public UnlockPatternSettingHandler(Setting setting) {
		super(setting, Settings.System.LOCK_PATTERN_ENABLED, Settings.ACTION_SECURITY_SETTINGS);
	}

	public void activate(MainSettingsActivity activity) {
		mActivity = activity;
		try {
			super.activate(activity);
		} catch (SettingNotFoundException e) {
			// this means unlock pattern was not set
			Log.d(TAG, "set unlock pattern first");
			updateSetting(false, false, R.string.txt_set_unlock_pattern);
		}
	}

	public void onSelected(int buttonIndex) {
		if (mSetting.enabled) {
			super.onSelected(buttonIndex);
		} else {
			try {
				chooseLockPattern();
			} catch (Exception e) {
				// fallback
				super.onSelected(buttonIndex);
			}
		}
	}

	private void chooseLockPattern() {
		final Intent intent = new Intent();
		intent.setClassName("com.android.settings", "com.android.settings.ChooseLockPattern");
		mActivity.startActivity(intent);
	}
	
	@Override
	public void onSwitched(boolean isSwitched) {
		if (isSwitched) {
			// ask user if he wants to set a pattern
			new AlertDialog.Builder(mActivity).setIcon(R.drawable.ic_dialog_menu_generic).setTitle(
					R.string.txt_unlock_pattern).setMessage(R.string.msg_which_unlock_pattern).setPositiveButton(
					R.string.btn_current_unlock_pattern, this).setNegativeButton(R.string.btn_new_unlock_pattern, this)
					.create().show();

		} else {
			super.onSwitched(isSwitched);
		}
	}

	public void onClick(DialogInterface dialog, int which) {
		if (which == -2) { // show pattern setup screen
			try {
				chooseLockPattern();
			} catch (Exception e) {
				super.onSelected(0); // fallback, show page for this setting 
			}
		}
		super.onSwitched(true); // enable in any case
	}
	
}
