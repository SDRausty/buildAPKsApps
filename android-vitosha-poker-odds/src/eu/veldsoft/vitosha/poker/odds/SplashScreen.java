package eu.veldsoft.vitosha.poker.odds;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

/**
 * This class is used to display the splash screen.
 * 
 * @author Yuriy Stanchev
 * 
 * @email i_stanchev@ml1.net
 * 
 * @date 11 Mar 2012
 */
public class SplashScreen extends Activity {

	/**
	 * Splash display time.
	 */
	private int splashTime = 5000;

	/**
	 * Splash thread.
	 */
	private Thread splashThread;

	/**
	 * Called when the activity is first created. Creates the splash screen and
	 * displays it for the defined splash time.
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

		/*
		 * Display the splash.
		 */
		setContentView(R.layout.splash);
		final SplashScreen sPlashScreen = this;

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
					Intent intent = new Intent();
					intent.setClass(sPlashScreen, VitoshaPokerOddsActivity.class);
					startActivity(intent);
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
	 *            Used to handle the touch event.
	 * 
	 * @return Returns true if a touch event has occurred and false otherwise.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
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
