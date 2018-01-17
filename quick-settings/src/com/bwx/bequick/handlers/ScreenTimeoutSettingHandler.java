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

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Dialog;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

import com.bwx.bequick.MainSettingsActivity;
import com.bwx.bequick.R;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingHandler;

public class ScreenTimeoutSettingHandler extends SettingHandler implements OnItemClickListener {

	//private static final String TAG = "ScreenTimeoutSettingHandler";
	private int mTimeout;
	
	// cache
	private ScreenTimeoutValues mValues;
	private Dialog mDialog;
	
	public ScreenTimeoutSettingHandler(Setting setting) {
		super(setting);
	}

	@Override
	public void activate(MainSettingsActivity activity) {
		mActivity = activity;
		int timeout = mTimeout = getCurrentTimeout();
		updateUIByValue(timeout);
		
		// we should not remember 0 if timeout is off
		// otherwise user will be switching between off and off
		if (timeout < 0) mTimeout = 60000; // default to 1 minute
	}

	@Override
	public void deactivate() {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
	}
	
	private void updateUIByValue(int value) {
		String descr;
		boolean checked = true;
		if (value <= 0) {
			// never
			descr = mActivity.getString(R.string.txt_screen_timeout_value_never);
			checked = false;
		} else if (value < 60000) {
			// seconds
			descr = mActivity.getString(R.string.txt_screen_timeout_value_seconds, value / 1000);
		} else {
			int minutes = value / 60000;
			descr = minutes == 1 
				? mActivity.getString(R.string.txt_screen_timeout_value_minute, minutes)
				: mActivity.getString(R.string.txt_screen_timeout_value_minutes, minutes);
		}
		setDescription(descr, checked);
		//Log.d(TAG, "value: " + value + ", descr: " + descr);
	}
	
	private void setDescription(String descr, boolean checked) {
		Setting setting = mSetting;
		setting.descr = descr;
		setting.checked = checked;
		setting.updateView();
	}
	
	@Override
	public void onSelected(int buttonIndex) {
		if (mValues == null) mValues = new ScreenTimeoutValues(mActivity);
		// show dialog
		mDialog = createDialog();
		mDialog.show();
	}

	private Dialog createDialog() {

		if (mDialog != null) return mDialog;
		
		// create dialog 
		Dialog dialog = new Dialog(mActivity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCanceledOnTouchOutside(true);
		dialog.setContentView(R.layout.simple_list_view);
		
		// prepare values
		ArrayList<HashMap<String, Object>> values = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> value;
		
		ScreenTimeoutValues vls = mValues;
		int numberOfValues = vls.getNumberOfValues();
		for (int i=0; i<numberOfValues; i++) {
			value = new HashMap<String, Object>();
			value.put("text", vls.getDescriptionByIndex(i));
			values.add(value);
		}

		// add never timeout
		value = new HashMap<String, Object>();
		value.put("text", vls.getDescriptionByIndex(ScreenTimeoutValues.INDEX_NEVER));
		values.add(value);
		
		SimpleAdapter adapter = new SimpleAdapter(mActivity, values, 
				R.layout.row_timeout, 
				new String[] {"text"},
				new int[] {R.id.text});
		
		ListView view = (ListView) dialog.findViewById(R.id.list);
		view.setAdapter(adapter);
		view.setOnItemClickListener(this);
		
		return dialog;
	}
	
	void onTimeoutSelected(int index) {
		// update UI
		setDescription(mValues.getDescriptionByIndex(index), index > -1);
		
		// set timeout
		int timeout = mValues.getTimeoutByIndex(index);
		// TODO REMOVE!!! workaround for timeout
		/*
		if (timeout == ScreenTimeoutValues.INDEX_NEVER) {
			timeout = Integer.MAX_VALUE;
		}
		*/
		setTimeout(timeout);
		//Log.d(TAG, "onTimeoutSelected");
	}
	
	@Override
	public void onSwitched(boolean isSwitched) {
		// remember current timeout
		if (!isSwitched) mTimeout = getCurrentTimeout();
		int timeout = isSwitched ? mTimeout : -1;
		setTimeout(timeout);
		updateUIByValue(timeout);
		
		//Log.d(TAG, "onSwitched");
	}

	@Override
	public void onValueChanged(int value) {
		// do nothing
	}

	private int getCurrentTimeout() {
		return Settings.System.getInt(mActivity.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0);
	}
	
	private void setTimeout(int timeout) {
		Settings.System.putInt(mActivity.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, timeout);
		//Log.d(TAG, "timeout updated: " + timeout + " ms");
	}

	public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
		if (index >= mValues.getNumberOfValues()) index = ScreenTimeoutValues.INDEX_NEVER;
		onTimeoutSelected(index);
		mDialog.hide();
	}
	
}
