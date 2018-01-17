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

import static com.bwx.bequick.Constants.TAG;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

import com.bwx.bequick.Constants;
import com.bwx.bequick.MainSettingsActivity;
import com.bwx.bequick.R;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingHandler;

public class RingerSettingHandler extends SettingHandler implements OnItemClickListener {

	class RingerStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateState();
		}
	}

	// cache
	private RingerStateReceiver mReceiver;
	private IntentFilter mFilter;
	private Dialog mDialog;
	
	public RingerSettingHandler(Setting setting) {
		super(setting);
	}

	@Override
	public void activate(MainSettingsActivity activity) {
		mActivity = activity;
		IntentFilter filter = mFilter;
		if (filter == null) {
			filter = new IntentFilter();
			filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
			filter.addAction(AudioManager.VIBRATE_SETTING_CHANGED_ACTION);
			mFilter = filter;
			mReceiver = new RingerStateReceiver();
		}
		// register receiver
		activity.registerReceiver(mReceiver, mFilter);
		updateState();
	}

	@Override
	public void deactivate() {
		// unregister receiver
		mActivity.unregisterReceiver(mReceiver);
		// dismiss dialog
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
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
		
		value = new HashMap<String, Object>();
		value.put("icon", R.drawable.ic_silent);
		value.put("text", mActivity.getString(R.string.txt_ringer_silent));
		values.add(value);
		
		value = new HashMap<String, Object>();
		value.put("icon", R.drawable.ic_vibro);
		value.put("text", mActivity.getString(R.string.txt_ringer_vibro));
		values.add(value);
		
		value = new HashMap<String, Object>();
		value.put("icon", R.drawable.ic_sound);
		value.put("text", mActivity.getString(R.string.txt_ringer_sound));
		values.add(value);
		
		value = new HashMap<String, Object>();
		value.put("icon", R.drawable.ic_vibro_sound);
		value.put("text", mActivity.getString(R.string.txt_ringer_vibrosound));
		values.add(value);
		
		SimpleAdapter adapter = new SimpleAdapter(mActivity, values, 
				R.layout.row_ringer_mode, 
				new String[] {"icon", "text"}, 
				new int[] {R.id.icon, R.id.text});
		
		
		ListView view = (ListView) dialog.findViewById(R.id.list);
		view.setAdapter(adapter);
		view.setOnItemClickListener(this);
		
		return dialog;
	}
	
	
	@Override
	public void onSelected(int buttonIndex) {
		if (buttonIndex == 0) {
			try {
				Intent intent = new Intent(Settings.ACTION_SOUND_SETTINGS);
				mActivity.startActivity(intent);
				
			} catch (Exception e1) {
				Log.d(TAG, "No handler for ACTION_SOUND_SETTINGS -> showing ACTION_DISPLAY_SETTINGS instead");
				
				try {
					Intent intent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
					mActivity.startActivity(intent);
				} catch (Exception e2) {
					Log.d(TAG, "No handler for ACTION_DISPLAY_SETTINGS -> just showing dialog");
					// this is version 1.5
					mDialog = createDialog();
					mDialog.show();
				}
				
			}
		} else {
			mDialog = createDialog();
			mDialog.show();
		}
	}

	@Override
	public void onSwitched(boolean isSwitched) {
		
		AudioManager manager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
		if (manager == null) return;

		// change audio settings
		manager.setRingerMode(isSwitched ? AudioManager.RINGER_MODE_NORMAL : AudioManager.RINGER_MODE_SILENT);
		// ringer notification is sent by Android itself
		
		validateState(manager);
	}

	private void validateState(AudioManager manager) {

		int ringer = manager.getRingerMode();
		
		if (ringer != AudioManager.RINGER_MODE_SILENT 
				&& ringer != AudioManager.RINGER_MODE_VIBRATE) {
			
			// if volume is zero set it to medium
			int volume = manager.getStreamVolume(AudioManager.STREAM_RING);
			if (volume == 0) {
				manager.setStreamVolume(AudioManager.STREAM_RING, 4, 0);
				manager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 4, 0);
			}
		}
		
		// send internal volume changed notification
		mActivity.sendBroadcast(new Intent(Constants.ACTION_VOLUME_UPDATED));
	}
	
	private void updateState() {
		
		final AudioManager manager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
		if (manager == null) return;
		
		int ringer = manager.getRingerMode();
		int vibro = manager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
		
		int descr; int icon; boolean silent = false;
		if (ringer == AudioManager.RINGER_MODE_SILENT) {
			descr = R.string.txt_ringer_silent;
			icon = R.drawable.ic_silent;
			silent = true;
		} else if (ringer == AudioManager.RINGER_MODE_NORMAL) {
			if (vibro == AudioManager.VIBRATE_SETTING_ON) {
				descr = R.string.txt_ringer_vibrosound;
				icon = R.drawable.ic_vibro_sound;
			} else {
				descr = R.string.txt_ringer_sound;
				icon = R.drawable.ic_sound;
			}
		} else {
			descr = R.string.txt_ringer_vibro;
			icon = R.drawable.ic_vibro;
		}

		// update setting values
		Setting setting = mSetting;
		setting.descr = mActivity.getString(descr);
		setting.iconId = icon;
		setting.checked = !silent;
		setting.updateView();
		
	}

	@Override
	public void onValueChanged(int value) {
		// do nothing
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
		
		AudioManager manager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
		if (manager != null) {

			int ringerMode = AudioManager.RINGER_MODE_NORMAL;
			int vibroMode = AudioManager.VIBRATE_SETTING_ON;
			
			switch(index) {
				case 0: // silent
					ringerMode = AudioManager.RINGER_MODE_SILENT;
					vibroMode = AudioManager.VIBRATE_SETTING_ONLY_SILENT;
					break;
				case 1: // vibro
					ringerMode = AudioManager.RINGER_MODE_VIBRATE;
					vibroMode = AudioManager.VIBRATE_SETTING_ON;
					break;
				case 2: // sound
					ringerMode = AudioManager.RINGER_MODE_NORMAL;
					vibroMode = AudioManager.VIBRATE_SETTING_OFF;
					break;
				case 3: // sound and vibro
					ringerMode = AudioManager.RINGER_MODE_NORMAL;
					vibroMode = AudioManager.VIBRATE_SETTING_ON;
					break;
			}
			
			// update manager modes
			
			// update
			manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, vibroMode);
			manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, vibroMode);
			manager.setRingerMode(ringerMode);
			
			validateState(manager);
		}
		
		mDialog.hide();
	}

}
