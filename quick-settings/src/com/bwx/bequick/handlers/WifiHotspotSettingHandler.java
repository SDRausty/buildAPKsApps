package com.bwx.bequick.handlers;

import java.lang.reflect.Method;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;

import com.bwx.bequick.Constants;
import com.bwx.bequick.MainSettingsActivity;
import com.bwx.bequick.R;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingHandler;

public class WifiHotspotSettingHandler extends SettingHandler {

	static final String TAG = "qs.wifihs";
	
	public static class WifiApManager {
		
	    public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";	
	    public static final String EXTRA_WIFI_AP_STATE = "wifi_state";
		
		public static final int WIFI_AP_STATE_DISABLING = 0;
		public static final int WIFI_AP_STATE_DISABLED = 1;
		public static final int WIFI_AP_STATE_ENABLING = 2;
		public static final int WIFI_AP_STATE_ENABLED = 3;
		public static final int WIFI_AP_STATE_FAILED = 4;
		
		private final WifiManager mWifiManager;
		
		public WifiApManager(Context context) {
			mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		}
		
		public boolean setWifiApEnabled(boolean enabled) {
			
			if (Constants.DEBUG) {
				Log.d(TAG, "setWifiApEnabled(" + enabled + ")");
			}
			
			if (enabled) { // disable WiFi in any case
				mWifiManager.setWifiEnabled(false);
			}
			
			try {
				
				// TODO comment from here
				/*
				Method getWifiApConfigurationMethod = mWifiManager.getClass().getMethod("getWifiApConfiguration");
				Object config = getWifiApConfigurationMethod.invoke(mWifiManager);
				*/
				
				// configuration = null works for many devices
				Method setWifiApEnabledMethod = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
				return (Boolean) setWifiApEnabledMethod.invoke(mWifiManager, null, enabled);
			} catch (Exception e) {
				Log.e(TAG, "", e);
				return false;
			}
		}
		
		public int getWifiApState() {
			try {
				Method method = mWifiManager.getClass().getMethod("getWifiApState");
				return (Integer) method.invoke(mWifiManager);
			} catch (Exception e) {
				Log.e(TAG, "", e);
				return WIFI_AP_STATE_FAILED;
			}
		}
	}
	
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiApManager.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
            	int state = intent.getIntExtra(WifiApManager.EXTRA_WIFI_AP_STATE, WifiApManager.WIFI_AP_STATE_FAILED);
                updateState(state);
            }
        }
    };
	
	private final IntentFilter mIntentFilter;
	private WifiApManager mWifiApManager;
	
	public WifiHotspotSettingHandler(Setting setting) {
		super(setting);
		mIntentFilter = new IntentFilter(WifiApManager.WIFI_AP_STATE_CHANGED_ACTION);
	}

	@Override
	public void activate(MainSettingsActivity activity) throws Exception {
		mActivity = activity;
		mWifiApManager = new WifiApManager(activity);
		updateState(mWifiApManager.getWifiApState());
		activity.registerReceiver(mReceiver, mIntentFilter);
	}

	/*
	private static String[] getStrings(Object obj, String methodName) throws Exception {
		Method method = obj.getClass().getMethod(methodName);
		return (String[]) method.invoke(obj);
	}
	*/
	
	@Override
	public void deactivate() {
		mActivity.unregisterReceiver(mReceiver);
	}

	@Override
	public void onSelected(int buttonIndex) {
		Intent intent = new Intent(Intent.ACTION_MAIN); intent.setClassName("com.android.settings", "com.android.settings.wifi.WifiApSettings");
		mActivity.startActivitiesSafely(intent, new Intent(Settings.ACTION_WIRELESS_SETTINGS), new Intent(Settings.ACTION_WIFI_SETTINGS));
	}

	@Override
	public void onSwitched(boolean switched) {
		if (mWifiApManager.setWifiApEnabled(switched)) {
			updateState(switched ? WifiApManager.WIFI_AP_STATE_ENABLING : WifiApManager.WIFI_AP_STATE_DISABLING);
		} else {
			Log.e(TAG, "cannot " + (switched ? "enable" : "disable") + " wifi hotspot");
		}
	}

	@Override
	public void onValueChanged(int value) {
		// do nothing
	}

	private void updateState(int wifiApState) {
		
		boolean enabled = false, checked = false;
		int stateTextId = R.string.txt_status_unknown;
		
		switch (wifiApState) {
			case WifiApManager.WIFI_AP_STATE_DISABLING: {
				enabled = false;
				checked = true;
				stateTextId = R.string.txt_status_turning_off;
				break;
			}
			case WifiApManager.WIFI_AP_STATE_DISABLED: {
				enabled = true;
				checked = false;
				stateTextId = R.string.txt_status_turned_off;
				break;
			}
			case WifiApManager.WIFI_AP_STATE_ENABLING: {
				enabled = false;
				checked = false;
				stateTextId = R.string.txt_status_turning_on;
				break;
			}
			case WifiApManager.WIFI_AP_STATE_ENABLED: {
				enabled = true;
				checked = true;
				stateTextId = R.string.txt_status_turned_on;
				break;
			}
			case WifiApManager.WIFI_AP_STATE_FAILED: {
				enabled = true;
				checked = false;
				stateTextId = R.string.txt_net_status_failed;
				break;
			}
		}
		
		Setting s = mSetting;
		s.checked = checked;
		s.enabled = enabled;
		s.descr = mActivity.getString(stateTextId);
		
		s.updateView();
	}
}
