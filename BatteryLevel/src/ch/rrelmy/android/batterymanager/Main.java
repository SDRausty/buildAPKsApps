package ch.rrelmy.android.batterymanager;

import java.util.regex.Pattern;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.BatteryManager;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Main extends Activity {
	
	protected LinearLayout mLayout;
	protected BatteryLevelView mBatteryLevelView;
	
	protected BroadcastReceiver batteryLevelReceiver;
	protected IntentFilter batteryLevelFilter;
	
	final static int MENU_CLOSE = 1;
	final static int MENU_ABOUT = 2;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mBatteryLevelView = new BatteryLevelView(this);
        mBatteryLevelView.setOnClickListener(mClickListener);
        
        setContentView(mBatteryLevelView);
        
        batteryLevelUpdater();
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	this.unregisterReceiver(batteryLevelReceiver);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	registerReceiver(batteryLevelReceiver, batteryLevelFilter);
    }
    
    // touch
	private OnClickListener mClickListener = new OnClickListener() {
	    public void onClick(View v) {
	    	finish();
	    }
	};
    
    private void batteryLevelUpdater()
    {
        batteryLevelReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                //context.unregisterReceiver(this);
                int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int level = -1;
                if (rawlevel >= 0 && scale > 0) {
                    level = (rawlevel * 100) / scale;
                }
                mBatteryLevelView.setLevel(level);
            }
        };
        batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, batteryLevelFilter);
    }
    
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	menu.add(0, MENU_CLOSE, 0, "Close");
    	menu.add(0, MENU_ABOUT, 0, "About");
    	
    	boolean result = super.onCreateOptionsMenu(menu);
    	return result;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
	        case MENU_CLOSE:
	            finish();
	            return true;
	        case MENU_ABOUT:
	            showAboutDialog();
	            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    public void showAboutDialog()
    {
    	Dialog dialog = new Dialog(this);
    	/*dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
    			WindowManager.LayoutParams.FLAG_BLUR_BEHIND);*/
    	
    	LinearLayout mDialogLayout = new LinearLayout(this);

    	TextView mTextView = new TextView(this);
    	mTextView.setPadding(20, 0, 20, 20);
    	mTextView.setLineSpacing(8, 1);
    	mTextView.setText(Html.fromHtml(
    			"<p>" +
    			"Author: Rémy Böhler<br />" +
    			"E-Mail: remyboehler@gmail.com" +
    			"</p>" +
    			
    			"Version: 0.2<br />" +
    			"License: GPLv3<br />" + 
    			"Source: github.com/rrelmy/BatteryLevel"
    	));
    	// TODO better way for that?
    	// link mail
    	Pattern patternMail = Pattern.compile("remyboehler@gmail.com");
    	Linkify.addLinks(mTextView, patternMail, "mailto://");
    	// link source link
    	Pattern patternLink = Pattern.compile("github.com/rrelmy/BatteryLevel");
    	Linkify.addLinks(mTextView, patternLink, "https://");
    	
    	mDialogLayout.addView(mTextView);
    	
    	dialog.setContentView(mDialogLayout);
    	dialog.setTitle("About");
    	dialog.show();
    }
    
    private class BatteryLevelView extends View {    	
    	private int level = -1;
    	
    	private Paint paintCanvas;
    	private Paint paintTextLevel;

    	public BatteryLevelView(Context context) 
    	{
    		super(context);

    		// canvas paint
    		paintCanvas = new Paint();
			paintCanvas.setStyle(Paint.Style.FILL);
			paintCanvas.setColor(Color.BLACK);
			paintCanvas.setAlpha(128);
			
    		// text paint
    		paintTextLevel = new Paint();
    		paintTextLevel.setAntiAlias(true);
    		paintTextLevel.setFakeBoldText(true);
    		paintTextLevel.setTextSize(150);
    		paintTextLevel.setTextAlign(Align.CENTER);
    		paintTextLevel.setShadowLayer(5, 0, 0, Color.BLACK);
    		paintTextLevel.setColor(Color.WHITE);
    	}
    	
    	public void setLevel(int level)
    	{
    		this.level = level;
    		invalidate();
    	}
    	
    	@Override
    	protected void onDraw(Canvas canvas)
    	{
    		super.onDraw(canvas);
    		
    		// background
			canvas.drawPaint(paintCanvas);

			// TextSize
			paintTextLevel.setTextSize(this.getWidth() / 3 | 1);
			
    		// draw text
			canvas.drawText(level + "%", this.getWidth() / 2, this.getHeight() / 2, paintTextLevel);

    	}
    }
}