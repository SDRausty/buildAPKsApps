package com.divineprog.webkitplayground;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

/**
 * JavaScript enabled WebView.
 * @author miki
 */
public class JavaScriptWebView extends WebView
{
    Context mContext;

	@SuppressLint("SetJavaScriptEnabled")
	public JavaScriptWebView(Context context)
	{
		super(context);

		mContext = context;

		WebSettings settings = getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setDomStorageEnabled(true);
		settings.setGeolocationEnabled(true);
		setVerticalScrollbarOverlay(true);

		setWebChromeClient(new MyChromeClient());

		try {
		    new JavaScriptWebViewNewSettings().applySettings(this); }
		catch (java.lang.Throwable e) {}
	}

    public void callJS(final String js)
    {
        final WebView webView = this;
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:" + js);
            }
        });
    }

	class MyChromeClient extends WebChromeClient
	{
		@Override
		public boolean onJsAlert(
			WebView view,
			String url,
			String message,
			JsResult result)
		{
		    // Display alert as a Toast.
			Toast.makeText(
				view.getContext(),
				message,
				Toast.LENGTH_SHORT).show();
			result.confirm();
			return true;
		}

		@Override
		public boolean onJsPrompt(
			WebView view,
			String url,
			String message,
			String defaultValue,
			JsPromptResult result)
		{
		    // Need to set a result.
			result.confirm("ok");

			// Hard-coded to test performance of prompt.
			callJS("PromptCallback()");

			return true;
		}

		@Override
		public void onGeolocationPermissionsShowPrompt(
		    String origin,
		    Callback callback)
		{
		    super.onGeolocationPermissionsShowPrompt(origin, callback);
		    callback.invoke(origin, true, false);
		}
	}
}
