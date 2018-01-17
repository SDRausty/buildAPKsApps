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
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ToggleButton;
import android.widget.ViewSwitcher;

import com.bwx.bequick.R;
import com.bwx.bequick.fwk.RangeSetting;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingRenderer;

public class SwitchableSliderRenderer implements SettingRenderer, OnClickListener {

	private LayoutInflater mInflater;
	private RangeSetting mSetting;
	private Context mContext;

	// view structure
	//private LinearLayout mView;
	private ViewSwitcher mSwitcher;
	private ToggleButton mToggle;

	// controlled renderers
	private MinMaxSliderSettingRenderer mSliderRenderer;
	private LinkSettingRenderer mBaseRenderer;
	private int mChildView = -1;
	
	public View getView(LayoutInflater inflater, Setting setting, View convertView, Context context) {
		
		// reuse if view already created
		//if (mView != null) return mView;

		// re-create view every time to disable unwanted animation every time window is opened
		mSliderRenderer = null;
		mBaseRenderer = null;
		mChildView = -1;
		
		mInflater = inflater;
		mContext = context;
		mSetting = (RangeSetting) setting;
		RangeSetting rangeSetting = (RangeSetting) setting;
		
		LinearLayout view = /*mView =*/ (LinearLayout) inflater.inflate(R.layout.row_switchable_slider_view, null);
		
		ViewSwitcher switcher = mSwitcher = (ViewSwitcher) view.findViewById(R.id.switcher);
		switcher.setOutAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_out_down));
		switcher.setInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_in_up));
		switcher.setAnimateFirstView(false);
		
		ToggleButton toggle = mToggle = (ToggleButton) view.findViewById(R.id.toggle);
		toggle.setOnClickListener(this);
		
		// initialize currently active view
		updateView(rangeSetting, toggle);
		
		return view;
	}
	
	private void updateView(RangeSetting setting, ToggleButton toggle) {
		
		boolean checked = setting.checked;
		ViewSwitcher switcher = mSwitcher;
		
		// update view
		if (checked) {
			LinkSettingRenderer renderer = mBaseRenderer;
			if (renderer == null) {
				renderer = mBaseRenderer = new LinkSettingRenderer();
				View child = renderer.getView(mInflater, setting, null, mContext);
				switcher.addView(child);
				if (mChildView == -1) mChildView = 0;
			}
			renderer.updateView();
		} else {
			MinMaxSliderSettingRenderer renderer = mSliderRenderer;
			if (renderer == null) {
				renderer = mSliderRenderer = new MinMaxSliderSettingRenderer();
				View child = renderer.getView(mInflater, setting, null, mContext);
				switcher.addView(child);
				if (mChildView == -1) mChildView = 0;
			} 
			renderer.updateView();
		}
		
		switcher.setDisplayedChild(mChildView);
		
		// update toggle
		toggle.setChecked(checked);
		toggle.setEnabled(setting.enabled);
	}
	
	public void notifySettingUpdated() {
		updateView(mSetting, mToggle);
	}

	public void onClick(View v) {
		
		mChildView = ~ mChildView & 0x1; // 0 or 1
		
		final ToggleButton toggle = mToggle;
		final RangeSetting setting = mSetting;
		boolean checked = toggle.isChecked();
		toggle.toggle(); // preserve state
		
		setting.notifySwitchClicked(checked);
	}

}
