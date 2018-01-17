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

public class ShakeSwitchDeterctor extends SwitchDetector implements SensorEventListener {

	private final long mShakeIntervalThreshold = 1500; // ms
	private final long mAttemptIntervalThreshold = 170; // ms
	private final float mShakeForceThreshold = 10.2f; // m/s^2
	private final int mShakeAttemptCountThreshold = 3;
	
	private long mLastShakeAttemptTime = System.currentTimeMillis();
	private long mLastShakeTime = System.currentTimeMillis();
	private int mLastShakeFingerprint;
	private int mShakeAttemptCounter;
	
	private boolean mOn;

	public ShakeSwitchDeterctor(Switchable swithable) {
		super(swithable, R.string.msg_flashlight_shake);
	}
	
	public void activate(Context context) {
		SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	if (sensor != null) {
    		sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
    	}
	}
	
	public void inactivate(Context context) {
		SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	if (sensor != null) {
    		sensorManager.unregisterListener(this, sensor);
    	}
	}
	
	public void onSensorChanged(SensorEvent event) {
		long now = System.currentTimeMillis();
		if (now - mLastShakeTime < mShakeIntervalThreshold) return;
			
		float[] values = event.values;
		float fx = values[0];
		float fy = values[1];
		float fz = values[2];
		
		double f1 = Math.sqrt(Math.pow(fx, 2) + Math.pow(fy, 2)); 
		double f2 = Math.sqrt(Math.pow(fx, 2) + Math.pow(fz, 2)); 
		double f3 = Math.sqrt(Math.pow(fz, 2) + Math.pow(fy, 2)); 

		int fingerprint; 
		if (f1 > f2) {
			if (f1 > f3) {
				fingerprint = 1; // f1 max
			} else {
				fingerprint = 4; // f3 max
			}
		} else {
			if (f2 > f3) {
				fingerprint = 2; // f2 max
			} else {
				fingerprint = 4; // f3 max
			}
		}
		
		long attemptInterval = now - mLastShakeAttemptTime;
		boolean isShake = f1 > mShakeForceThreshold || f2 > mShakeForceThreshold || f3 > mShakeForceThreshold; 
		
		//System.out.println(interval + " " + fingerprint + " " + f1 + " " + f2 + " " + f3);
		
		if (isShake) {

			if (attemptInterval < mAttemptIntervalThreshold) {
				
				// shake attempt detected
				if (fingerprint != mLastShakeFingerprint) {
					
					mLastShakeFingerprint = fingerprint;
					
					// it's a different direction
					if (++mShakeAttemptCounter > mShakeAttemptCountThreshold) {
						
						// this is a shake! 
						//System.out.println("\nSHAKED!!!\n");
						mShakeAttemptCounter = 0;
						mLastShakeTime = now;
						
						toggleState();
						
					} // else, not enough attempts, continue to listen
				} // else, continue to listen
				
			} else {
				// timeout, reset counter
				mShakeAttemptCounter = 0;
			}
			
			mLastShakeAttemptTime = now;

		}
		
	}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// do nothing
	}
	
    private void toggleState() {
    	mOn = !mOn;
    	mSwitchable.switchLight(mOn);
    }
}
