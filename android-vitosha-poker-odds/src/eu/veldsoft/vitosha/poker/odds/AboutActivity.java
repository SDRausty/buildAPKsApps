package eu.veldsoft.vitosha.poker.odds;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

/**
 * This class is used to create a WebView so we can display the HTML pages for
 * the about info.
 * 
 * @author Yuriy Stanchev
 * 
 * @email i_stanchev@ml1.net
 * 
 * @date 11 Mar 2012
 */
public class AboutActivity extends Activity {

	/**
	 * Called when the activity is first created. Set the web view layout and
	 * get the title and file to be displayed.
	 * 
	 * @param savedInstanceState
	 *            Is used to save the state of the created Activity.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		String title = (String) extras.get("title");
		String file = (String) extras.get("file");

		setContentView(R.layout.about);

		setTitle(title);

		WebView view = (WebView) findViewById(R.id.web);

		view.setFocusable(true);
		view.setFocusableInTouchMode(true);
		view.requestFocus();
		view.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		view.loadUrl(file);

	}
}
