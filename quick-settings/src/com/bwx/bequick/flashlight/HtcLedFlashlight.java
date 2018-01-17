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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import com.bwx.bequick.Constants;

import android.content.Context;
import android.util.Log;

public class HtcLedFlashlight implements Flashlight {

	private static final String TAG = "qs.htcled";
	private static final String PATH = "/sys/devices/platform/flashlight.0/leds/flashlight/brightness";
	private static final String ON = "126";
	private static final String OFF = "0";
	
	private File mFile;
	
	public HtcLedFlashlight() {
		mFile = new File(PATH);
	}
	
	public boolean isOn(Context context) {
		String value = readValue();
		if (value != null && value.length() > 0) {
			return !OFF.equals(value);
		} else {
			return false;
		}
	}

	public void setOn(boolean on, Context context) {
		writeValue(on ? ON : OFF);
	}

	private String readValue() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(mFile));
			String value = br.readLine();
			return value.trim();
		} catch (Exception e) {
			return null;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e2) {
					// ignore
				}
			}
		}
	}
	
	private boolean writeValue(String value) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(mFile);
			fw.write(value);
			
			if (Constants.DEBUG) {
				Log.d(TAG, "set brightness: " + value);
			}
			
			return true;
		} catch (Exception e) {
			return false;
		} finally {	
			if (fw != null) {
				try {
					fw.close();
				} catch (Exception e2) {
					// ignore
				}
			}
		}
	}

	public boolean isSupported(Context context) {
		boolean supported = mFile.exists();
		if (supported) {
			setOn(true, context);
			supported = isOn(context);
			setOn(false, context);
		}
		Log.d(TAG, "isSupported: " + supported);
		return supported;
	}

	public int getType() {
		return TYPE_HTC;
	}
	
}
