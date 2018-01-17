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

import static com.bwx.bequick.Constants.SDK_VERSION;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;

import com.bwx.bequick.Constants;

public class LedFlashlightReceiver extends BroadcastReceiver {

	private static final String TAG = "qs.led";
	
	// a callback action AlarmManager sends us periodically
	private static final String ACTION_WARN = "com.bwx.bequick.WARN_FLASHLIGHT";

	// UI sends this action in oder to control flashlight state (uses EXT_ENABLED)
	public static final String ACTION_CONTROL_FLASHLIGHT = "com.bwx.bequick.FLASHLIGHT";
	
	// receiver sends this event when flashlight state gets changed
	public static final String ACTION_FLASHLIGHT = "com.bwx.bequick.FLASHLIGHT_STATE";
	
	public static final String EXT_ENABLED = "enabled";
	
	private static final String PREF_WARN_COUNT = "warn_count";
	private static final int WARN_TIMEOUT = 300000; // 5 min
	//private static final int WARN_TIMEOUT = 6 * 1000;

	private static Flashlight FLASHLIGHT;
	
	/**
	 * @param context
	 * @return	led flashlight type or -1 if not supported
	 */
	public static int detectLedFlashlightType(Context context) {

		Flashlight flashlight = detectFromBuild();

		if (flashlight == null) { // still null, try and fail
			for (int i=0; i<Flashlight.TYPES_NUMBER; i++) {
				flashlight = checkFlashlight(i);
				if (flashlight != null && flashlight.isSupported(context)) {
					break;
				}
				flashlight = null;
			}
		}
		
		int type = flashlight == null ? -1 /*not supported*/ : flashlight.getType();
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_COMMON, Context.MODE_WORLD_WRITEABLE);
		prefs.edit().putInt(Constants.PREF_FLASHLIGHT_TYPE, type).commit();
		return type;
	}
	
	private static Flashlight detectFromBuild() {
		if (SDK_VERSION >= 8 && ("sholes".equals(Build.DEVICE) /*droid*/ 
					|| "shadow".equals(Build.BOARD) /*droidx*/ 
					|| "droid2".equals(Build.BOARD) /*droid2*/)) {
				return new Droid22Flashlight();
		} else if (SDK_VERSION >= 7 && 
				("U20a".equals(Build.DEVICE) || "E10a".equals(Build.DEVICE) /*X10*/ )) {
			return new FroyoLedFlashlight();
		}
		return null;
	}
	
	private static Flashlight checkFlashlight(int item) {
		try {
			switch(item) {
				case 0: return new HtcLedFlashlight();
				case 1: return new FroyoLedFlashlight();
				case 2: return new Droid22Flashlight();
				case 3: return new Moto21LedFlashlight();
				default: return null;
			}
		} catch (Exception e) {
			Log.e(TAG, "led not supported", e);
			return null;
		}
	}
	
	private static Flashlight getFlashlight(Context context) {
		
		if (FLASHLIGHT == null) {
			synchronized(LedFlashlightReceiver.class) {
				if (FLASHLIGHT == null) {
					SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_COMMON, Context.MODE_WORLD_WRITEABLE);
					int type = prefs.getInt(Constants.PREF_FLASHLIGHT_TYPE, -2); /* -2 means not yet detected */
					if (type == -2) {
						// this can happen on first start after upgrate to 1.9.4 p1
						type = detectLedFlashlightType(context);
					}
					switch(type) {
						case Flashlight.TYPE_MOTO21: FLASHLIGHT = new Moto21LedFlashlight(); break;
						case Flashlight.TYPE_HTC: FLASHLIGHT = new HtcLedFlashlight(); break;
						case Flashlight.TYPE_FROYO: FLASHLIGHT = new FroyoLedFlashlight(); break;
						case Flashlight.TYPE_DROID22: FLASHLIGHT = new Droid22Flashlight(); break;
						default: break; // stay null
					}
				}
			}
		}		
		
		return FLASHLIGHT;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		String action = intent.getAction();
		
		if (ACTION_CONTROL_FLASHLIGHT.equals(action)) {

			boolean enabled = intent.getExtras().getBoolean(EXT_ENABLED);
			enableFlashlight(context, enabled);
			clearWarnCounter(context);

			if (enabled) {
				Flashlight flashlight = getFlashlight(context);
				if (flashlight != null && flashlight.isOn(context)) {
					scheduleNextWarn(context);
				} // otherwise do nothing (no need to update UI as it stays switched off anyway)
				
			} else {
				
				// cancel next warn
				PendingIntent operation = getPendingIntent(context);
				AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				alarm.cancel(operation);
				//Log.d(TAG, "warn canceled");
			}
			
		} else if (ACTION_WARN.equals(action)) {
			
			Flashlight flashlight = getFlashlight(context);
			if (flashlight != null && flashlight.isOn(context)) {
				
				int count = getWarnCount(context);
				if (count > 2) {

					enableFlashlight(context, false);
					vibrate(context);
				} else {
					
					scheduleNextWarn(context);
					vibrate(context);
				}
			} // else not further processing needed as flashlight is already switched off
		}
	}

	private static void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) vibrator.vibrate(150);
        //Log.d(TAG, "vibrated");
	}
	
	private static void enableFlashlight(Context context, boolean enabled) {
		// control flashlight
		Flashlight flashlight = getFlashlight(context);
		if (flashlight == null) return;
		try {
			flashlight.setOn(enabled, context);
			// send event
			Intent intent = new Intent(ACTION_FLASHLIGHT);
			context.sendBroadcast(intent);
		} catch (Exception e) {
			// this can happen if flashlight stops working after a system software update
	    	SharedPreferences props = context.getSharedPreferences(Constants.PREFS_COMMON, Context.MODE_WORLD_WRITEABLE);
	    	props.edit().putInt(Constants.PREF_FLASHLIGHT_TYPE, 0).commit(); // screen flashlight
		}
	}
	
    public static boolean isFlashlightEnabled(Context context) {
		Flashlight flashlight = getFlashlight(context);
		if (flashlight == null) return false;
		try {
			return flashlight.isOn(context);
		} catch (Exception e) {
			return false;
		}
    }
    
	private static void scheduleNextWarn(Context context) {
		PendingIntent operation = getPendingIntent(context);
		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + WARN_TIMEOUT, operation);
		//Log.d(TAG, "next warn scheduled");
	}
	
	private static PendingIntent getPendingIntent(Context context) {
		Intent i = new Intent(ACTION_WARN);
        return PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
	}
	
    private static int getWarnCount(Context context) {

    	SharedPreferences props = context.getSharedPreferences(Constants.PREFS_RUNTIME, Context.MODE_WORLD_WRITEABLE);
    	int count = props.getInt(PREF_WARN_COUNT, 0);
  
    	Editor editor = props.edit();
    	editor.putInt(PREF_WARN_COUNT, ++count);
    	editor.commit();
    	
    	return count;
    }
	
    private static void clearWarnCounter(Context context) {
    	
    	SharedPreferences props = context.getSharedPreferences(Constants.PREFS_RUNTIME, Context.MODE_WORLD_WRITEABLE);
    	Editor editor = props.edit();
    	editor.putInt(PREF_WARN_COUNT, 0);
    	editor.commit();
    }
    
}
