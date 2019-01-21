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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.bwx.bequick.Constants;
import com.bwx.bequick.MainSettingsActivity;
import com.bwx.bequick.R;
import com.bwx.bequick.SettingsApplication;
import com.bwx.bequick.fwk.RangeSetting;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingHandler;

public class BrightnessSettingHandler extends SettingHandler implements OnClickListener, Runnable {

	private static final int MINIMUM_BACKLIGHT = 18;
	private static final int MAXIMUM_BACKLIGHT = 255;
	private static final int RANGE = MAXIMUM_BACKLIGHT - MINIMUM_BACKLIGHT;

    public static final String MODE_KEY = "screen_brightness_mode";
    public static final int MODE_MANUAL = 0;
    public static final int MODE_AUTO = 1;
    public static final int MODE_UNSUPPORTED = -1;
	
    private static final int UPDATE_TIMEOUT = 45; // ms
    
	private boolean mChanged;

	// cached variables
	private Handler mHandler;
	private LayoutParams mAttrs;

	protected int getMaximum() {
		return MAXIMUM_BACKLIGHT;
	}
	
	protected int getMinimum() {
		return MINIMUM_BACKLIGHT;
	}
	
	protected int getRange() {
		return RANGE;
	}
	
	public BrightnessSettingHandler(Setting setting) {
		super(setting);
	}

	@Override
	public void activate(final MainSettingsActivity activity) {
		mActivity = activity;

		SettingsApplication app = (SettingsApplication) activity.getApplication();
		SharedPreferences prefs = app.getPreferences();
		boolean useLightSensor = prefs.getBoolean(Constants.PREF_LIGHT_SENSOR, false);
		
		// get values
		ContentResolver resolver = activity.getContentResolver();
		final int mode = Settings.System.getInt(resolver, MODE_KEY, MODE_UNSUPPORTED);
		final int value = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS, 0);
		final boolean auto = mode == MODE_AUTO;
		
		// convert from 255 basis to 100 basis
		RangeSetting setting = (RangeSetting) mSetting;
		setting.value = getPercentValue(value);
		setting.descr = auto ? activity.getString(R.string.txt_autobrightness_enabled) : null;
		setting.enabled = useLightSensor;
		setting.checked = auto; 
		
		setting.updateView();
	}
	
	@Override
	public void deactivate() {
		updateSystemBrightness();
		// remove cache
		// mActivity = null;
		mHandler = null;
		mAttrs = null;
	}

	@Override
	public void onSelected(int buttonIndex) {
		
		switch (buttonIndex) {
		
			case 0: { 
				// shortcut was clicked (from LinkSettingRenderer)
				// open brightness settings
				mActivity.startActivitiesSafely(new Intent(Settings.ACTION_DISPLAY_SETTINGS));
				break;
			}
		
			case 1: { // value was updated (from MinMaxSliderRenderer)
				final ContentResolver resolver = mActivity.getContentResolver();
				final int mode = Settings.System.getInt(resolver, MODE_KEY, MODE_MANUAL);
				
				if (mode != MODE_MANUAL) {
					
					// ask user if he wants to disable auto brightness
					new AlertDialog.Builder(mActivity).setIcon(R.drawable.ic_dialog_menu_generic).setTitle(
							R.string.txt_volume).setMessage(R.string.msg_disable_autobrightness).setPositiveButton(
							R.string.btn_yes, this).setNegativeButton(R.string.btn_no, this).create().show();
	
				} else {
	
					// workaround for stop tracking
					updateSystemBrightness();
				}
				break;
			} 
		}
		
	}

	@Override
	public void onSwitched(boolean switched) {

		final Activity activity = mActivity;
		final ContentResolver resolver = activity.getContentResolver();
		
		setAutobrightness(activity, resolver, switched);
		
		// update setting and view
		RangeSetting setting = (RangeSetting) mSetting;
		setting.checked = switched;
		setting.descr = switched ? activity.getString(R.string.txt_autobrightness_enabled) : null;
		
		if (!switched) {
			// refresh value
			final int value = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS, 0);
			setting.value = getPercentValue(value);
		}
		
		setting.updateView();
	}

	protected void setAutobrightness(Activity activity, ContentResolver resolver, boolean on) {
		Settings.System.putInt(resolver, MODE_KEY, on ? MODE_AUTO : MODE_MANUAL);
	}
	
	@Override
	public void onValueChanged(int value) {

		// store value
		RangeSetting setting = (RangeSetting) mSetting;
		setting.value = value;
		updateWindowBrightness();
		mChanged = true;
	}

	private void updateWindowBrightness() {
		
		// update window brightness with a delay of 300 ms
		Handler handler = mHandler;
		if (handler == null) {
			handler = new Handler();
			mHandler = handler;
		}
		handler.removeCallbacks(this);
		handler.postAtTime(this, SystemClock.uptimeMillis() + UPDATE_TIMEOUT);
		
	}
	
	protected int getPercentValue(int value) {
		return (value - getMinimum() ) * 100 / getRange();
	}
	
	private int getPropertyValue(int value) {
		return getMinimum() + (value * getRange()) / 100;
	}

	private void updateSystemBrightness() {
		if (mChanged) {
			// update system settings
			final RangeSetting setting = (RangeSetting) mSetting;
			final ContentResolver resolver = mActivity.getContentResolver();
			final int value = getPropertyValue(setting.value);
			Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, value);
			mChanged = false;
		}
	}

	public void onClick(DialogInterface dialog, int which) {
		if (which == -1) {
			
			// disable auto-brightness
			final ContentResolver resolver = mActivity.getContentResolver();
			Settings.System.putInt(resolver, MODE_KEY, MODE_MANUAL);
			//enableLightSensor(false);
			//Log.d(TAG, "auto-brightness disabled");
			
			// update system brightness
			updateSystemBrightness();
		} else {
			
			// revert brightness back
			int value = Settings.System.getInt(mActivity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0);
			RangeSetting setting = (RangeSetting) mSetting;
			setting.value = getPercentValue(value);
			setting.updateView();
			updateWindowBrightness();
		}
		
		dialog.dismiss();
	}

	public void run() {
		
		RangeSetting setting = (RangeSetting) mSetting;
		
		// convert from 100 basis to 255 basis
		int value = getPropertyValue(setting.value);
		
		// update current view's brightness
		LayoutParams attrs = mAttrs;
		if (attrs == null) {
			attrs = mActivity.getWindow().getAttributes();
			mAttrs = attrs;
		}
		attrs.screenBrightness = value / (float)getMaximum();
		
		// request brightness update
		Window window = mActivity.getWindow();
		window.setAttributes(attrs);
	}
	
}
