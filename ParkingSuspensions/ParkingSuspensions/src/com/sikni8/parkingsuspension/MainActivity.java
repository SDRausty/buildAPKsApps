package com.sikni8.parkingsuspension;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {
    
	Button btnMenu, btnLogo;
	DateFormat df = new SimpleDateFormat("EEEEE, LLLL d", Locale.US);
	String[] suspendedDates = {
			"Tuesday, January 1",
			"Monday, January 21",
			"Sunday, February 10",
			"Tuesday, February 12",
			"Wednesday, February 13",
			"Monday, February 18",
			"Sunday, February 24",
			"Tuesday, March 26",
			"Wednesday, March 27",
			"Thursday, March 28",
			"Friday, March 29",
			"Monday, April 1",
			"Tuesday, April 2",
			"Thursday, May 2",
			"Friday, May 3",
			"Thursday, May 9",
			"Wednesday, May 15",
			"Thursday, May 16",
			"Monday, May 27",
			"Thursday, July 4",
			"Wednesday, August 7",
			"Thursday, August 8",
			"Friday, August 9",
			"Thursday, August 15",
			"Monday, September 2",
			"Thursday, September 5",
			"Friday, September 6",
			"Saturday, September 14",
			"Thursday, September 19",
			"Friday, September 20",
			"Thursday, September 26",
			"Friday, September 27",
			"Monday, October 14",
			"Tuesday, October 15",
			"Wednesday, October 16",
			"Thursday, October 17",
			"Friday, November 1",
			"Sunday, November 3",
			"Tuesday, November 5",
			"Monday, November 11",
			"Thursday, November 28",
			"Sunday, December 8",
			"Wednesday, December 25"
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        if (Arrays.asList(suspendedDates).contains(df.format(Calendar.getInstance(Locale.US).getTime()))) {
        	int index = Arrays.asList(suspendedDates).indexOf(df.format(Calendar.getInstance(Locale.US).getTime()));
        	
        	int k = MainActivity.this.getResources().getIdentifier("ivIcon" + index, "id", MainActivity.this.getPackageName());
        	
        	ImageView view = (ImageView) findViewById(k);
        	
        	view.setImageResource(R.drawable.caliconpressed);
        }
        
        //Checks to see if it's portrait or landscape
		//int k = MainActivity.this.getResources().getConfiguration().orientation;
		//Toast.makeText(getApplicationContext(), String.valueOf(k), 2000).show();
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
	
	@Override
	public void onAttachedToWindow() { //ensures smooth gradient
	    super.onAttachedToWindow();
	    Window window = getWindow();
	    window.setFormat(PixelFormat.RGBA_8888);
	}
	public void onDestory() {
		super.onDestroy();
        unbindDrawables(findViewById(R.id.svDates));
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
    	    intent.setData(Uri.parse("market://details?id=com.sikni8.parkingsuspension"));
    	    if (MyStartActivity(intent) == false) {
    	        //Market (Google play) app seems not installed, let's try to open a web browser
    	        intent.setData(Uri.parse("https://play.google.com/store/apps/details?com.sikni8.parkingsuspension"));
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
}