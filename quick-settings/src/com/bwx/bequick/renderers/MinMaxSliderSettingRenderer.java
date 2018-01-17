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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.bwx.bequick.R;
import com.bwx.bequick.fwk.RangeSetting;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingRenderer;

public class MinMaxSliderSettingRenderer implements SettingRenderer, OnClickListener, OnSeekBarChangeListener {

	private LayoutInflater mInflater;
	private RangeSetting mSetting;
	private Context mContext;
	private LinearLayout mView;

	private ImageButton mMaxButton;
	private ImageButton mMinButton;
	private TextView mDescr;

	// seek bar
	private SeekBar mSlider;
	private PopupWindow mPreviewPopup;
	private TextView mPreviewText;

	public View getView(LayoutInflater inflater, Setting setting, View convertView, Context context) {
		
		// reuse if view already created
		if (mView != null) return mView;

		mInflater = inflater;
		mContext = context;
		RangeSetting set = (RangeSetting) setting;
		LinearLayout view = mView = (LinearLayout) inflater.inflate(R.layout.row_setting_minmax_slider, null);
		
		// re-initialize views
		SeekBar slider = mSlider = (SeekBar) view.findViewById(R.id.slider_view);
		mDescr = (TextView) view.findViewById(R.id.descr_view);
		ImageButton minButton = (ImageButton) view.findViewById(R.id.min_button);
		ImageButton maxButton = (ImageButton) view.findViewById(R.id.max_button);
		
		// set listeners
		slider.setOnSeekBarChangeListener(this);
		slider.setOnClickListener(this);
		maxButton.setOnClickListener(this);
		minButton.setOnClickListener(this);
		
		// set values
		minButton.setImageResource(set.minIconId);
		maxButton.setImageResource(set.maxIconId);
		
		mMinButton = minButton;
		mMaxButton = maxButton;
		mSetting =  set;
		
		// update view
		updateView();

		return view;
	}

	/**
	 * a button min or max is clicked
	 */
	public void onClick(View view) {
		RangeSetting setting = mSetting;
		if (view.equals(mMinButton)) {
			setting.value = setting.min;
			mSlider.setProgress(setting.value);
		} else if (view.equals(mMaxButton)) {
			setting.value = setting.max;
			mSlider.setProgress(setting.value);
		}
		updatePreviewText(setting);
		setting.notifySettingValueChanged(setting.value);
		setting.notifyButtonClicked(1);
	}

	public void notifySettingUpdated() {
		updateView();
	}

	void updateView() {
		SeekBar slider = mSlider;
		RangeSetting setting = mSetting;
		
		// ignore for now
		/*
		boolean enabled = setting.enabled;
		mMinButton.setEnabled(enabled);
		mMaxButton.setEnabled(enabled);
		slider.setEnabled(enabled);
		*/
		
		slider.setMax(setting.max);
		slider.setProgress(setting.value);
		
		// update description
		final String descr = setting.descr;
		final TextView descrView = mDescr;
		descrView.setText(descr);
		descrView.setVisibility(descr == null ? View.GONE : View.VISIBLE);
		
		updatePreviewText(setting);
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			mSetting.notifySettingValueChanged(progress);
			updatePreviewText(mSetting);
		}
	}

	private void updatePreviewText(RangeSetting setting) {
		TextView preview = mPreviewText;
		if (preview != null) {
			int min = setting.min;
			int max = setting.max;
			int progress = setting.value;
			if (max != 100 || min != 0) {
				// calculate progress in %
				int range = max - min;
				progress = progress * 100 / range;
			}
			preview.setText(String.valueOf(progress) + " %");
		}
	}
	
	public void onStartTrackingTouch(SeekBar seekBar) {
		LinearLayout view = mView;
		
		// create preview popup
		PopupWindow popup = mPreviewPopup;
		if (popup == null) {
			Context context = mContext;
			
			TextView text = mPreviewText = (TextView) mInflater.inflate(R.layout.popup_text, null);
			final float scale = context.getResources().getDisplayMetrics().density;
			popup = new PopupWindow(text, (int) (70 * scale), (int) (56 * scale));
			popup.setBackgroundDrawable(context.getResources().getDrawable(android.R.drawable.alert_dark_frame));
			popup.setContentView(text);
			mPreviewPopup = popup;
			
			updatePreviewText(mSetting);
		}
		
		int xoff = (view.getWidth() - popup.getWidth() ) / 2;
		int yoff = view.getHeight() + (int) (popup.getHeight() / 1.2);
		popup.showAsDropDown(view, xoff, -yoff);			
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		mPreviewPopup.dismiss();
		mSetting.notifyButtonClicked(1); // update value
	}

}
