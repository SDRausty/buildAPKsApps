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

package com.bwx.bequick.renderers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bwx.bequick.R;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingRenderer;

public class ToggleSettingRenderer implements SettingRenderer, OnClickListener, OnLongClickListener {

	// main group view
	private View mView;
	private Setting mSetting;
	
	// cached sub-views
	private TextView mTitle;
	private TextView mDescr;
	private ImageView mIcon;
	private ToggleButton mToggle;
	
	public View getView(LayoutInflater inflater, Setting setting, View convertView, Context context) {

		// reuse if view exists
		if (mView != null) return mView;
		
		// create new view always. this is important for "dialog" mode
		mView = convertView = inflater.inflate(R.layout.row_setting_toggle, null); // inflate view of needed type
		
		// re-initialize views
		mTitle = (TextView) convertView.findViewById(R.id.title_view);
		mDescr = (TextView) convertView.findViewById(R.id.descr_view);
		mToggle = (ToggleButton) convertView.findViewById(R.id.clicable_right);
		mIcon = (ImageView) convertView.findViewById(R.id.icon_view);
		
		ImageButton button = (ImageButton) convertView.findViewById(R.id.popup_button);
		button.setVisibility(setting.hasPopup ? View.VISIBLE : View.GONE);
		LinearLayout clicableLeft = (LinearLayout) convertView.findViewById(R.id.clickable_left);
		
		// set listeners
		mToggle.setOnClickListener(this);
		button.setOnClickListener(this);
		clicableLeft.setOnClickListener(this);
		clicableLeft.setOnLongClickListener(setting.prefs == null ? null : this);

		// set values
		mSetting = setting;
		
		// update view
		updateView();
		
		return convertView;
	}

	public void onClick(View view) {
		if (view instanceof ToggleButton) {
			boolean isChecked = mToggle.isChecked();
			mToggle.toggle(); // preserve state
			mSetting.notifySwitchClicked(isChecked);
		} else if (view instanceof ImageButton) {
			mSetting.notifyButtonClicked(1);
		} else {
			mSetting.notifyButtonClicked(0);
		}
	}

	public void notifySettingUpdated() {
		updateView();
	}

	private void updateView() {
		Setting setting = mSetting;
		mTitle.setText(setting.titleId);
		mDescr.setText(setting.descr);
		mIcon.setImageResource(setting.iconId);
		
		ToggleButton toggle = mToggle;
		toggle.setChecked(setting.checked);
		toggle.setEnabled(setting.enabled);
	}

	public boolean onLongClick(View v) {
		mSetting.startPrefsActivity(v.getContext());
		return true;
	}

}
