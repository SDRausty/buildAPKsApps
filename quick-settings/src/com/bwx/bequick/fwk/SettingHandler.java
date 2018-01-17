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

package com.bwx.bequick.fwk;

import com.bwx.bequick.MainSettingsActivity;

public abstract class SettingHandler {
	
	protected final Setting mSetting;

	// cache
	protected MainSettingsActivity mActivity;
	
	public SettingHandler(Setting setting) {
		mSetting = setting;
		setting.assignHandler(this);
	}

	protected String getString(int resId, Object...args) {
		return mActivity.getString(resId, args);
	}
	
	/**
	 * This method is called for every visible setting when main activity gets visible.
	 * Handler implementation should register needed intent receivers here and update
	 * setting view.
	 * 
	 * @param activity	current activity which should be cached for further use
	 * @throws Exception	if an implementation thrown an exception then main
	 * 						activity will inform user that this setting is not supported
	 * 						on user's system and this setting will disappear from the list of
	 * 						supported settings. It is always a good idea to fail here then 
	 * 						later, when user will try to toggle the setting.
	 */
	public abstract void activate(MainSettingsActivity activity) throws Exception;
	
	/**
	 * SettingHandler must unregister receivers here.
	 */
	public abstract void deactivate();
	
	// callbacks from UI controls
	public abstract void onSwitched(boolean switched);
	public abstract void onSelected(int buttonIndex);
	public abstract void onValueChanged(int value);
	
	
	
}
