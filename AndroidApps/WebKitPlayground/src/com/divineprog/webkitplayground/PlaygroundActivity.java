package com.divineprog.webkitplayground;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;

public class PlaygroundActivity extends InputActivity
{
	JavaScriptWebView mWebView;
    DrawingSurface mDrawingSurface;
	JavaScriptTestInterface mJavaScriptTestInterface;
    JavaScriptSurfaceInterface mJavaScriptSurfaceInterface;
    JavaScriptInputListener mJavaScriptInputListener;
    NativeDrawingTestInputListener mNativeDrawingTestInputListener;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Create widgets.
		mWebView = new JavaScriptWebView(this);
		mDrawingSurface = new DrawingSurface(this);

		// Set up JS interfaces.
		mJavaScriptTestInterface = new JavaScriptTestInterface(this);
        mJavaScriptSurfaceInterface = new JavaScriptSurfaceInterface(
            mWebView,
            mDrawingSurface);
        mWebView.addJavascriptInterface(mJavaScriptTestInterface, "Test");
        mWebView.addJavascriptInterface(mJavaScriptSurfaceInterface, "Surface");

        // Create input listeners.
        mJavaScriptInputListener = new JavaScriptInputListener(mWebView);
        mNativeDrawingTestInputListener = new NativeDrawingTestInputListener(this);

		// Set content of WebView.
		mWebView.loadUrl("file:///android_asset/index.html");

		// Show WebView.
		setContentView(mWebView);
	}
/*
	void logScreenSize()
	{
	    DisplayMetrics dm = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(dm);
        Log.i("@@@","Screen width : " + (dm.widthPixels/dm.xdpi*25.6) + " mm");
        Log.i("@@@","Screen height : " + (dm.heightPixels/dm.ydpi*25.6) + " mm");
	}
*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public static class NativeDrawingTestInputListener extends Input.Adapter
	{
	    PlaygroundActivity mActivity;
	    int mCounter;
	    long mTimeStamp;

	    public NativeDrawingTestInputListener(PlaygroundActivity activity)
	    {
	        mActivity = activity;
	    }

	    void callJS(String js)
	    {
	        mActivity.mWebView.callJS(js);
	    }

	    @Override
        public void onTouchDown(int x, int y, int id)
	    {
	        mCounter = 0;
	        mTimeStamp = System.currentTimeMillis();
	    }

        @Override
        public void onTouchDrag(int x, int y, int id)
        {
            ++ mCounter;
            // 100 is same number as NumDrags in index.html
            if (mCounter == 100)
            {
                mTimeStamp = System.currentTimeMillis() - mTimeStamp;
                callJS("AndroidCanvasNativeTestCallback(" + mTimeStamp + ")");
            }
            else
            {
                JavaScriptSurfaceInterface surfaceInterface =
                    mActivity.mJavaScriptSurfaceInterface;
                surfaceInterface.setColor(200, 0, 0);
                surfaceInterface.fillRect(x - 30, y - 30, 60, 60);
                surfaceInterface.updateScreen();
            }
        }
	}
}
