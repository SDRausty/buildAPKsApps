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

package com.bwx.bequick.handlers.autosync;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import com.bwx.bequick.MainSettingsActivity;
import com.bwx.bequick.R;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingHandler;

public class AutoSyncSettingHandler extends SettingHandler {

	private static final String TAG = "AutoSyncSettingHandler";

	public static interface SyncControl {

		public static final Uri CONTENT_URI = Uri.parse("content://sync/settings");
		public static final String KEY = "name";
		public static final String VALUE = "value";
		
		boolean isSyncActivated();
		void setSyncActivated(boolean flag);
		void activate();
		void deactivate();
	}
	
	private SyncControl mSyncControl;

	public AutoSyncSettingHandler(Setting setting) {
		super(setting);
	}

	@Override
	public void activate(MainSettingsActivity activity) throws Exception {
		mActivity = activity;
		
		SyncControl syncControl = mSyncControl;
		if (syncControl == null) {
			
			// try to create a control
			try {
				syncControl = new SyncControl20(mActivity);
			} catch (Throwable e) {
				Log.e(TAG, "it's not 2.0", e);
				ContentResolver resolver = activity.getContentResolver();
				Cursor cursor = resolver.query(SyncControl.CONTENT_URI, new String[] { SyncControl.KEY , SyncControl.VALUE }, null, null, null);
				syncControl = cursor == null ? new SyncControl16(resolver) : new SyncControl15(resolver, cursor);
			}
			
			mSyncControl = syncControl;
			Log.d(TAG, "sync control: " + syncControl.toString());
		}
		
		// update setting
		syncControl.activate();
		updateState(isBackgroundDataActivated(), syncControl.isSyncActivated());
	}

	@Override
	public void deactivate() {
		mSyncControl.deactivate();
	}

	@Override
	public void onSelected(int buttonIndex) {
		mActivity.startActivitiesSafely(new Intent(Settings.ACTION_SYNC_SETTINGS), new Intent(Settings.ACTION_SETTINGS));
	}
	
	@Override
	public void onSwitched(boolean isSwitched) {
		mSyncControl.setSyncActivated(isSwitched);
		updateState(true, isSwitched);
	}

	@Override
	public void onValueChanged(int value) {
		// do nothing
	}

	private void updateState(boolean enabled, boolean checked) {
		// update setting according to the states
		Setting setting = mSetting;
		setting.enabled = enabled;
		setting.checked = checked;
		
		int descrId = enabled 
			? (checked ? R.string.txt_status_enabled : R.string.txt_status_disabled)
			: R.string.txt_enable_background_data;
			
		setting.descr = mActivity.getString(descrId);
		setting.updateView();
	}
	
	private boolean isBackgroundDataActivated() {
		ConnectivityManager connManager = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
		return connManager == null ? true : connManager.getBackgroundDataSetting();
	}
	
}
