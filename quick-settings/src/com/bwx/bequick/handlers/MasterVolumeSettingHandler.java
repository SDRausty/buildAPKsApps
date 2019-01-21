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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import com.bwx.bequick.Constants;
import com.bwx.bequick.MainSettingsActivity;
import com.bwx.bequick.R;
import com.bwx.bequick.fwk.RangeSetting;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingHandler;

public class MasterVolumeSettingHandler extends SettingHandler {

	//private static final String TAG = "MasterVolumeSettingHandler";
	private AudioManager mManager;
	
	// cache
	private BroadcastReceiver mVolumeReceiver;
	private IntentFilter mFilter;
	
	class VolumeChangedReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			updateSettingState();
		}
	}
	
	public MasterVolumeSettingHandler(Setting setting) {
		super(setting);
	}

	@Override
	public void activate(MainSettingsActivity activity) throws Exception {
		mActivity = activity;
		AudioManager manager = mManager;
		if (manager == null) {
			manager = (AudioManager) mActivity.getSystemService(Activity.AUDIO_SERVICE);
			mManager = manager;
		}
		updateSettingState();
		
		// register volume receiver
		BroadcastReceiver receiver = mVolumeReceiver;
		IntentFilter filter = mFilter;
		if (receiver == null) {
			mVolumeReceiver = receiver = new VolumeChangedReceiver();
			mFilter = filter = new IntentFilter(Constants.ACTION_VOLUME_UPDATED);
		}
		activity.registerReceiver(receiver, filter);
	}

	private void updateSettingState() {
		
		AudioManager manager = mManager;

		// get current volumes
		int v1 = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
		int v2 = manager.getStreamVolume(AudioManager.STREAM_RING);
		int v3 = manager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
		int v4 = manager.getStreamVolume(AudioManager.STREAM_ALARM);
		
		// get max value for state
		int maxv1 = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int maxv2 = manager.getStreamMaxVolume(AudioManager.STREAM_RING);

		// get relative values in %
		int relv2 = Math.round(100f / maxv2 * v2);
		int relv1 = Math.round(100f / maxv1 * v1);
		
		int relv = Math.max(relv2 , relv1); // max between ring & music in %
		System.out.println("Music: " + relv1 + ", ringer: " + relv2 + ", max: " + relv);
		
		RangeSetting setting = (RangeSetting) mSetting;
		setting.value = round(15f / 100 * relv); // value 1 .. 15
		setting.descr = mActivity.getString(R.string.txt_master_volume_desc, v2, v3, v1, v4);
		setting.updateView();
		
	}
	
	@Override
	public void deactivate() {
		mActivity.unregisterReceiver(mVolumeReceiver);
	}

	@Override
	public void onSelected(int buttonIndex) {
		// do nothing
	}

	@Override
	public void onSwitched(boolean isSwitched) {
		// do nothing
	}

	@Override
	public void onValueChanged(int value) {

		RangeSetting setting = (RangeSetting) mSetting;
		setting.value = value; 
		int v = (int) (100f / 15 * value); // value in % 
		
		System.out.println("New value: " + setting.value + ", value%=" + v);
		
		AudioManager manager = mManager;
		
		// get max value for state
		int maxv1 = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int maxv2 = manager.getStreamMaxVolume(AudioManager.STREAM_RING);
		int maxv3 = manager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
		int maxv4 = manager.getStreamMaxVolume(AudioManager.STREAM_ALARM);

		// get current volumes
		int v1, v2, v3, v4;

		manager.setStreamVolume(AudioManager.STREAM_MUSIC, v1 =  round(maxv1 * v / 100f), AudioManager.FLAG_PLAY_SOUND);
		manager.setStreamVolume(AudioManager.STREAM_RING, v2 = round(maxv2 * v / 100f), 0);
		manager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, v3 = round(maxv3 * v / 100f), 0);
		manager.setStreamVolume(AudioManager.STREAM_ALARM, v4 = round(maxv4 * v / 100f), 0);
		
		if (manager.getRingerMode() == AudioManager.RINGER_MODE_SILENT & v2 > 0) {
			manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		} else if (manager.getRingerMode() != AudioManager.RINGER_MODE_SILENT && v2 == 0) {
			manager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		}

		setting.descr = mActivity.getString(R.string.txt_master_volume_desc, v2, v3, v1, v4);
		setting.updateView();
		
	}
	
	public static int round(float value) {
		if (value > 0.2f && value < 0.5f) {
			return 1;
		} else {
			return Math.round(value);
		}
	}
	
}

