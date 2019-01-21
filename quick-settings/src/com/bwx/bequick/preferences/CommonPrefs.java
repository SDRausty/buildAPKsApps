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

import static com.bwx.bequick.Constants.ACTION_UPDATE_STATUSBAR_INTEGRATION;
import static com.bwx.bequick.Constants.EXTRA_INT_APPEARANCE;
import static com.bwx.bequick.Constants.EXTRA_BOOL_INVERSE_COLOR;
import static com.bwx.bequick.Constants.EXTRA_INT_STATUS;
import static com.bwx.bequick.Constants.PREF_APPEARANCE;
import static com.bwx.bequick.Constants.PREF_INVERSE_VIEW_COLOR;
import static com.bwx.bequick.Constants.PREF_FLASHLIGHT;
import static com.bwx.bequick.Constants.PREF_STATUSBAR_INTEGRATION;
import static com.bwx.bequick.Constants.PREF_ABOUT;
import static com.bwx.bequick.Constants.PREF_DOC;
import static com.bwx.bequick.Constants.PREF_ABOUT_QUICKER;
import static com.bwx.bequick.Constants.SDK_VERSION;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.bwx.bequick.R;
import com.bwx.bequick.flashlight.LedFlashlightReceiver;

public class CommonPrefs extends BasePrefs implements OnClickListener, OnPreferenceClickListener {

	// private static final String TAG = "QuickSettingsPreferences";

	public CommonPrefs() {
		super(R.layout.prefs_common);
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		findPreference(PREF_ABOUT).setOnPreferenceClickListener(this);
		findPreference(PREF_DOC).setOnPreferenceClickListener(this);
		findPreference(PREF_ABOUT_QUICKER).setOnPreferenceClickListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		
		// preview settings before applying them
		
		if (PREF_STATUSBAR_INTEGRATION.equals(key) || PREF_APPEARANCE.equals(key)
				|| PREF_INVERSE_VIEW_COLOR.equals(key)) {
			updateStatusbarShortcut(prefs);

		} else if (PREF_FLASHLIGHT.equals(key)) {
			String value = prefs.getString(PREF_FLASHLIGHT, "0");
			if ("1".equals(value)) { // this is led flashlight
				int type = LedFlashlightReceiver.detectLedFlashlightType(this);
				if (type > -1) {
					// show usage warning
					createDialog(R.string.txt_flashlight, R.string.msg_flashlight_led_usage, true).show();
				} else {
					// set property back
					activateScreenFlashlight();
					// show not supported error
					createDialog(R.string.txt_flashlight, R.string.msg_flashlight_not_supported, false).show();
				}
			} else if ("0".equals(value)) { /*screen flashlight*/
				// switch off flashlight in any case
				Intent intent = new Intent(LedFlashlightReceiver.ACTION_CONTROL_FLASHLIGHT);
				intent.putExtra(LedFlashlightReceiver.EXT_ENABLED, false);
				sendBroadcast(intent);
			}
		}
	}

	private Dialog createDialog(int titleId, int textId, boolean twoButtons) {
		Builder builder = new AlertDialog.Builder(this).setIcon(R.drawable.ic_dialog_menu_generic).setTitle(titleId)
				.setMessage(textId);
		if (twoButtons) {
			builder.setPositiveButton(R.string.btn_accept, this);
			builder.setNegativeButton(R.string.btn_calcel, this);
		} else {
			builder.setNeutralButton(R.string.btn_close, this);
		}
		return builder.create();
	}

	private void updateStatusbarShortcut(SharedPreferences sharedPreferences) {

		// get status
		int status = Integer.parseInt(sharedPreferences.getString(PREF_STATUSBAR_INTEGRATION, "0"));
		int appearance = Integer.parseInt(sharedPreferences.getString(PREF_APPEARANCE, "0"));
		boolean inverseColor = sharedPreferences.getBoolean(PREF_INVERSE_VIEW_COLOR, false);

		// create intent
		Intent intent = new Intent(ACTION_UPDATE_STATUSBAR_INTEGRATION);
		intent.putExtra(EXTRA_INT_STATUS, status);
		intent.putExtra(EXTRA_INT_APPEARANCE, appearance);
		intent.putExtra(EXTRA_BOOL_INVERSE_COLOR, inverseColor);

		// broadcast
		sendBroadcast(intent);

	}

	public void onClick(DialogInterface dialog, int which) {
		if (Dialog.BUTTON_NEGATIVE == which) { // cancel disclaimer
			activateScreenFlashlight();
		}
	}

	private void activateScreenFlashlight() {
		ListPreference pref = (ListPreference) findPreference(PREF_FLASHLIGHT);
		pref.setValue("0");
	}

	public boolean onPreferenceClick(Preference preference) {

		if (PREF_ABOUT.equals(preference.getKey())) {
			showDialog(0);
			return true;
		} else if (PREF_DOC.equals(preference.getKey())) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://code.google.com/p/quick-settings/wiki/TableOfContents"));
			startActivity(browserIntent);
		} else if (PREF_ABOUT_QUICKER.equals(preference.getKey())) {
			openQuickerInMarket(this);
			return true;
		}

		return false;
	}

	protected Dialog onCreateDialog(int id) {
		
		if (id == 0) {
			Builder builder = new AlertDialog.Builder(this).setIcon(R.drawable.ic_dialog_menu_generic).setTitle(
					R.string.pref_about);
			View view = getLayoutInflater().inflate(R.layout.about, null);
			TextView info = (TextView) view.findViewById(R.id.about_info);
			info.setText(Html.fromHtml(getString(R.string.about_info, getVersionNumber(this))));
			builder.setView(view);

			builder.setNegativeButton(R.string.btn_close, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});

			return builder.create();
		}
		
		return null;
	}
	
	public static String getVersionNumber(Context context) {
		String version;
		try {
			PackageInfo packagInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			version = packagInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) { 
			version = "?";
		}
		return version;
	}

	public static void openQuickerInMarket(Activity activity) {
		
		if (SDK_VERSION >= 7) {
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.bwx.quicker"));
				activity.startActivity(intent);
				return;
			} catch (Exception e) {
				// no market installed, open in browser
			}
		}

		// otherwise just show in browser
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://market.android.com/details?id=com.bwx.quicker"));
		activity.startActivity(browserIntent);
		
	}
	
}