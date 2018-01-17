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

import static com.bwx.bequick.Constants.PREFS_COMMON;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public abstract class BasePrefs extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private final int mLayoutResource;
	
	public BasePrefs(int layoutResource) {
		mLayoutResource = layoutResource;
	}
	
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	PreferenceManager manager = getPreferenceManager();
    	manager.setSharedPreferencesName(PREFS_COMMON);
    	addPreferencesFromResource(mLayoutResource);
    }
	
    protected void onResume() {
    	super.onResume();
    	getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
    }
    
	public abstract void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key);
}
