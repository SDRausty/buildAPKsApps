package com.bwx.bequick;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bwx.bequick.fwk.Setting;

public class ListSettingsLayout {

	private final LinearLayout mList;
	private final SettingsApplication mApp;
	
	public ListSettingsLayout(View parent, SettingsApplication app) {
		mList = (LinearLayout) parent.findViewById(R.id.settings_list);
		mApp = app;
	}
	
    public void updateLayout(Activity activity) {
    	
    	final LayoutInflater inflater = activity.getLayoutInflater();
    	final ArrayList<Setting> settings = mApp.getSettings();
    	final int settingCount = getSettingsCount(settings);
    	final LinearLayout layout = mList;

    	// Go through all settings and ensure they have corresponding 
    	// views in the layout. Then remove not used views in such.
    	
    	for (int settingIndex = 0; settingIndex < settingCount; settingIndex++) {

    		int layoutIndex = settingIndex << 1;
    		View layoutView = layout.getChildCount() < layoutIndex ? null : layout.getChildAt(layoutIndex);

    		Setting setting = settings.get(settingIndex + 1);
    		View settingView = setting.getAssignedRenderer().getView(inflater, setting, null, activity);
    		
    		if (layoutView == null) {
    			// we just need to append setting view

    			safelyRemoveViewFromParent(layout, layoutIndex, settingView);

        		// add setting view
        		layout.addView(settingView);
        		
    			// add delimiter if it is not the last setting
    			ImageView delimiter = new ImageView(activity);
    			delimiter.setImageResource(android.R.drawable.divider_horizontal_dim_dark);
    			layout.addView(delimiter);
    			settingView.setTag(delimiter);
    			
    		} else if (layoutView != settingView) {
    			
    			// we just need to remove current view and put setting view instead
    			// replace view
    			layout.removeView(layoutView);

        		// detach it from a parent if there is a parent
    			safelyRemoveViewFromParent(layout, layoutIndex, settingView);
    			
    			layout.addView(settingView, layoutIndex); // add setting view
    			settingView.setTag(layout.getChildAt(layoutIndex + 1)); // reuse delimiter
    		} 
    		// else - do nothing, this is same view
    		
    	}
    	
    	// if we have more views then settings, we have to remove redundant views
    	final int layoutCount = layout.getChildCount();
    	final int expectedCount = settingCount << 1;
    	if (layoutCount > expectedCount) {
    		layout.removeViews(expectedCount, layoutCount - expectedCount);
    	}
    }

    
    /**
     * removes setting view and its delimiter from a parent
     * @param layout
     * @param layoutIndex
     * @param settingView
     */
    private void safelyRemoveViewFromParent(LinearLayout layout, int layoutIndex, View settingView) {
    	LinearLayout parent = (LinearLayout) settingView.getParent();
		if (parent != null) { 
			View delimiter = (View) settingView.getTag();
			parent.removeView(settingView);
			parent.removeView(delimiter);
		}
    }

    private int getSettingsCount(ArrayList<Setting> settings) {
		int size = settings.size();
		for (int index=0; index<size; index++) {
			int id = settings.get(index).id;
			if (id == Setting.GROUP_HIDDEN) return index - 1;
		}
		return size;
    }

	
}
