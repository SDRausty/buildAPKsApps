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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bwx.bequick.R;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingRenderer;

public class LinkSettingRenderer implements SettingRenderer, OnClickListener, OnLongClickListener {

	private View mView;
	
	private TextView mTitle;
	private ImageView mIcon;
	private TextView mDescr;
	
	private Setting mSetting;
	
	public View getView(LayoutInflater inflater, Setting setting, View convertView, Context context) {
		
		if (mView != null) return mView;
		mView = convertView = inflater.inflate(R.layout.row_setting_link, null);
		
		// re-initialize views
		mTitle = (TextView) convertView.findViewById(R.id.title_view);
		mDescr = (TextView) convertView.findViewById(R.id.descr_view);
		mIcon = (ImageView) convertView.findViewById(R.id.icon_view);
		LinearLayout clicableLeft = (LinearLayout) convertView.findViewById(R.id.clickable_left);

		// set values
		mSetting = setting;
		
		// set listeners
		clicableLeft.setOnClickListener(this);
		clicableLeft.setOnLongClickListener(setting.prefs == null ? null : this);

		// update view
		updateView();
		
		return convertView;
	}

	public void onClick(View view) {
		mSetting.notifyButtonClicked(0);
	}

	public void notifySettingUpdated() {
		updateView();
	}

	void updateView() {
		Setting setting = mSetting;
		mDescr.setText(setting.descr);
		mTitle.setText(setting.titleId);
		mIcon.setImageResource(setting.iconId);
	}

	public boolean onLongClick(View view) {
		mSetting.startPrefsActivity(view.getContext());
		return true;
	}

}
