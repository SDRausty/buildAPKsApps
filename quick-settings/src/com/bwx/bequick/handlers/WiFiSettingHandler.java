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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;

import com.bwx.bequick.MainSettingsActivity;
import com.bwx.bequick.R;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingHandler;
import com.bwx.bequick.handlers.WifiHotspotSettingHandler.WifiApManager;

public class WiFiSettingHandler extends SettingHandler {

	private static final int STATE_INPROCESS = 0;
	private static final int STATE_DONE = 1;

	/**
	 * WiFi status listener dynamically registered at start up
	 * 
	 * @author sergej@beworx.com
	 */
	class WiFiStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null)
				return;

			if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {

				int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
				updateWiFiState(wifiState);
			} else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {

				NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				DetailedState state = info.getDetailedState();

				String descr = null;
				switch (state) {
					case CONNECTING:
						descr = mActivity.getString(R.string.txt_net_status_connecting);
						break;
					case OBTAINING_IPADDR:
						descr = mActivity.getString(R.string.txt_net_status_obtainingip);
						break;
					case CONNECTED:
						// String name =
						// intent.getStringExtra(WifiManager.EXTRA_BSSID);
						String name = getWiFiManager().getConnectionInfo().getSSID();
						descr = mActivity.getString(R.string.txt_net_status_connected_to, name);
						break;
					case FAILED:
						descr = mActivity.getString(R.string.txt_net_status_failed);
						break;
					case DISCONNECTING:
						descr = mActivity.getString(R.string.txt_net_status_disconnecting);
						break;
					case DISCONNECTED:
						descr = mActivity.getString(R.string.txt_net_status_disconnected);
						break;
				}

				if (descr != null) {
					mSetting.descr = descr; // update description
					mSetting.updateView();
				}
			}
		}
	}

	private WiFiStateReceiver mWifiStateReceiver;
	private WifiManager mWiFiManager;
	private int mActionState = STATE_DONE;
	private IntentFilter mFilter;

	public WiFiSettingHandler(Setting setting) {
		super(setting);
	}

	@Override
	public void onSwitched(boolean checked) {

		// disable hotspot if it was enabled (important for android 2.2)
		if (checked && Integer.parseInt(Build.VERSION.SDK) >= 8) {
			WifiApManager wifiApManager = new WifiApManager(mActivity);
			int state = wifiApManager.getWifiApState();
			if (state == WifiApManager.WIFI_AP_STATE_ENABLED || state == WifiApManager.WIFI_AP_STATE_ENABLING) {
				wifiApManager.setWifiApEnabled(false);
			}
		}

		getWiFiManager().setWifiEnabled(checked);
		updateWiFiState(checked ? WifiManager.WIFI_STATE_ENABLING : WifiManager.WIFI_STATE_DISABLING);

	}

	private WifiManager getWiFiManager() {
		if (mWiFiManager == null)
			mWiFiManager = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
		return mWiFiManager;
	}

	@Override
	public void onSelected(int buttonIndex) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setClassName("com.android.settings", "com.android.settings.wifi.WifiSettings");
		mActivity.startActivitiesSafely(intent, new Intent(Settings.ACTION_WIRELESS_SETTINGS), new Intent(
				Settings.ACTION_WIFI_SETTINGS));
	}

	@Override
	public void activate(MainSettingsActivity activity) {
		mActivity = activity;

		// update state
		updateWiFiState(getWiFiManager().getWifiState());

		// register wifi event receiver
		IntentFilter filter = mFilter;
		WiFiStateReceiver receiver = mWifiStateReceiver;
		if (receiver == null) {
			receiver = new WiFiStateReceiver();
			mWifiStateReceiver = receiver;

			filter = new IntentFilter();
			filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
			mFilter = filter;
		}
		activity.registerReceiver(receiver, filter);

	}

	private void updateWiFiState(int wifiState) {

		int state;
		int actionState;
		switch (wifiState) {
		case WifiManager.WIFI_STATE_DISABLING:
			state = R.string.txt_status_turning_off;
			actionState = STATE_INPROCESS;
			break;

		case WifiManager.WIFI_STATE_DISABLED:
			state = R.string.txt_status_turned_off;
			actionState = STATE_DONE;
			break;

		case WifiManager.WIFI_STATE_ENABLING:
			state = R.string.txt_status_turning_on;
			actionState = STATE_INPROCESS;
			break;

		case WifiManager.WIFI_STATE_ENABLED:
			state = R.string.txt_status_turned_on;
			actionState = STATE_DONE;
			break;

		default:
			state = R.string.txt_status_unknown;
			actionState = STATE_DONE;
			break;
		}

		String stateText = mActivity.getString(state);
		setActionState(actionState);

		// update description
		Setting setting = mSetting;
		setting.descr = stateText;
		setting.checked = wifiState == WifiManager.WIFI_STATE_ENABLED || wifiState == WifiManager.WIFI_STATE_DISABLING;
		setting.updateView();
	}

	private void setActionState(int state) {
		if (mActionState != state) {
			mActionState = state;
			mSetting.enabled = state == STATE_DONE;
		}
	}

	@Override
	public void deactivate() {
		mActivity.unregisterReceiver(mWifiStateReceiver);
	}

	@Override
	public void onValueChanged(int value) {
		// do nothing
	}

}
