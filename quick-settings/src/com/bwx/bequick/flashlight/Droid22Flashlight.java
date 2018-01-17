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

import java.lang.reflect.Method;

import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public class Droid22Flashlight implements Flashlight {

	private static final String TAG = "qs.droid22led";
	private Object mManager;
	
	public int getType() {
		return TYPE_DROID22;
	}

	public boolean isOn(Context context) {
		try {
			Object manager = getManager();
			if (manager != null) {
				Method getFlashlightEnabledMethod = manager.getClass().getMethod("getFlashlightEnabled");
				return (Boolean) getFlashlightEnabledMethod.invoke(manager);
			}
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
		return false;
	}

	public boolean isSupported(Context context) {
		// Build.VERSION.SDK_INT == 8 && ("shadow".equals(Build.BOARD) || "droid2".equals(Build.BOARD) || "sholes".equals(Build.BOARD)) && getManager() != null;
		if (Build.VERSION.SDK_INT == 8 && getManager() != null) {
			setOn(true, context);
			boolean on = isOn(context);
			setOn(false, context);
			return on;
		}
		return false;
	}

	public void setOn(boolean on, Context context) {
		try {
			Object manager = getManager();
			if (manager != null) {
				Method setFlashlightEnabledMethod = manager.getClass().getMethod("setFlashlightEnabled", boolean.class);
				setFlashlightEnabledMethod.invoke(manager, on);
			}
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
	}

	private Object getManager() {
		if (mManager == null) {
			try {
				Class<?> managerClass = Class.forName("android.os.ServiceManager");
				Method methodGetService = managerClass.getMethod("getService", String.class);
				IBinder hardwareService = (IBinder) methodGetService.invoke(managerClass, "hardware");
				
				Class<?> stubClass = Class.forName("android.os.IHardwareService$Stub");
				Method asInterfaceMethod = stubClass.getMethod("asInterface", IBinder.class);
				mManager = asInterfaceMethod.invoke(stubClass, hardwareService);
			} catch (Exception e) {
				Log.e(TAG, "", e);
			}
		}
		return mManager;
	}
}
