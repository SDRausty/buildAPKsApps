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

package com.bwx.bequick.handlers;

import com.bwx.bequick.MainSettingsActivity;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingHandler;

public class VolumeControlSettingHandler extends SettingHandler {

	private VolumeDialog mDialog;

	public VolumeControlSettingHandler(Setting setting) {
		super(setting);
	}

	@Override
	public void activate(MainSettingsActivity activity) throws Exception {
		mActivity = activity;
		mDialog = new VolumeDialog(activity);
	}

	@Override
	public void deactivate() {
		mDialog.dismiss();
		mDialog = null;
	}

	@Override
	public void onSelected(int buttonIndex) {
		VolumeDialog dialog = mDialog;
		if (dialog != null) dialog.show();
	}

	@Override
	public void onSwitched(boolean switched) {
		// do nothing
	}

	@Override
	public void onValueChanged(int value) {
		// do nothing
	}
}
