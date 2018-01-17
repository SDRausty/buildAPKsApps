package com.kindborg.webviewbasic;

import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity
{
	WebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mWebView = new WebView(this);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setWebViewClient(new WebViewClient());
		mWebView.addJavascriptInterface(this, "Android");
		mWebView.loadUrl("file:///android_asset/index.html");
		setContentView(mWebView);
	}

    @JavascriptInterface
    public void showToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void vibrate(long milliseconds)
    {
	    ((Vibrator)
	    	getSystemService(Context.VIBRATOR_SERVICE))
	    		.vibrate(milliseconds);
    }
}
