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

import static com.bwx.bequick.Constants.PREF_ADS_SHOWN;
import static com.bwx.bequick.Constants.PREF_APPEARANCE;
import static com.bwx.bequick.Constants.PREF_FLASHLIGHT;
import static com.bwx.bequick.Constants.SDK_VERSION;
import static com.bwx.bequick.Constants.DEBUG;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bwx.bequick.flashlight.LedFlashlightReceiver;
import com.bwx.bequick.flashlight.ScreenLightActivity;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingHandler;
import com.bwx.bequick.fwk.SettingsFactory;
import com.bwx.bequick.preferences.CommonPrefs;

public class MainSettingsActivity extends Activity implements OnClickListener, OnSharedPreferenceChangeListener {

	private static final String TAG = "ShowSettingsActivity";

	class CommonIntentReceiver extends BroadcastReceiver {

		// cache
		private Bitmap mBattery;
		private Paint mPaint;
		private float mX;
		private float mY;

		@Override
		public void onReceive(Context context, final Intent intent) {

			runOnUiThread(new Runnable() {
				public void run() {

					if (LedFlashlightReceiver.ACTION_FLASHLIGHT.equals(intent.getAction())) {
						updateFlashlightView();

					} else {

						// read battery status
						int rawlevel = intent.getIntExtra("level", -1);
						int scale = intent.getIntExtra("scale", -1);
						// int status = intent.getIntExtra("status", -1);
						// int health = intent.getIntExtra("health", -1);
						int level = 0;
						if (rawlevel >= 0 && scale > 0) {
							level = (rawlevel * 100) / scale;
						}

						// update battery status
						final DisplayMetrics metrics = new DisplayMetrics();
						getWindowManager().getDefaultDisplay().getMetrics(metrics);
						final float scaledDensity = metrics.scaledDensity;

						Bitmap battery = mBattery;
						Paint paint = mPaint;
						if (battery == null) {
							// initialize cache
							battery = mBattery = BitmapFactory.decodeResource(getResources(), R.drawable.ic_battery);
							paint = mPaint = new Paint();
							paint.setColor(Color.BLACK);
							paint.setFlags(Paint.ANTI_ALIAS_FLAG);
							paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

							paint.setTextSize(14 * scaledDensity);

							// calculate position
							mY = 18 * scaledDensity;
						}

						// align center
						int delta = 8;
						if (level > 9)
							delta -= 4;
						if (level == 100)
							delta -= 5;
						mX = (10 + delta) * scaledDensity;

						battery = battery.copy(Bitmap.Config.ARGB_8888, true);
						Canvas canvas = new Canvas(battery);
						// canvas.drawText(getString(R.string.battery_state_value,
						// level), mX, mY, paint);
						canvas.drawText(String.valueOf(level), mX, mY, paint);
						ImageButton view = (ImageButton) findViewById(R.id.battery);
						view.setImageBitmap(battery);
					}

				}
			});
		}
	}

	ProgressDialog mInitializingDialog;

	// common intent receiver
	private CommonIntentReceiver mCommonIntentReceiver;
	private IntentFilter mCommonIntentFilter;

	private SettingsApplication mApp;
	private TextView mCardStateView;
	private TextView mInternalStateView;
	private ImageButton mFlashlight;
	private boolean mRestartRequired; // true - if appearance changed

	private ListSettingsLayout mLayout;

	// preferences
	private int mPrefAppearance;
	private int mPrefFlashlight;

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// check eula
		SettingsApplication app = mApp = (SettingsApplication) getApplication();
		SharedPreferences prefs = app.getPreferences();

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.settings_view);

		// read configuration
		prefs.registerOnSharedPreferenceChangeListener(this);
		mPrefAppearance = Integer.parseInt(prefs.getString(PREF_APPEARANCE, "0"));
		mPrefFlashlight = Integer.parseInt(prefs.getString(PREF_FLASHLIGHT, "0"));

		// initialize views
		mCardStateView = (TextView) findViewById(R.id.card_state_value);
		mInternalStateView = (TextView) findViewById(R.id.memory_state_value);

		View battery = findViewById(R.id.battery);
		battery.setOnClickListener(this);

		mFlashlight = (ImageButton) findViewById(R.id.flashlight);
		mFlashlight.setOnClickListener(this);

		mLayout = new ListSettingsLayout(findViewById(R.id.settings_list), app);
		
		if (SDK_VERSION >= 7) { // quicker compatible
			boolean shown = prefs.getBoolean(PREF_ADS_SHOWN, false);
			if (!shown) {
				prefs.edit().putBoolean(PREF_ADS_SHOWN, true).commit();
				showDialog(0);
			}
		}
		
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		
		OnClickListener onclick = new OnClickListener() {
			public void onClick(View view) {
				dismissDialog(0);
				final int viewId = view.getId();
				if (R.id.button1 == viewId) {
					CommonPrefs.openQuickerInMarket(MainSettingsActivity.this);
				}
			}
		};
		
		Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.quicker_ads);
		dialog.findViewById(R.id.button1).setOnClickListener(onclick);
		dialog.findViewById(R.id.button2).setOnClickListener(onclick);
		
		return dialog;
	}
	
	private void updateFlashlightView() {
		ImageButton flashlight = mFlashlight;
		int pref = mPrefFlashlight;
		if (pref == 2) {
			// hidden flashlight
			flashlight.setVisibility(View.GONE);
		} else {
			// show
			flashlight.setVisibility(View.VISIBLE);
			if (pref == 1) {
				// update flashlight state image
				boolean enabled = LedFlashlightReceiver.isFlashlightEnabled(this);
				flashlight.setImageResource(enabled ? R.drawable.ic_flashlight_on : R.drawable.ic_flashlight);
			}
		}
	}

	private void createInitializeActivateHandlers() {

		Iterator<Setting> settings = mApp.getSettings().iterator();
		settings.next(); // jump one a "visible" group

		while (settings.hasNext()) {
			final Setting setting = settings.next();
			final int id = setting.id;
			SettingHandler handler = setting.getAssignedHandler();

			if (handler == null) {
				// switch visible to false
				if (id == Setting.GROUP_HIDDEN) {
					break; // stop here
				}

				// try to create a handler
				handler = SettingsFactory.createSettingHandler(setting);
			}

			if (handler != null) {

				// we have to activate visible setting
				// Log.d(TAG , "activate: "+ id);

				try {
					handler.activate(this);
				} catch (Throwable e) {
					// write log
					Log.e(TAG, "cannot activate: " + id, e);
					String settingName = getString(setting.titleId);
					Toast.makeText(this, getString(R.string.msg_cannot_init_setting, settingName), Toast.LENGTH_LONG)
							.show();

					// remove from the the central settings list
					settings.remove();
				}
			}
		}
	}

	private void updateMemoryStatus() {
		String state; 

		// internal
		state = getMemoryStatus(Environment.getDataDirectory(), R.string.txt_memory_state_value);
		if (state == null) {
			state = getString(R.string.txt_status_unknown);
		}
		mInternalStateView.setText(state);
		
		// external
		state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			state = getMemoryStatus(Environment.getExternalStorageDirectory(), R.string.txt_card_state_value);
			if (state == null) {
				state = getString(R.string.txt_no_card);
			}
		} else {
			state = getString(R.string.txt_no_card);
		}
		mCardStateView.setText(state);

	}
	
	private String getMemoryStatus(File path, int resId) {
		try {
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            long availableBlocks = stat.getAvailableBlocks();
            
            if (DEBUG) {
            	Log.d(TAG, "memory/path: " + path + " ------------");
            	Log.d(TAG, "memory/blockSize: " + blockSize);
            	Log.d(TAG, "memory/totalBlocks: " + totalBlocks);
            	Log.d(TAG, "memory/availableBlocks: " + availableBlocks);
            }

            long totalSize = totalBlocks * blockSize;
            long availableSize = availableBlocks * blockSize;
            long availablePercent = (totalSize == 0) ? -1 : availableSize * 100 / totalSize;
            
            if (Constants.DEBUG) {
            	Log.d(TAG, "memory/totalSize: " + totalSize);
            	Log.d(TAG, "memory/availableSize: " + availableSize);
            }
            
            String res = getString(resId);
            if (availablePercent > -1) {
            	res += " (" + availablePercent + "%)";
            } else {
            }
            res += " " + Formatter.formatFileSize(this, availableSize);
            return res;
            
        } catch (IllegalArgumentException e) {
            // this can occur if the SD card is removed, but we haven't received the 
            // ACTION_MEDIA_REMOVED Intent yet.
            return null;
        }		
	}
	
	protected void onResume() {
		super.onResume();

		updateMemoryStatus();

		// register common receiver
		IntentFilter filter = mCommonIntentFilter;
		if (filter == null) {
			filter = mCommonIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
			filter.addAction(LedFlashlightReceiver.ACTION_FLASHLIGHT);
			mCommonIntentReceiver = new CommonIntentReceiver();
		}
		registerReceiver(mCommonIntentReceiver, filter);

		// create/initialize/activate handlers
		createInitializeActivateHandlers();
		mLayout.updateLayout(this);
		updateFlashlightView();

		if (mRestartRequired) {
			finish();
			Intent intent = new Intent(Constants.ACTION_START_QS);
			sendBroadcast(intent);
		}
	}

	protected void onPause() {
		// Log.d(TAG, "onPause");

		// unregister battery receiver
		unregisterReceiver(mCommonIntentReceiver);

		// deactivate setting handlers
		final ArrayList<Setting> settings = mApp.getSettings();
		final int length = settings.size();
		for (int i = 1; i < length; i++) { // jump over first group setting

			final Setting setting = settings.get(i);
			final int id = setting.id;
			if (id == Setting.GROUP_HIDDEN)
				break; // hidden are already disabled

			try {
				setting.getAssignedHandler().deactivate();
			} catch (Exception e) {
				Log.w(TAG, e);
			}
			// Log.d(TAG, "deactivate: " + id);
		}

		// dismiss initialization dialog
		if (mInitializingDialog != null) {
			mInitializingDialog.dismiss();
			mInitializingDialog = null;
		}

		super.onPause();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_customize: // customization
			startActivity(new Intent(this, LayoutSettingsActivity.class));
			break;
		case R.id.menu_preferences: // preferences
			startActivity(new Intent(this, CommonPrefs.class));
			break;
		// case R.id.menu_addons: // addons
		// startActivity(new Intent(this, AddonsActivity.class));
		// break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.battery: {
			// show battery statistics
			Intent intent1 = new Intent(); intent1.setClassName("com.android.settings", "com.android.settings.fuelgauge.PowerUsageSummary");
			Intent intent2 = new Intent(); intent2.setClassName("com.android.settings", "com.android.settings.BatteryInfo");
			startActivitiesSafely(intent1, intent2);
			break;
		}

		case R.id.flashlight: {

			if (mPrefFlashlight == 0) {
				// start screen based flashlight
				startActivity(new Intent(this, ScreenLightActivity.class));
			} else {
				// toggle led-based flashlight
				boolean enabled = LedFlashlightReceiver.isFlashlightEnabled(this);
				Intent intent = new Intent(LedFlashlightReceiver.ACTION_CONTROL_FLASHLIGHT);
				intent.putExtra(LedFlashlightReceiver.EXT_ENABLED, !enabled);
				sendBroadcast(intent);
			}
			break;
		}
		}

	}

	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if (PREF_APPEARANCE.equals(key)) {
			int appearance = Integer.parseInt(prefs.getString(PREF_APPEARANCE, "0"));
			mRestartRequired = appearance != mPrefAppearance;
			mPrefAppearance = appearance;

		} else if (PREF_FLASHLIGHT.equals(key)) {
			mPrefFlashlight = Integer.parseInt(prefs.getString(PREF_FLASHLIGHT, "0"));
			updateFlashlightView();

		}
	}

	public SettingsApplication getApp() {
		return mApp;
	}

	public boolean startActivitiesSafely(Intent... intents) {
    	int size = intents.length;
    	for (int index=0; index<size; index++) {
    		try {
    			Intent intent = intents[index];
    			startActivity(intent);
    			return true;
    		} catch (Exception e) {
    			if (index + 1 == size) {
    				// this was the last intent to try
    				Log.e(TAG, "cannot launch activity", e);
    			}
    		}
    	}
    	return false;
    }
}