package com.dummies.android.silentmodetoggle;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {

	private AudioManager mAudioManager;
	private boolean mPhoneIsSilent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

		checkIfPhoneIsSilent();

		setButtonClickListener();

		Log.d("SilentModeApp", "This is a test");
	}

	private void setButtonClickListener() {
		Button toggleButton = (Button) findViewById(R.id.toggleButton);
		toggleButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mPhoneIsSilent) {
					// Change back to normal mode
					mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
					mPhoneIsSilent = false;
				} else {
					// Change to silent mode
					mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
					mPhoneIsSilent = true;
				}

				// Now toggle the UI again
				toggleUi();
			}
		});
	}

	/**
	 * Checks to see if the phone is currently in silent mode.
	 */
	private void checkIfPhoneIsSilent() {
		int ringerMode = mAudioManager.getRingerMode();
		if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
			mPhoneIsSilent = true;
		} else {
			mPhoneIsSilent = false;
		}

	}

	/**
	 * Toggles the UI images from silent to normal and vice versa.
	 */
	private void toggleUi() {

		ImageView imageView = (ImageView) findViewById(R.id.phone_icon);
		Drawable newPhoneImage;

		if (mPhoneIsSilent) {
			newPhoneImage = getResources().getDrawable(R.drawable.phone_silent);

		} else {
			newPhoneImage = getResources().getDrawable(R.drawable.phone_on);
		}

		imageView.setImageDrawable(newPhoneImage);
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkIfPhoneIsSilent();
		toggleUi();
	};
}