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

import com.bwx.bequick.Constants;
import com.bwx.bequick.MainSettingsActivity;
import com.bwx.bequick.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.media.AudioManager;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class VolumeDialog implements OnCancelListener, OnClickListener, OnSeekBarChangeListener, OnCheckedChangeListener {

	// from android.provider.Settings
    public static final String NOTIFICATIONS_USE_RING_VOLUME = "notifications_use_ring_volume";
	
	class Holder {
		int index;
		int max;
		TextView text;
		SeekBar seek;
	}
	
	private static final int[] STREAM_TYPES = {
		AudioManager.STREAM_RING,
		AudioManager.STREAM_NOTIFICATION,
		AudioManager.STREAM_MUSIC,
		AudioManager.STREAM_ALARM,
		AudioManager.STREAM_VOICE_CALL,
		AudioManager.STREAM_SYSTEM
	};
	
	private static final int[] TEXT_IDS = {
		R.id.text1, R.id.text2, R.id.text3, R.id.text4, R.id.text5, R.id.text6  
	};
	
	private static final int[] SLIDER_IDS = {
		R.id.slider1, R.id.slider2, R.id.slider3, R.id.slider4, R.id.slider5, R.id.slider6  
	};
	
	private AudioManager mManager;
	private Activity mActivity;
	private Dialog mDialog;

	private Holder mRingerHolder;
	private Holder mNotificationHolder;
	
	private boolean mInitialChecked;
	private boolean mChecked;
	
	public VolumeDialog(MainSettingsActivity activity) {
		mActivity = activity;
	}

	public void show() {
		
		Dialog dialog = mDialog;
		AudioManager manager = mManager;
		
		if (dialog == null) {
			 
			mManager = manager = (AudioManager) mActivity.getSystemService(Activity.AUDIO_SERVICE);

			dialog = new Dialog(mActivity);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.settings_volume_streams);
			dialog.setOnCancelListener(this);
			dialog.findViewById(R.id.button1).setOnClickListener(this);
			dialog.findViewById(R.id.button2).setOnClickListener(this);
			
			CheckBox checkbox = (CheckBox) dialog.findViewById(R.id.checkbox1);
			checkbox.setOnCheckedChangeListener(this);
			mDialog = dialog;
		}
		
		boolean useRingerVolume = useRingerVolume();
		Holder holder; int max, value; TextView text; SeekBar seek;
		int size = STREAM_TYPES.length;
		for (int i=0; i<size; i++) {
			
			if (useRingerVolume && i == 1) {
				value = manager.getStreamVolume(STREAM_TYPES[i-1]); // use ringer volume
			} else {
				value = manager.getStreamVolume(STREAM_TYPES[i]);
			}
			
			max = manager.getStreamMaxVolume(STREAM_TYPES[i]);
			text = (TextView) dialog.findViewById(TEXT_IDS[i]);;
			seek = (SeekBar) dialog.findViewById(SLIDER_IDS[i]);
			
			holder = new Holder();
			holder.index = i;
			holder.text = text;
			holder.max = max;
			holder.seek = seek;
			
			seek.setTag(holder);
			seek.setMax(holder.max);
			seek.setProgress(value);
			seek.setOnSeekBarChangeListener(this);
			
			text.setText(value + "/" + max);
			
			if (i == 0) {
				mRingerHolder = holder;
			} else if (i == 1) {
				mNotificationHolder = holder;
			}
		}
		
		CheckBox checkbox = (CheckBox) dialog.findViewById(R.id.checkbox1);
		checkbox.setChecked(useRingerVolume);
		mInitialChecked = mChecked = checkbox.isChecked();
		
		dialog.show();
	}
	
	public void onCancel(DialogInterface dialog) {
		hide();
	}

	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.button1: {
				

				AudioManager manager = mManager; Dialog dialog = mDialog;
				int size = STREAM_TYPES.length; SeekBar seek;
				for (int i=size; i-->0;) {
					//if (i == 1 && mChecked) continue; // ignore notification volume - we use ringer volume instead
					seek = (SeekBar) dialog.findViewById(SLIDER_IDS[i]);
					manager.setStreamVolume(STREAM_TYPES[i], seek.getProgress(), 0);
				}
				
				Activity activity = mActivity;
				
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
				
				hide();
				break;
			}
			case R.id.button2: {

				// revert initial checked state
				if (mChecked != mInitialChecked) setUseRingerVolume(mInitialChecked);
				
				hide();
				break;
			}
		}
	}

	public void hide() {
		if (mDialog != null) mDialog.hide();
	}

	public void dismiss() {
		if (mDialog != null) mDialog.dismiss();
	}
	
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			Holder holder = (Holder) seekBar.getTag();
			String text = progress + "/" + holder.max;
			holder.text.setText(text);
			if (mChecked && holder == mRingerHolder) {
				mNotificationHolder.seek.setProgress(mRingerHolder.seek.getProgress());
				mNotificationHolder.text.setText(mRingerHolder.text.getText());
			}
		}
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		// do nothing
		
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		// do nothing
	}

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		mChecked = isChecked;
		
		// update flag immediately
		setUseRingerVolume(mChecked);
		
		SeekBar seek = mNotificationHolder.seek;
		if (isChecked) {
			seek.setEnabled(false);
			seek.setProgress(mRingerHolder.seek.getProgress());
			mNotificationHolder.text.setText(mRingerHolder.text.getText());
		} else {
			seek.setEnabled(true);
		}
	}

	private boolean useRingerVolume() {
		return Settings.System.getInt(mActivity.getContentResolver(), NOTIFICATIONS_USE_RING_VOLUME, 1) == 1;
	}

	private void setUseRingerVolume(boolean checked) {
		Settings.System.putInt(mActivity.getContentResolver(), NOTIFICATIONS_USE_RING_VOLUME, checked ? 1 : 0);
	}
}
