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

package com.bwx.bequick.preferences;

import java.util.Map;

import static com.bwx.bequick.Constants.*;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;

import com.bwx.bequick.Constants;
import com.bwx.bequick.R;

public class GpsPrefs extends BasePrefs {

    public GpsPrefs() {
		super(R.layout.prefs_gps);
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	detectGpsMode(getPreferenceManager().getSharedPreferences());
    }
    
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// nothing
	}

	public static boolean detectGpsMode(SharedPreferences preferences) {
    	Map<String, ?> prefs = preferences.getAll();
    	Boolean mode = (Boolean) prefs.get(PREF_GPS_MODE);
    	if (mode == null) {
    		// detect default mode
    		mode = Constants.SDK_VERSION < 9; // true - allowed
    		if (DEBUG) {
    			Log.d(TAG, "Detected " + PREF_GPS_MODE + " " + mode);
    		}
    		// update preferences
    		Editor editor = preferences.edit();
    		editor.putBoolean(PREF_GPS_MODE, mode).commit();
    	}
    	return mode;
	}
	
}
