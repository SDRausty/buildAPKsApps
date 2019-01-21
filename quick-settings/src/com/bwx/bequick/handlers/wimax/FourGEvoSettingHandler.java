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

package com.bwx.bequick.handlers.wimax;

import java.lang.reflect.Method;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.util.Log;

import com.bwx.bequick.MainSettingsActivity;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingHandler;

/**
 * Evo 4G toggle
 * 
 * @author Brian Rodgers 
 */
public class FourGEvoSettingHandler extends SettingHandler {

	private static final String TAG = "FourGEvoSettingHandler";
	private static final String ACTION_WIMAX_CHANGED = "com.htc.net.wimax.WIMAX_ENABLED_CHANGED";
	
	private static class WiMaxManager {

		private static final String CONTEXT_WIMAX_SERVICE = "wimax";
		
		private static final int RAW_WIMAX_STATE_DISABLING = 0;
		private static final int RAW_WIMAX_STATE_DISABLED = 1;
		private static final int RAW_WIMAX_STATE_ENABLING = 2;
		private static final int RAW_WIMAX_STATE_ENABLED = 3;
		
		private Object mService;
		private Method mGetState;
		private Method mSetState;
		
		public WiMaxManager(Context context) throws Exception {
			mService = context.getSystemService(CONTEXT_WIMAX_SERVICE);
			mGetState = mService.getClass().getMethod("getWimaxState");
			mSetState =  mService.getClass().getMethod("setWimaxEnabled", new Class[] { Boolean.TYPE });
		}
		
		public FourGState getState() {
			int wimaxState = RAW_WIMAX_STATE_DISABLED;
			
			try {
				wimaxState = (Integer) mGetState.invoke(mService);
			} catch (Exception e) {
				Log.e(TAG, "could not get wimax state", e);
				return FourGState.UNKNOWN;
			}
			
			if (wimaxState == RAW_WIMAX_STATE_DISABLED) {
				return FourGState.OFF;
			} else if (wimaxState == RAW_WIMAX_STATE_ENABLED) {
				return FourGState.ON;
			} else if (wimaxState == RAW_WIMAX_STATE_ENABLING){
				return FourGState.TURNING_ON;
			} else if (wimaxState == RAW_WIMAX_STATE_DISABLING) {
				return FourGState.TURNING_OFF;
			} else {
				return FourGState.UNKNOWN;
			}
		}
		
		public void toggleState() {
			try {
				FourGState state = getState();
				mSetState.invoke(mService, new Object[] {state != FourGState.ON});
			} catch (Exception e) {
				Log.e(TAG, "could not toggle wimax state", e);
			}
		}
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override public void onReceive(Context context, Intent intent) {
			updateState();
		}
	};
	
	private WiMaxManager mWiMaxManager;
	
	public FourGEvoSettingHandler(Setting setting) {
		super(setting);
	}

	@Override
	public void activate(MainSettingsActivity activity) throws Exception {
		mActivity = activity;
		mWiMaxManager = new WiMaxManager(activity);
		activity.registerReceiver(mReceiver, new IntentFilter(ACTION_WIMAX_CHANGED));
		updateState();
	}

	@Override
	public void deactivate() {
		mActivity.unregisterReceiver(mReceiver);
	}

	@Override
	public void onSelected(int buttonIndex) {
		Intent intent1 = new Intent(Intent.ACTION_MAIN); intent1.setClassName("com.android.settings", "com.android.settings.wimax.WimaxSettings");
		Intent intent2 = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
		mActivity.startActivitiesSafely(intent1, intent2);
	}

	@Override
	public void onSwitched(boolean switched) {
		mWiMaxManager.toggleState();
		// no need to update state as we have an intent receiver
	}

	@Override
	public void onValueChanged(int value) {
		// do nothing
	}

	void updateState() {
		// get state
		FourGState state = mWiMaxManager.getState();
		// update view
		String stateText = mActivity.getString(state.getStatusResourceId());
		mSetting.descr = stateText;
		mSetting.checked = state == FourGState.ON;
		mSetting.enabled = state == FourGState.ON || state == FourGState.OFF;
		mSetting.updateView();
	}
}
