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

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.util.Log;

public class FroyoLedFlashlight implements Flashlight {

	private static final String TAG = "qs.floyoled";
	
	private static final String MODE_TORCH = Camera.Parameters.FLASH_MODE_TORCH;
	private static final String MODE_OFF = Camera.Parameters.FLASH_MODE_OFF;

	private Camera mCamera;

	public boolean isOn(Context context) {
		return mCamera != null && MODE_TORCH.equals(mCamera.getParameters().getFlashMode());
	}

	public boolean isSupported(Context context) {
		int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
		Camera camera = null;
		try {
			camera = Camera.open();
			return sdkVersion >= 8 /*froyo*/ && camera.getParameters().getFlashMode() != null; /*flash is supported*/
		} catch (Exception e) {
			Log.e(TAG, "isSupported", e);
			return false;
		} finally {
			if (camera != null) {
				camera.release();
			}
		}
	}

	public void setOn(boolean on, Context context) {
		Camera camera = mCamera;
		if (on) {
			if (camera == null) {
				mCamera = camera = Camera.open();
				camera.startPreview();
			}
			Parameters params = camera.getParameters();
			params.setFlashMode(MODE_TORCH);
			camera.setParameters(params);
		} else {
			if (camera != null) {
				try {
					Parameters params = camera.getParameters();
					params.setFlashMode(MODE_OFF);
					camera.setParameters(params);
				} finally {
					camera.release();
					mCamera = null;
				}
			}
		}
	}

	public int getType() {
		return TYPE_FROYO;
	}

}
