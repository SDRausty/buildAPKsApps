package com.sikni8.parkingsuspension;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class AboutApp extends Activity {
	
	TextView tvAbt, tvVers, tvCopy , tvCon;
	Button btnBack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aboutapp);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        tvAbt = (TextView) findViewById(R.id.tvAboutHdr);
        tvVers = (TextView) findViewById(R.id.tvVersion);
        tvCopy = (TextView) findViewById(R.id.tvCopyright);
        tvCon = (TextView) findViewById(R.id.tvContact);
        
        tvAbt.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/highschool.ttf"));
        tvVers.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/highschool.ttf"));
        tvCopy.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/highschool.ttf"));
        tvCon.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/highschool.ttf"));
    }
	
	@Override
	public void onAttachedToWindow() { ///ensures smooth gradient
	    super.onAttachedToWindow();
	    Window window = getWindow();
	    window.setFormat(PixelFormat.RGBA_8888);
	}
	@Override
	public void onBackPressed() {
	    /// do something on back.
        finish();
        overridePendingTransition (R.anim.right_slide_in, R.anim.right_slide_out);
	    return;
	}
	public void onDestory() {
		super.onDestroy();
        unbindDrawables(findViewById(R.id.llAbout));
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