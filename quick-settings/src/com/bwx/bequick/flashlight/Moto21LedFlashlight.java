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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;

public class Moto21LedFlashlight implements Flashlight {

	private static final String TAG = "qs.motoled";
	private Object mService;
	
	public boolean isOn(Context context) {
    	try {
    		Object service = getFlashlightService(context);
    		Method getFlashlightEnabled = service.getClass().getMethod("getFlashlightEnabled");
    		Boolean res = (Boolean) getFlashlightEnabled.invoke(service);
    		return res.booleanValue();
    	} catch (Exception e) {
    		Log.e(TAG, "cannot get flashlight state", e);
    		return false;
    	}
	}

	public void setOn(boolean on, Context context) {
    	try {
    		Object service = getFlashlightService(context);
    		Method setFlashlightEnabled = service.getClass().getMethod("setFlashlightEnabled", boolean.class);
    		setFlashlightEnabled.invoke(service, on);
    	} catch (Exception e) {
    		Log.e(TAG, "cannot enable flashlight", e);
    	}
	}

    private Object getFlashlightService(Context context) throws Exception {
    	Object service = mService;
    	if (service == null) {
			Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
			Field f = Class.forName(vibrator.getClass().getName()).getDeclaredField("mService");
			f.setAccessible(true);
			service = mService = f.get(vibrator);
    	}
    	return mService;
    }

	public boolean isSupported(Context context) {
		setOn(true, context);
		boolean supported = isOn(context);
		setOn(false, context);
		return supported;
	}

	public int getType() {
		return TYPE_MOTO21;
	}

}
