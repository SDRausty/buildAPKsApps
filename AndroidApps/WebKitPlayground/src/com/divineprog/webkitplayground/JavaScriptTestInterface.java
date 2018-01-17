package com.divineprog.webkitplayground;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class JavaScriptTestInterface
{
	PlaygroundActivity mActivity;

	public JavaScriptTestInterface(PlaygroundActivity activity)
	{
	    mActivity = activity;
	}

	void callJS(String js)
	{
	    mActivity.mWebView.callJS(js);
	}

	/**
	 * Handy to have - display toasts from JS.
	 */
	@JavascriptInterface
    public void showToast(String message)
	{
        Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Logging.
     */
    @JavascriptInterface
    public void log(String message)
    {
        Log.i("WebKitPlayground", message);
    }

	/**
	 * Called from JS to test JS <-> Java round trip call.
     * This is used to test performance of two way calls.
	 */
	@JavascriptInterface
	public void twoWayCall()
	{
		callJS("TwoWayCallback()");
	}

    /**
     * Called from JS to test JS -> Java call.
     * Invokes JS callback function when n == trigger.
     * This is used to test performance of one way calls.
     */
    @JavascriptInterface
    public void oneWayCall(int n, int trigger)
    {
        if (n == trigger)
        {
            callJS("OneWayCallback()");
        }
    }

    /**
     * Display the WebView.
     */
    @JavascriptInterface
    public void showWebView()
    {
        mActivity.removeInputListeners();
        mActivity.showView(mActivity.mWebView);
    }

    /**
     * Test performance of touch strokes with events routed through JS.
     */
    @JavascriptInterface
    public void setupJavaScriptDrawingTest()
    {
        mActivity.removeInputListeners();
        mActivity.addInputListener(mActivity.mJavaScriptInputListener);
        mActivity.showView(mActivity.mDrawingSurface);
    }

    /**
     * Test performance of native touch strokes.
     */
    @JavascriptInterface
    public void setupNativeDrawingTest()
    {
        mActivity.removeInputListeners();
        mActivity.addInputListener(mActivity.mNativeDrawingTestInputListener);
        mActivity.showView(mActivity.mDrawingSurface);
    }
}
