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

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

/**
 * This class is used to display the end splash screen.
 * 
 * @author Yuriy Stanchev
 * 
 * @email i_stanchev@ml1.net
 * 
 * @date 22 Apr 2012
 */
public class EndSplash extends Activity {

	/**
	 * Splash display time.
	 */
	private int splashTime = 14000;

	/**
	 * Splash thread.
	 */
	private Thread splashThread;

	/**
	 * Called when the activity is first created. Creates the splash screen and
	 * displays it for the defined splash time.
	 * 
	 * @param savedInstanceState
	 *            is used to save the state of the created Activity.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 22 Apr 2012
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		/*
		 * Display the end splash screen inside a webview.
		 */
		setContentView(R.layout.about);
		setTitle("Credits");
		WebView view = (WebView) findViewById(R.id.web);
		view.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		view.loadUrl("file:///android_asset/credits.html");

		/*
		 * Start the thread for displaying the splash screen.
		 */
		splashThread = new Thread() {
			@Override
			public void run() {
				try {
					synchronized (this) {
						/*
						 * Wait for a moment so the user can see the splash.
						 */
						wait(splashTime);
						interrupt();
					}

				} catch (InterruptedException e) {
				} finally {
					finish();
				}
			}
		};

		/*
		 * Start the thread.
		 */
		splashThread.start();
	}

	/**
	 * Used to stop the splash screen, when user interaction begins - if the
	 * user taps on the screen before the end of the thread.
	 * 
	 * @param event
	 *            used to handle the touch event.
	 * 
	 * @return returns if a touch event has occurred.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 22 Apr 2012
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			synchronized (splashThread) {
				splashThread.notifyAll();
			}
		}

		return (true);
	}
}
