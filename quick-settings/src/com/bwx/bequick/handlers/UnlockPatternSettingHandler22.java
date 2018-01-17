package com.bwx.bequick.handlers;

import android.content.Intent;
import android.provider.Settings;

import com.bwx.bequick.MainSettingsActivity;
import com.bwx.bequick.R;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingHandler;

/**
 * Pattern handler for android 2.2
 * 
 * @author sergej@beworx.com
 */
public class UnlockPatternSettingHandler22 extends SettingHandler {
	
	public final static String PASSWORD_TYPE_KEY = "lockscreen.password_type";
	
	private final static String LOCK_PATTERN_ENABLED = "lock_pattern_autolock"; // Settings.Secure.LOCK_PATTERN_ENABLED
	
	private static final long SYSTEM_TYPE_NUMERIC = 131072; //DevicePolicyManager.PASSWORD_QUALITY_NUMERIC;
	private static final long SYSTEM_TYPE_ALPHABETIC = 262144; //DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC;
	private static final long SYSTEM_TYPE_ALPHANUMERIC = 327680; //DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC;
	
	private static final int TYPE_NONE = 0;
	private static final int TYPE_PATTERN = 1;
	private static final int TYPE_PIN = 2;
	private static final int TYPE_PASS = 3;
	
	public UnlockPatternSettingHandler22(Setting setting) {
		super(setting);
	}

	@Override
	public void activate(MainSettingsActivity activity) throws Exception {
		mActivity = activity;
		
		int pattern = Settings.Secure.getInt(mActivity.getContentResolver(), LOCK_PATTERN_ENABLED);			
		int type;
		if (pattern == 1) {
			type = TYPE_PATTERN;
		} else {
			long passwordType = Settings.Secure.getLong(mActivity.getContentResolver(), PASSWORD_TYPE_KEY);
			if (passwordType == SYSTEM_TYPE_ALPHABETIC || passwordType == SYSTEM_TYPE_ALPHANUMERIC) {
				type = TYPE_PASS;
			} else if (passwordType == SYSTEM_TYPE_NUMERIC) {
				type = TYPE_PIN;
			} else {
				type = TYPE_NONE;
			}
		}
		
		Setting s = mSetting;
		s.checked = type != TYPE_NONE;
		
		switch (type) {
			case TYPE_PATTERN: s.descr = getString(R.string.txt_pattern); break;
			case TYPE_PIN: s.descr = getString(R.string.txt_pin); break;
			case TYPE_PASS: s.descr = getString(R.string.txt_password); break;
			default: s.descr = getString(R.string.txt_status_disabled); break;
		}
		
		s.updateView();
	}

	@Override
	public void deactivate() {
		// do nothing
	}

	@Override
	public void onSelected(int buttonIndex) {
		mActivity.startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
	}

	@Override
	public void onSwitched(boolean switched) {
		Intent intent = new Intent();
		intent.setClassName("com.android.settings", "com.android.settings.ChooseLockGeneric");
		mActivity.startActivity(intent);
	}

	@Override
	public void onValueChanged(int value) {
		// do nothing
	}

}
