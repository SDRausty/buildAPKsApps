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

package com.bwx.bequick.fwk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;

import com.bwx.bequick.Constants;
import com.bwx.bequick.MainSettingsActivity;
import com.bwx.bequick.renderers.LinkSettingRenderer;
import com.bwx.bequick.renderers.MinMaxSliderSettingRenderer;
import com.bwx.bequick.renderers.SwitchableSliderRenderer;
import com.bwx.bequick.renderers.ToggleSettingRenderer;


/**
 * Main class representing a setting in a list
 * 
 * @author sergej@beworx.com
 */
public class Setting {

	public static final int PLACEHOLDER = -1;
	
	public static final int GROUP_VISIBLE = 100;
	public static final int GROUP_HIDDEN = 101;
	
	public static final int WIFI = 1;
	public static final int GPS = 2;
	public static final int RINGER = 3;
	public static final int BRIGHTNESS = 4;
	public static final int AIRPLANE_MODE = 5;
	public static final int MOBILE_DATA_APN = 6;
	public static final int BLUETOOTH = 7;
	public static final int SCREEN_TIMEOUT = 8;
	public static final int VOLUME = 9;
	public static final int AUTO_SYNC = 10;
	public static final int AUTO_ROTATE = 11;
	public static final int LOCK_PATTERN = 12;
	public static final int MASTER_VOLUME = 13;
	public static final int WIFI_HOTSPOT = 14;
	public static final int MOBILE_DATA = 15;
	public static final int FOUR_G = 16;

	private static long[] VIBRO_PATTERN = {0, 30};
	
	public final int id;
	public final int titleId;
	
	public int index;
	public int iconId;
	public String descr;
	public boolean enabled;
	public boolean checked;
	public boolean hasPopup;
	
	/* preferences activity name or null if none */
	public Class<?> prefs;
	
	private SettingHandler mHandler;
	private SettingRenderer mRenderer;
	
	public Setting(int id, int title) {
		this.id = id;
		this.titleId = title;
		this.enabled = true;
	}
	
	protected Setting(int id, int iconId, int title, String descr) {
		this(id, title);
		this.descr = descr;
		this.iconId = iconId;
	}
	
	public void removeRenderer() {
		mRenderer = null;
	}
	
	public SettingRenderer getAssignedRenderer() {
		SettingRenderer renderer = mRenderer;
		
		if (renderer == null) {
			// create new renderer
			switch (id) {
			
				case Setting.BRIGHTNESS:
					renderer = enabled // auto-brightness enabled
						? new SwitchableSliderRenderer()
						: new MinMaxSliderSettingRenderer();
					break;
				
				case Setting.MASTER_VOLUME:
					renderer = new MinMaxSliderSettingRenderer();
					break;
					
				case Setting.VOLUME: renderer = new LinkSettingRenderer(); break;
				
				//case Setting.TOOLBAR: renderer = new ToolbarSettingRenderer(); break;
				default: renderer = new ToggleSettingRenderer(); break;
			}
			mRenderer = renderer;
		}
		return renderer;
	}

	/*package*/ void assignHandler(SettingHandler handler) {mHandler = handler;}
	public SettingHandler getAssignedHandler() { return mHandler; }
	
	public void updateView() {
		if (mRenderer != null) mRenderer.notifySettingUpdated();
	}
	
	public void notifyButtonClicked(int buttonIndex) {
		if (mHandler != null) mHandler.onSelected(buttonIndex);
	}
	
	public void notifySwitchClicked(boolean on) {
		SettingHandler handler = mHandler;
		if (handler != null) {
			
			// vibrate 
			MainSettingsActivity activity = handler.mActivity;
			SharedPreferences config = activity.getApp().getPreferences();
			boolean vibrate = config.getBoolean(Constants.PREF_HAPTIC, false);
			if (vibrate) {
				Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
				if (vibrator != null) vibrator.vibrate(VIBRO_PATTERN, 1);
			}
			
			// notify
			handler.onSwitched(on);
		} 
	}
	
	public void notifySettingValueChanged(int value) {
		if (mHandler != null) mHandler.onValueChanged(value);
	}
	
	public void startPrefsActivity(Context context) {
		if (prefs == null) throw new IllegalStateException("prefs class is null");
		context.startActivity(new Intent(context, prefs));
	}
	
}
