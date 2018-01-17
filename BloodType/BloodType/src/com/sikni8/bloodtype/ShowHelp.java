package com.sikni8.bloodtype;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

public class ShowHelp extends Activity {
	
	protected Button bBack;
	ImageView imageView;
	Animation exitAnimation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showhelp);
		
		imageView = (ImageView) findViewById(R.id.ivPlus);
		//exitAnimation = AnimationUtils.loadAnimation(this, R.anim.animateit);
		//imageView.startAnimation(exitAnimation);
		
		final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(this, R.anim.overshoot);
		imageView.startAnimation(animAnticipateOvershoot);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
    
	@Override
	public void onBackPressed() {
	    /// do something on back.
        finish();
        overridePendingTransition (R.anim.right_slide_in, R.anim.right_slide_out);
	    return;
	}
	@Override
	public void onResume() {
		super.onResume();
		Log.v("RESUME", "ACTIVITY RESUMED");
	}
	@Override
	public void onAttachedToWindow() { //ensures smooth gradient
	    super.onAttachedToWindow();
	    Window window = getWindow();
	    window.setFormat(PixelFormat.RGBA_8888);
	}
	public void onDestory() {
		super.onDestroy();
        unbindDrawables(findViewById(R.id.llHelp));
        System.gc();
	}
    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home: 
				onBackPressed();
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}
}