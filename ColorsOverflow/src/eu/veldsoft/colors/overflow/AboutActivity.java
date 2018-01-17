/*==============================================================================
 =                                                                             =
 = Overflow is very simple but very addictive board game. The game is for two  =
 = players who try to conquer all stones of the opposite player. The game was  =
 = developed as master thesis in New Bulgarian University, Sofia, Bulgaria.    =
 =                                                                             =
 = Copyright (C) 2012 by Yuriy Stanchev  ( i_stanchev@ml1.net )                =
 =                                                                             =
 = This program is free software: you can redistribute it and/or modify        =
 = it under the terms of the GNU General Public License as published by        =
 = the Free Software Foundation, either version 3 of the License, or           =
 = (at your option) any later version.                                         =
 =                                                                             =
 = This program is distributed in the hope that it will be useful,             =
 = but WITHOUT ANY WARRANTY; without even the implied warranty of              =
 = MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               =
 = GNU General Public License for more details.                                =
 =                                                                             =
 = You should have received a copy of the GNU General Public License           =
 = along with this program. If not, see <http://www.gnu.org/licenses/>.        =
 =                                                                             =
 =============================================================================*/

package eu.veldsoft.colors.overflow;

import android.os.Bundle;
import android.view.View;
import android.app.Activity;
import android.webkit.WebView;
import android.content.Intent;

/**
 * This class is used to create a WebView so we can display the HTML pages for
 * the game rules and info about the author.
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
		// Initialize the databases for the AIs.
		// Intent AIDBInitialization = new Intent(getApplicationContext(),
		// AIDBInitialization.class);
		// startActivity(AIDBInitialization);
	}
}
