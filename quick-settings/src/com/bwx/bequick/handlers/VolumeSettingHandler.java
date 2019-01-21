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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.media.AudioManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.bwx.bequick.Constants;
import com.bwx.bequick.MainSettingsActivity;
import com.bwx.bequick.R;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingHandler;


public class VolumeSettingHandler extends SettingHandler implements OnSeekBarChangeListener, OnClickListener, OnCancelListener {

	//private static final String TAG = "VolumeSettingHandler";
	private static final int BUTTON_CANCEL = -2;

	private int mIdCounter;
	
	private static final int[] STREAM_TYPES = {
		AudioManager.STREAM_RING,
		AudioManager.STREAM_NOTIFICATION,
		AudioManager.STREAM_MUSIC,
		AudioManager.STREAM_ALARM,
		AudioManager.STREAM_VOICE_CALL,
		AudioManager.STREAM_SYSTEM
	};

	private static final int[] STRING_IDS = {
		R.string.txt_volume_ringer,
		R.string.txt_volume_notification,
		R.string.txt_volume_media,
		R.string.txt_volume_alarm,
		R.string.txt_volume_voice_call,
		R.string.txt_volume_system
	};
	
	private TextView[] mValueViews;
	private SeekBar[] mSeekBars;
	private int[] mVolumes;
	private Dialog mDialog;
	
	/**
	 * <li> Ringer volume
	 * <li> Music volume
	 * <li> in-call voice volume
	 * <li> notification volume
	 * <li> alarm volume
	 * @param setting
	 */
	public VolumeSettingHandler(Setting setting) {
		super(setting);
	}

	@Override
	public void activate(MainSettingsActivity activity) throws Exception {
		mActivity = activity;
		//Log.d(TAG, "activate: " + activity);
	}

	@Override
	public void deactivate() {
		//Log.d(TAG, "deactivate");
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
	}

	@Override
	public void onSelected(int buttonIndex) {
		mDialog = createDialog();
		prepareDialog();
		mDialog.show();
	}

	@Override
	public void onSwitched(boolean isSwitched) {
		// do nothing
	}

	@Override
	public void onValueChanged(int value) {
		// do nothing
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {

			final AudioManager manager = (AudioManager) mActivity.getSystemService(Activity.AUDIO_SERVICE);
			final int index = (Integer) seekBar.getTag();
			final int streamType = STREAM_TYPES[index];
			
			// update view
			final TextView valueView = mValueViews[index];
			valueView.setText(progress + "/" + manager.getStreamMaxVolume(streamType));
			
			//Log.d(TAG, "onProgressChanged: index: " + index + ", value: " + progress + ", seekbar: " + seekBar);
		}
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		// do nothing
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		// do nothing
	}

	public void onCancel(DialogInterface dialog) {
		mDialog.hide();
	}
	
	public void onClick(DialogInterface dialog, int whichButton) {

		if (BUTTON_CANCEL == whichButton) {
			return; // stop on cancel
		}
		
		Activity activity = mActivity;
		
		// cache
		final AudioManager manager = (AudioManager) activity.getSystemService(Activity.AUDIO_SERVICE);
		final int[] volumes = mVolumes;
		final SeekBar[] seekBars = mSeekBars;
		final int[] streamTypes = STREAM_TYPES;
		
		// apply changes
		final int length = seekBars.length;
		boolean prevChanged = false;
		
		for (int i = 0; i<length; i++) {
			// get control
			int value = seekBars[i].getProgress();
			boolean isNotification = i == 1;
			boolean changed = value != volumes[i] 
               || (isNotification && prevChanged);

			if (changed) {
				manager.setStreamVolume(streamTypes[i], value, 0);
				//Log.d(TAG, "updating index: " + i + ", value: " + value);
			}
			
			if (isNotification) {
				// if notification change updated volume as well we should return volume back (2.0 behavior)
				
				int ringerVolumeDesired = seekBars[0].getProgress();
				int ringerVolumeActual = manager.getStreamVolume(streamTypes[0]);
				if (ringerVolumeDesired != ringerVolumeActual) {
					manager.setStreamVolume(streamTypes[0], ringerVolumeDesired, 0);
					//Log.d(TAG, "changing ringer volume to desired value: " + + value);
				}
			}
			
			prevChanged = changed;
		}

		// check if we have to switch silent / not silen mode
		int ringerVolume = manager.getStreamVolume(AudioManager.STREAM_RING);
		int notifVolume = manager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
		int ringerMode = manager.getRingerMode();
		if (ringerVolume > 0 && ringerMode == AudioManager.RINGER_MODE_SILENT) {
			Toast.makeText(activity, activity.getString(R.string.msg_not_silent_warning, ringerVolume), Toast.LENGTH_LONG).show();
		} else if (ringerVolume == 0 && ringerMode == AudioManager.RINGER_MODE_NORMAL) {
			Toast.makeText(activity, R.string.msg_zero_ringer_warning, Toast.LENGTH_LONG).show();
		} else if (notifVolume == 0 && ringerMode == AudioManager.RINGER_MODE_NORMAL) {
			Toast.makeText(activity, R.string.msg_zero_notification_warning, Toast.LENGTH_LONG).show();
		}
		
		// send internal volume changed notification
		activity.sendBroadcast(new Intent(Constants.ACTION_VOLUME_UPDATED));

	}
	
	public boolean prepareDialog() {

		// local cache
		final AudioManager manager = (AudioManager) mActivity.getSystemService(Activity.AUDIO_SERVICE);
		final SeekBar[] seekBars = mSeekBars;
		final TextView[] valueViews = mValueViews;
		final int[] volumes = mVolumes;
		final int length = seekBars.length;
		final int[] streamTypes = STREAM_TYPES;
		
		for (int i=0; i<length; i++) {
			
			// get value and max value
			int volume = manager.getStreamVolume(streamTypes[i]);
			int max = manager.getStreamMaxVolume(streamTypes[i]);
			
			// update values
			final TextView valueView = valueViews[i];
			valueView.setText(volume + "/" + max);
			
			// update seekbar
			SeekBar seekBar = seekBars[i];
			seekBar.setMax(max);
			seekBar.setProgress(volume);
			
			volumes[i] = volume; // store initial volume
			
			//Log.d(TAG, "prepare: index: " + i + ", value: " + volume + ", max: " + max + ", seekbar: " + seekBar);
		}
		return true;
	}	
	
	private Dialog createDialog() {
		
		if (mDialog != null) return mDialog;
		
		Activity activity = mActivity;
		
		// load main view
		final LayoutInflater factory = LayoutInflater.from(activity);
		final View dialog = factory.inflate(R.layout.settings_volume_streams, null);
		final ViewGroup parent = (ViewGroup) dialog.findViewById(R.id.placeholder);
		
		// local cache
		final int[] stringIds = STRING_IDS;
		final int length = stringIds.length;
		final SeekBar[] seekBars = mSeekBars = new SeekBar[length];
		final TextView[] valueViews = mValueViews = new TextView[length]; 		
		mVolumes = new int[length];
		
		// add controls for all stream
		for (int i=0; i<length; i++) {
			
			View control = factory.inflate(R.layout.row_volume, null);
			
			// init text
			TextView text = (TextView) control.findViewById(R.id.text);
			text.setText(stringIds[i]);

			// init values
			valueViews[i] = (TextView) control.findViewById(R.id.value);

			// init seekbars
			SeekBar seekBar = (SeekBar) control.findViewById(R.id.seekbar);
			seekBar.setOnSeekBarChangeListener(this);
			seekBar.setId(mIdCounter++); // workaround for seekbar id bug
			seekBar.setTag(i);
			seekBars[i] = seekBar;
			
			// add to parent
			parent.addView(control);

			//Log.d(TAG, "create: index: " + i + ", seekbar: " + seekBar);
		}
		
		AlertDialog d = new AlertDialog.Builder(activity)
			.setIcon(R.drawable.ic_dialog_menu_generic)
			.setTitle(R.string.txt_volume)
			.setView(dialog)
			.setPositiveButton(R.string.btn_set, this)
			.setNegativeButton(R.string.btn_calcel, this)
			.create();
		
		d.setOnCancelListener(this);
		
		return d;
	}
}
