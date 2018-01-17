/*
 * Copyright (C) 2010 Sergej Shafarenka, beworx.com
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

package com.bwx.bequick.flashlight;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.bwx.bequick.Constants;
import com.bwx.bequick.R;
import com.bwx.bequick.SettingsApplication;
import com.bwx.bequick.flashlight.SwitchDetector.Switchable;

public class ScreenLightActivity extends Activity implements Switchable {

	private SwitchDetector mSwitchDetector;
	private PowerManager.WakeLock mLock;
	
	private TextView mMessage;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.flashlight);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		SettingsApplication app = (SettingsApplication) getApplication();
		String value = app.getPreferences().getString(Constants.PREF_FLASHLIGHT_SWITCH, "0");
		switch (Integer.parseInt(value)) {
			case 1:
				mSwitchDetector = new DelaySwitchDetector(this);
				break;
			case 2:
				mSwitchDetector = new ShakeSwitchDeterctor(this);
				break;
			default:
				mSwitchDetector = new OrientationSwitchDetector(this);
				break;
		}
		
		mMessage = (TextView) findViewById(R.id.text);
		mMessage.setText(mSwitchDetector.getTextId());

	}

    protected void onResume() {
    	super.onResume();

		// set wake lock
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		WakeLock lock = mLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "QS.Flashlight");
		if (lock != null) lock.acquire();

		mSwitchDetector.activate(this);
    }
    
    protected void onPause() {
    	
    	// remove wake lock
    	WakeLock lock = mLock;
    	if (lock != null) lock.release();
    	mLock = null;
    	
    	mSwitchDetector.inactivate(this);
    	
    	super.onPause();
    }

	public void switchLight(boolean on) {
		mMessage.setVisibility(on ? View.GONE : View.VISIBLE);

		final Window window = getWindow();
		LayoutParams attrs = window.getAttributes();
		attrs.screenBrightness = on ? 1 : 0.1f;
		window.setAttributes(attrs);
	}

}
