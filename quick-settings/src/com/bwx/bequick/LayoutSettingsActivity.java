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

package com.bwx.bequick;

import android.app.ListActivity;
import android.os.Bundle;

public class LayoutSettingsActivity extends ListActivity {

	// cache
	private SettingsApplication mApplication;
	private LayoutSettingsAdapter mSettingsAdapter;
	
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.settings_layout);
    	// get application
    	mApplication = (SettingsApplication) getApplication();
    	// create settings adapter
    	mSettingsAdapter = new LayoutSettingsAdapter(this, mApplication.getSettings());
    	setListAdapter(mSettingsAdapter);
    }
	
    protected void onResume() {
    	super.onResume();
    	mSettingsAdapter.notifyDataSetChanged();
    	setVisible(true);
    }	
	
    protected void onPause() {
    	super.onPause();
    	// important, as main activity can have modified the list and there will be a IndexOutOfBoundException 
    	setVisible(false);
    }
    
    protected void onStop() {
    	super.onStop();
        mApplication.persistSettings(); // persist settings as they could have been changed
    }
}
