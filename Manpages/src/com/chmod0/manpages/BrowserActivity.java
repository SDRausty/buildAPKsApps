package com.chmod0.manpages;

import java.net.URL;
import com.chmod0.manpages.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Class that loads the html content of a manpage
 * For now, it just goes to julien.guepin.fr to download the page
 * TODO...
 * In the future, it will download an archive with all pages, an store it on the sd card
 * It will then be possible to load man pages locally, from the sd card
 * 
 * @author chmod0
 *
 */
public class BrowserActivity extends Activity {

	private WebView webview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Ask rights  to display the progress bar
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		
		setContentView(R.layout.pages_browser);
		// Get the manpage to be open from the Intent extras
		final Page manpage = (Page)((Bundle)getIntent().getExtras().get("manpage")).get("manpage");
		// Url pointing to the right manpage
		String manPageUrl ="htmlman/htmlman" + manpage.getSection() + "/" + manpage.getName() + "." + manpage.getSection() + ".html";
		String baseUrl;

		webview = (WebView)findViewById(R.id.webview);
		// Set a new webview client to manage the opening of links
		webview.setWebViewClient(new MyWebViewClient());
		// Enable javascript on manpages
		webview.getSettings().setJavaScriptEnabled(true);		
		
		final Activity activity = this;
		
		// Customize the webview to display a progress bar on top of the activity
		webview.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				activity.setTitle("Loading...");
				activity.setProgress(progress * 100);
				if(progress == 100)
					activity.setTitle("Manpages");
			}
		});
		
		// Check if the page file exists on the external storage
		if( ! ManualActivity.checkPageFilesOnExternalStorage(manPageUrl)) {
			baseUrl = "http://julien.guepin.fr/android/manpages/";
			Log.d("MANPAGES", "No file on ext storage");
		} else {
			baseUrl = "file://" + Environment.getExternalStorageDirectory() + "/Android/data/com.chmod0.manpages/";
			Log.d("MANPAGES", "File present on ext storage");
		}
		webview.loadUrl(baseUrl + manPageUrl);

	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    // Check if the key event was the BACK key and if there's history
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
	        webview.goBack();
	        return true;
	    }
	    // If it wasn't the BACK key or there's no web page history, bubble up to the default
	    // system behavior (probably exit the activity)
	    return super.onKeyDown(keyCode, event);
	}
	
	/**
	 * Create the options menu of the activity
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(getApplication()).inflate(R.menu.options_browser, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Actions to do when the options menu is clicked
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		LayoutInflater factory = getLayoutInflater();
		switch(item.getItemId()){
		case R.id.about_menu:
			AlertDialog.Builder aboutDialog = new AlertDialog.Builder(this);
			aboutDialog.setTitle("About...");
			aboutDialog.setIcon(R.drawable.ic_launcher);
			aboutDialog.setView(factory.inflate(R.layout.about_popup, null,
					false));
			aboutDialog.setPositiveButton("Ok", null);
			aboutDialog.show();
			break;
		case R.id.section_menu:
			AlertDialog.Builder sectionsDialog = new AlertDialog.Builder(this);
			sectionsDialog.setTitle("Sections");
			sectionsDialog.setView(factory.inflate(R.layout.sections_popup, null, false));
			sectionsDialog.setPositiveButton("Ok", null);
			sectionsDialog.show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * This is a class that handle the notifications of the webview
	 * I want the manpages url to open within the webview, and external urls to open in the stock android browser.
	 * @author chmod0
	 */
	private class MyWebViewClient extends WebViewClient{
		
		public boolean shouldOverrideUrlLoading(WebView view, String url){
			try{
				URL urlObj = new URL(url);
				if(urlObj.getHost().equals("julien.guepin.fr")){
					//Allow the WebView to do its thing
			        return false;
				} else{
					Intent intent = new Intent(Intent.ACTION_VIEW);
					Uri request = Uri.parse(url);
			        intent.setData(request);
			        // Let Android start its stock browser
			        startActivity(intent);
					return true;
				}
			} catch(Exception e){
				e.printStackTrace();
				return false;
			}
		}
	}
}
