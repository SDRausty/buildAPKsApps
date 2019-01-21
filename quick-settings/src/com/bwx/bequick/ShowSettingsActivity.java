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

package com.bwx.bequick;

import static com.bwx.bequick.Constants.PREF_APPEARANCE;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;

/**
 * This is a invisible proxy activity, which calls quick settings activity
 * either in full screen or in dialog mode.
 * 
 * @author sergej@beworx.com
 */
public class ShowSettingsActivity extends Activity {

	private static final String TAG = "ShowSettingsActivity";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SettingsApplication app = (SettingsApplication) getApplication();
		SharedPreferences prefs = app.getPreferences();
		// get application
		String appearance = prefs.getString(PREF_APPEARANCE, "0");
		String className = "0".equals(appearance) ? "com.bwx.bequick.MainSettingsActivity" : "com.bwx.bequick.DialogSettingsActivity";

		Intent intent = new Intent();
		intent.setClassName(getPackageName(), className);

		// launch real activity depending on the configuration
		try {

			// start activity
			startActivity(intent);
		} catch (ActivityNotFoundException e) {

			Log.e(TAG, "cannot dispatch launch request", e);

			// this could only happen if installation went wrong and
			// Manifest.xml was not applied
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.msg_reinstall_required).setNeutralButton(R.string.btn_close,
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							finish(); // finish this activity
						}
					}).create().show();
		}

		finish(); // finish this activity in any case
	}

}
