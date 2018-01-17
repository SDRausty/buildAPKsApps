/*
 * Copyright (C) 2010 Sergej Shafarenka, beworx.com
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

package com.bwx.bequick.flashlight;

import com.bwx.bequick.R;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class OrientationSwitchDetector extends SwitchDetector implements SensorEventListener {

	private SensorManager mSensorManager;
	private Sensor mSensor;

	private Boolean mState;
	
	public OrientationSwitchDetector(Switchable switchable) {
		super(switchable, R.string.msg_flashlight_orientation);
	}

	public void activate(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void inactivate(Context context) {
		if (mSensorManager != null && mSensor != null) {
			 mSensorManager.unregisterListener(this, mSensor);
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// do nothing
	}

	public void onSensorChanged(SensorEvent event) {
        float pinch = Math.abs(event.values[1]);

        if (pinch < 30) { // up
            updateState(true);
        } else if (pinch > 75) { // downside
            updateState(false);
        }
	}

    private void updateState(boolean up) {

        if (mState == null) {
            mState = new Boolean(up);
            onStateChanged();
        } else {

            // post event only if state changes
            if (mState != up) {
                mState = up;
                onStateChanged();
            }
        }
    }

	private void onStateChanged() {
		mSwitchable.switchLight(!mState);
	}
	
 }
