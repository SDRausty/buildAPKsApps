package com.sikni8.bloodtype;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Typeface;

public class MainActivity extends Activity {
	
	protected Button bA, bB, bC, bD, bE, bF, bG, bH;
	protected Animation animAlpha;
	protected Button btnMenu, btnLogo;
	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;
	private TextView tvInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		bA = (Button) findViewById(R.id.btnA);
		bA.setOnClickListener(pressA);
		
		bB = (Button) findViewById(R.id.btnB);
		bB.setOnClickListener(pressB);
		
		bC = (Button) findViewById(R.id.btnC);
		bC.setOnClickListener(pressC);
		
		bD = (Button) findViewById(R.id.btnD);
		bD.setOnClickListener(pressD);
		
		bE = (Button) findViewById(R.id.btnE);
		bE.setOnClickListener(pressE);
		
		bF = (Button) findViewById(R.id.btnF);
		bF.setOnClickListener(pressF);
		
		bG = (Button) findViewById(R.id.btnG);
		bG.setOnClickListener(pressG);
		
		bH = (Button) findViewById(R.id.btnH);
		bH.setOnClickListener(pressH);
		
		animAlpha = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alphabtn);
		
		tvInfo = (TextView) findViewById(R.id.textView1);
		
		tvInfo.setTypeface(Typeface.createFromAsset(MainActivity.this.getAssets(), "fonts/gbold.otf"));
		
		// Set up our Prefs and Editor
		prefs = this.getSharedPreferences("BloodType", Context.MODE_PRIVATE);
		editor = prefs.edit();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
	    
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_quit:
			moveTaskToBack(true);
			return true;
		case R.id.action_help:
			//display the help activity
			Intent myIntent2 = new Intent(getApplicationContext(), ShowHelp.class);
	        startActivityForResult(myIntent2, 0);
	        overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
			return true;
		case R.id.action_table:
			//display the help activity
			Intent myIntent3 = new Intent(getApplicationContext(), BloodTable.class);
	        startActivityForResult(myIntent3, 0);
	        overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
			return true;
		case R.id.action_about:
			//display the about window
			Log.i("Opening About Activity", "ABOUT");
	        Intent myIntent = new Intent(MainActivity.this, AboutApp.class);
	        startActivityForResult(myIntent, 0);
	    	overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
			return true;
		case R.id.action_rate:
			//Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
    	    Intent intent = new Intent(Intent.ACTION_VIEW);
    	    //Try Google play
    	    intent.setData(Uri.parse("market://details?id=com.sikni8.bloodtype"));
    	    if (MyStartActivity(intent) == false) {
    	        //Market (Google play) app seems not installed, let's try to open a web browser
    	        intent.setData(Uri.parse("https://play.google.com/store/apps/details?com.sikni8.bloodtype"));
    	        if (MyStartActivity(intent) == false) {
    	            //Well if this also fails, we have run out of options, inform the user.
    	            //let the user know nothing was successful
    	        }
    	    }
    	    return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private boolean MyStartActivity(Intent aIntent) {
	    try
	    {
	        startActivity(aIntent);
	        return true;
	    }
	    catch (ActivityNotFoundException e)
	    {
	        return false;
	    }
	}
	
	View.OnClickListener pressA = new View.OnClickListener() {
		public void onClick(final View v) {
			animAlpha.setAnimationListener(new AnimationListener() { //changed to fade4
	            public void onAnimationStart(Animation animation) {}
	            public void onAnimationRepeat(Animation animation) {}
	            public void onAnimationEnd(Animation animation) {
	            	editor.putString("BloodVal", "0");
					editor.commit();
	    			Intent myIntent = new Intent(MainActivity.this, BloodType.class);
	    			startActivityForResult(myIntent, 0);
	    			overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
	            }
	        });
			v.startAnimation(animAlpha);
		}
	};
	View.OnClickListener pressB = new View.OnClickListener() {
		public void onClick(final View v) {
			animAlpha.setAnimationListener(new AnimationListener() { //changed to fade4
	            public void onAnimationStart(Animation animation) {}
	            public void onAnimationRepeat(Animation animation) {}
	            public void onAnimationEnd(Animation animation) {
	            	editor.putString("BloodVal", "1");
					editor.commit();
	    			Intent myIntent = new Intent(MainActivity.this, BloodType.class);
	    			startActivityForResult(myIntent, 0);
	    			overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
	            }
	        });
			v.startAnimation(animAlpha);
		}
	};
	View.OnClickListener pressC = new View.OnClickListener() {
		public void onClick(final View v) {
			animAlpha.setAnimationListener(new AnimationListener() { //changed to fade4
	            public void onAnimationStart(Animation animation) {}
	            public void onAnimationRepeat(Animation animation) {}
	            public void onAnimationEnd(Animation animation) {
	            	editor.putString("BloodVal", "2");
					editor.commit();
	    			Intent myIntent = new Intent(MainActivity.this, BloodType.class);
	    			startActivityForResult(myIntent, 0);
	    			overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
	            }
	        });
			v.startAnimation(animAlpha);
		}
	};
	View.OnClickListener pressD = new View.OnClickListener() {
		public void onClick(final View v) {
			animAlpha.setAnimationListener(new AnimationListener() { //changed to fade4
	            public void onAnimationStart(Animation animation) {}
	            public void onAnimationRepeat(Animation animation) {}
	            public void onAnimationEnd(Animation animation) {
	            	editor.putString("BloodVal", "3");
					editor.commit();
	    			Intent myIntent = new Intent(MainActivity.this, BloodType.class);
	    			startActivityForResult(myIntent, 0);
	    			overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
	            }
	        });
			v.startAnimation(animAlpha);
		}
	};
	View.OnClickListener pressE = new View.OnClickListener() {
		public void onClick(final View v) {
			animAlpha.setAnimationListener(new AnimationListener() { //changed to fade4
	            public void onAnimationStart(Animation animation) {}
	            public void onAnimationRepeat(Animation animation) {}
	            public void onAnimationEnd(Animation animation) {
	            	editor.putString("BloodVal", "4");
					editor.commit();
	    			Intent myIntent = new Intent(MainActivity.this, BloodType.class);
	    			startActivityForResult(myIntent, 0);
	    			overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
	            }
	        });
			v.startAnimation(animAlpha);
		}
	};
	View.OnClickListener pressF = new View.OnClickListener() {
		public void onClick(final View v) {
			animAlpha.setAnimationListener(new AnimationListener() { //changed to fade4
	            public void onAnimationStart(Animation animation) {}
	            public void onAnimationRepeat(Animation animation) {}
	            public void onAnimationEnd(Animation animation) {
	            	editor.putString("BloodVal", "5");
					editor.commit();
	    			Intent myIntent = new Intent(MainActivity.this, BloodType.class);
	    			startActivityForResult(myIntent, 0);
	    			overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
	            }
	        });
			v.startAnimation(animAlpha);
		}
	};
	View.OnClickListener pressG = new View.OnClickListener() {
		public void onClick(final View v) {
			animAlpha.setAnimationListener(new AnimationListener() { //changed to fade4
	            public void onAnimationStart(Animation animation) {}
	            public void onAnimationRepeat(Animation animation) {}
	            public void onAnimationEnd(Animation animation) {
	            	editor.putString("BloodVal", "6");
					editor.commit();
	    			Intent myIntent = new Intent(MainActivity.this, BloodType.class);
	    			startActivityForResult(myIntent, 0);
	    			overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
	            }
	        });
			v.startAnimation(animAlpha);
		}
	};
	View.OnClickListener pressH = new View.OnClickListener() {
		public void onClick(final View v) {
			animAlpha.setAnimationListener(new AnimationListener() { //changed to fade4
	            public void onAnimationStart(Animation animation) {}
	            public void onAnimationRepeat(Animation animation) {}
	            public void onAnimationEnd(Animation animation) {
	            	editor.putString("BloodVal", "7");
					editor.commit();
	    			Intent myIntent = new Intent(MainActivity.this, BloodType.class);
	    			startActivityForResult(myIntent, 0);
	    			overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
	            }
	        });
			v.startAnimation(animAlpha);
		}
	};
	@Override
	public void onAttachedToWindow() { //ensures smooth gradient
	    super.onAttachedToWindow();
	    Window window = getWindow();
	    window.setFormat(PixelFormat.RGBA_8888);
	}
	public void onDestory() {
		super.onDestroy();
        unbindDrawables(findViewById(R.id.bloodType));
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
}