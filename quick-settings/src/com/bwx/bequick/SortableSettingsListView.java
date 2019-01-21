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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.bwx.bequick.fwk.Setting;

/**
 * This class is based on TouchInterceptor.java implementation 
 * from Android Open Source Project.
 * 
 * @author sergej@beworx.com 
 */
public class SortableSettingsListView extends ListView {

	static class Dragger {

		private final Setting mSetting;
		private final WindowManager.LayoutParams mWindowParams;
		private final WindowManager mWindowManager;
		private final int mDragPointOffset;
		private final int mCoordOffset;
		private final int mRowHeight;
		
		private Bitmap mBitmap;
		private ImageView mImageView;

		public Dragger(Context context, Setting setting, ViewGroup item, MotionEvent ev) {
			
			mSetting = setting;
            int y = (int) ev.getY();
			
            mDragPointOffset = y - item.getTop();
            mCoordOffset = ((int)ev.getRawY()) - y;
            mRowHeight = item.getHeight();
			
            // enable cache
            // clear cache, otherwise we are going to have problems in donut ;)
            item.setDrawingCacheEnabled(false);
			item.setDrawingCacheEnabled(true);
			// create bitmap
			Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache());
			mBitmap = bitmap;
		
			// create window
            mWindowParams = new WindowManager.LayoutParams();
            
            WindowManager.LayoutParams params = mWindowParams;
            params.gravity = Gravity.TOP;
            params.alpha = 0.65f;
            params.x = 0;
            params.y = y - mDragPointOffset + mCoordOffset;

            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            params.format = PixelFormat.TRANSLUCENT;
            params.windowAnimations = 0;
            
            ImageView imageView = new ImageView(context);
            int backGroundColor = context.getResources().getColor(android.R.color.black);
            imageView.setBackgroundColor(backGroundColor);
            imageView.setImageBitmap(bitmap);

            // add view
            mWindowManager = (WindowManager) context.getSystemService("window");
            mWindowManager.addView(imageView, params);
            mImageView = imageView;
            
		}

		int getRowHeight() {
			return mRowHeight;
		}
		
        void move(int x, int y) {
            mWindowParams.y = y - mDragPointOffset + mCoordOffset;
            mWindowManager.updateViewLayout(mImageView, mWindowParams);
        }
		
		void cleanup() {
			mWindowManager.removeView(mImageView);
			mImageView.setVisibility(View.INVISIBLE); // hidden window won't draw thus we can recycle image
			mImageView = null;
			if (mBitmap != null) {
				mBitmap.recycle();
				mBitmap = null;
				//Log.d("Dragger", "bitmap recycled");
			}
		}
		
		Setting getSetting() {
			return mSetting;
		}
		
		int getMiddleY(int y) {
			return y - mDragPointOffset + (mRowHeight / 2);
		}
	}

	//private static final String TAG = "SortableListView";
	private static final Setting SETTING_PLACEHOLDER = new Setting(Setting.PLACEHOLDER, R.string.txt_status_unknown);
	
	// state
	private Dragger mDragger;
	private int mCurrentPos;
	private LayoutSettingsAdapter mAdapter;

	private int mBigStep;
	private int mSmallStep;

	public SortableSettingsListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		float scale = context.getResources().getDisplayMetrics().density;
		mBigStep = (int) (scale * 20);
		mSmallStep = (int) (scale * 8);
	}

	public boolean onInterceptTouchEvent(MotionEvent ev) {
		
        switch (ev.getAction()) {
        	case MotionEvent.ACTION_DOWN:
        		
        		// get item in the list
	        	final int x = (int) ev.getX();
	            final int y = (int) ev.getY();
	            int pos = pointToPosition(x, y); // absolute position in the list
                if (pos == AdapterView.INVALID_POSITION) break; // we clicked on delimiter
                
                // check if we should drag
                final ViewGroup item = (ViewGroup) getChildAt(pos - getFirstVisiblePosition());
                View draggerIcon = item.findViewById(R.id.icon);
                if (draggerIcon == null) break; // this item does not have a dragger icon
                if (x < draggerIcon.getLeft() - 8) break; // user clicked left of the dragger
                
                LayoutSettingsAdapter adapter = mAdapter;
                if (adapter == null) {
                	adapter = (LayoutSettingsAdapter) getAdapter(); // cache adapter
                	mAdapter = adapter;
                }
                
                // create dragger and start dragging
                Setting setting = (Setting) adapter.getItem(pos);
                mDragger = new Dragger(getContext(), setting, item, ev);
                
                // replace with placeholder
                adapter.setItem(pos, SETTING_PLACEHOLDER);
                mCurrentPos = pos;
                
                // update view
                adapter.getView(pos, item, null);
                
                // vibrate 
                Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null) vibrator.vibrate(30);
                
                return false;
        }
        return super.onInterceptTouchEvent(ev);
	}

	/**
	 * Swap current setting with a setting under given position
	 * @param pos
	 */
	private void swapSettings(int pos) {
		
		LayoutSettingsAdapter adapter = mAdapter;
		int cur = mCurrentPos;
		
    	// swap settings
    	Setting setting = (Setting) adapter.getItem(pos); // new setting
    	//Log.d(TAG, "over setting: " + setting.getId());

    	adapter.setItem(pos, SETTING_PLACEHOLDER);
    	adapter.setItem(cur, setting);

    	int firstPos = getFirstVisiblePosition();
    	
    	// update views
    	adapter.getView(pos, getChildAt(pos - firstPos), null);
    	adapter.getView(cur, getChildAt(cur - firstPos), null);
    	
    	mCurrentPos = pos;
	}
	
	public boolean onTouchEvent(MotionEvent ev) {
		
		Dragger dragger = mDragger;
		
		if (dragger != null) {
			
			// we should handle this event because dragger was created
			int action = ev.getAction();
			switch(action) {
			
	            case MotionEvent.ACTION_DOWN:
	            case MotionEvent.ACTION_MOVE:
	            	
                    final int x = (int) ev.getX();
                    final int y = (int) ev.getY();
	            	
                    // move dragger
                    dragger.move(x, y);
                    
                    // check if we have to switch elements
    	            int pos = pointToPosition(x, dragger.getMiddleY(y)); // absolute position in the list
                    if (pos == AdapterView.INVALID_POSITION) break; // we move over a delimiter                    
                    
                    // do not allow to put over very first item
                    if (pos == 0) pos = 1;

                    if (pos != mCurrentPos) {
                    	// we have to go through all item is they are not direct siblings
                    	int step = pos - mCurrentPos > 0 ? 1 : -1;
                    	int tmpPos = mCurrentPos;
                    	while(true) {
                    		tmpPos += step;
                    		swapSettings(tmpPos);
                    		//Log.d(TAG, "moving:" + tmpPos);
                    		if (tmpPos == pos) break; // exit
                    	}
                    	
                    }
                    
                    // --------- scroll view ---------
                    final int height = getHeight();
                    final int border = (int) (height / 3.5f);
                    final int border2 = border / 2;
                    int speed = 0;
                    if (y < border) {
                    	// scroll up
                    	speed = y < border2 ? -mBigStep : -mSmallStep;
                    } else if (y > height - border) {
                    	// scroll down
                    	speed = y > height - border2 ? mBigStep : mSmallStep;
                    }

                    if (speed != 0) {
                    	int ref = pointToPosition(x, y);
                        View v = getChildAt(ref - getFirstVisiblePosition());
                        if (v != null) {
                            pos = v.getTop();
                            setSelectionFromTop(ref, pos - speed);
                        }                    	
                    }
                    
	            	break;
            
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                	
                	// drop dragger
                	Setting setting = dragger.getSetting();

                	// update view
                	mAdapter.setItem(mCurrentPos, setting);
                	//mAdapter.notifyDataSetChanged();
                	
                	// update view
                	mAdapter.getView(mCurrentPos, getChildAt(mCurrentPos - getFirstVisiblePosition()), null);
                	
                	mCurrentPos = 0;

                	// remove dragger
                	dragger.cleanup();
                	mDragger = null;
                	
                	// show APN control warning when needed
                	if (Constants.SDK_VERSION < 10 /*2.3.3*/ 
                			&& (setting.id == Setting.MOBILE_DATA_APN || setting.id == Setting.MOBILE_DATA)
                			&& mAdapter.isInVisibleInList(setting)) {
                		Toast.makeText(getContext(), R.string.msg_use_mobile_data_hint, Toast.LENGTH_LONG).show();
                	}
                	
                	break;
			}
			
			return true;
		}
		
		return super.onTouchEvent(ev);
	}
	
}
