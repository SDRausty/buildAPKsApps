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

import java.util.ArrayList;

import com.bwx.bequick.fwk.Setting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LayoutSettingsAdapter extends BaseAdapter implements OnClickListener {

	private static final LayoutParams FILL_PARRENT = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
	
	// list of settings
	private ArrayList<Setting> mSettings;
	private LayoutInflater mInflater;
	private float mScale;
	
	public LayoutSettingsAdapter(Context context, ArrayList<Setting> settings) {
		mInflater = LayoutInflater.from(context);
		mSettings = settings;
		mScale = context.getResources().getDisplayMetrics().density;
		
		// update index
		int length = settings.size();
		for (int i=0; i<length; i++) {
			settings.get(i).index = i;
		}
	}

    public boolean areAllItemsEnabled() {
        return false;
    }

    public boolean isEnabled(int position) {
    	return mSettings.get(position).id < Setting.GROUP_VISIBLE;
    }
	
	public int getCount() {
		return mSettings.size();
	}

	public Object getItem(int position) {
		return mSettings.get(position);
	}

	public void setItem(int position, Setting setting) {
		setting.index = position;
		mSettings.set(position, setting);
	}
	
	public long getItemId(int position) {
		return mSettings.get(position).id;
	}

	public boolean isInVisibleInList(Setting setting) {
		ArrayList<Setting> settings = mSettings;
		int size = settings.size();
		for (int i=0; i<size; i++) {
			int id = settings.get(i).id;
			if (id == setting.id) return true;
			if (id == Setting.GROUP_HIDDEN) return false;
		}
		return false;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if (convertView == null) {
			// create placeholder view
			convertView = mInflater.inflate(R.layout.row_layout_placeholder, null);
		}
		
		LinearLayout placeholder = (LinearLayout) convertView;
		Setting setting = mSettings.get(position);
		final int id = setting.id;
		
		if (id == Setting.GROUP_VISIBLE || id == Setting.GROUP_HIDDEN) {
			
			// create / initialize separator
			View view = placeholder.findViewById(R.id.separator);
			if (view == null) {
				placeholder.removeAllViews();
				view = mInflater.inflate(R.layout.row_layout_separator, null);
				placeholder.addView(view, FILL_PARRENT);
			}
			
			TextView text = (TextView) view.findViewById(R.id.title);
			text.setText(setting.titleId);
			
			placeholder.setMinimumHeight(0);
		} else if (id == Setting.PLACEHOLDER) {
			
			// do nothing, already there
			placeholder.removeAllViews();
			placeholder.setMinimumHeight((int)(58 * mScale));
		} else {
			
			// create / initialize setting
			View view = placeholder.findViewById(R.id.settings);
			if (view == null) {
				placeholder.removeAllViews();
				view = mInflater.inflate(R.layout.row_layout_setting, null);
				placeholder.addView(view, FILL_PARRENT);
			}
			
			// initialize view
			ImageView image = (ImageView) view.findViewById(R.id.icon_view);
			image.setImageResource(setting.iconId);
			
			TextView text = (TextView) view.findViewById(R.id.title_view);
			text.setText(setting.titleId);

			ImageButton button = (ImageButton) view.findViewById(R.id.wrench);
			boolean visible = setting.prefs != null;
			button.setVisibility(visible ? View.VISIBLE : View.GONE);
			if (visible) {
				button.setTag(setting);
				button.setOnClickListener(this);
			}
			placeholder.setMinimumHeight(0);
		}
		placeholder.requestLayout();
		
		return convertView;
	}

	public ArrayList<Setting> getSettings() {
		return mSettings;
	}

	public void onClick(View v) {
		Setting setting = (Setting) v.getTag();
		setting.startPrefsActivity(v.getContext());
	}
}
