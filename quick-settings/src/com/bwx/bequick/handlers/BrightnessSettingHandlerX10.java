package com.bwx.bequick.handlers;

import android.app.Activity;
import android.content.ContentResolver;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.bwx.bequick.fwk.RangeSetting;
import com.bwx.bequick.fwk.Setting;

public class BrightnessSettingHandlerX10 extends BrightnessSettingHandler {

	public static final String DEVICE = "SonyEricssonX10i";
	public static final int SDK_VERSION = 4;
	
	private static final int MAXIMUM = 254;
	private static final int MINIMUM = 18;
	private static final int RANGE = MAXIMUM - MINIMUM;
	
	public BrightnessSettingHandlerX10(Setting setting) {
		super(setting);
	}

	protected int getMinimum() {
		return MINIMUM;
	}
	
	protected int getMaximum() {
		return MAXIMUM;
	}
	
	protected int getRange() {
		return RANGE;
	}

	protected void setAutobrightness(Activity activity, ContentResolver resolver, boolean on) {
		super.setAutobrightness(activity, resolver, on);
		
		// set auto brightness on/off
		int value = on ? 255 : 128; // auto or middle brightness
		Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, value);

		if (!on) {
			// update slider
			RangeSetting setting = (RangeSetting) mSetting;
			setting.value = getPercentValue(value);
			setting.descr = null;
			setting.enabled = false;
			setting.checked = false; 
			setting.updateView();
		}
		
		// update current view's brightness
		LayoutParams attrs = mActivity.getWindow().getAttributes();
		attrs.screenBrightness = on ? 1f : value / (float) getMaximum();
		
		// request brightness update
		Window window = mActivity.getWindow();
		window.setAttributes(attrs);
	}
	
}