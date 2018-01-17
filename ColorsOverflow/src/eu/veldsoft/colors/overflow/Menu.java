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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * This is the main menu of the game.
 * 
 * @author Yuriy Stanchev
 * 
 * @email i_stanchev@ml1.net
 * 
 * @date 11 Mar 2012
 */
public class Menu extends Activity implements OnClickListener {

	/**
	 * Name of the AI database file holding the Normal AI database table and
	 * Hard AI database table.
	 */
	private static final String DATABASE_NAME = "ai.db";

	/**
	 * Path to the AI database file.
	 */
	private final String DATABASE_PATH = "/data/data/com.netsecl.stanchev/databases/";

	/**
	 * Full path to the AI database file.
	 */
	private final String path_DB = DATABASE_PATH + DATABASE_NAME;

	/**
	 * Shared preferences of the game.
	 */
	public static final String PREFS_NAME = "GameSettings";

	/**
	 * Private method used to copy the database file for the Normal and Hard AI.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	private void databaseCopy() {
		InputStream copy;
		try {
			/*
			 * Get the file from the assets folder.
			 */
			copy = getAssets().open(DATABASE_NAME);
			/*
			 * Open an empty output stream.
			 */
			OutputStream dbOut = new FileOutputStream(path_DB);
			/*
			 * Copy the file.
			 */
			byte[] buffer = new byte[1024];
			int length;
			while ((length = copy.read(buffer)) > 0) {
				dbOut.write(buffer, 0, length);
			}
			/*
			 * Close all streams.
			 */
			dbOut.flush();
			dbOut.close();
			copy.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            It is used to save the state of the created Activity.
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
		setContentView(R.layout.main);

		/*
		 * Find all the buttons and set their listeners.
		 */
		Button newGameButton = (Button) findViewById(R.id.bNew);

		Button exitButton = (Button) findViewById(R.id.bExit);

		Button aboutButton = (Button) findViewById(R.id.bAbout);

		Button settingsButton = (Button) findViewById(R.id.bSettings);

		Button rulesButton = (Button) findViewById(R.id.bGameRules);

		Button highscoreButton = (Button) findViewById(R.id.bHighscore);

		/*
		 * Set the listeners of the buttons.
		 */
		newGameButton.setOnClickListener(this);

		exitButton.setOnClickListener(this);

		aboutButton.setOnClickListener(this);

		settingsButton.setOnClickListener(this);

		rulesButton.setOnClickListener(this);

		highscoreButton.setOnClickListener(this);
		/*
		 * Used to create the database if it doesn't exist on the SD Card.
		 */
		NormalAISQLAdapter sqliteNormalAiHandle = new NormalAISQLAdapter(this);
		sqliteNormalAiHandle.openToRead();
		sqliteNormalAiHandle.close();
		/*
		 * Used to copy the database once it is initialized.
		 */
		databaseCopy();

	}

	/**
	 * Defines the actions for the menu buttons.
	 * 
	 * @param view
	 *            GUI visual control.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	public void onClick(View view) {
		/*
		 * On click actions for all the buttons on the menu.
		 */
		switch (view.getId()) {

		case R.id.bExit:
			Intent EndSplash = new Intent(getApplicationContext(),
					EndSplash.class);
			startActivity(EndSplash);
			finish();
			break;

		case R.id.bAbout:
			Intent about = new Intent(getApplicationContext(),
					AboutActivity.class);
			String title = "About Overflow";
			String file = "file:///android_asset/about.html";
			about.putExtra("title", title);
			about.putExtra("file", file);
			startActivity(about);
			break;

		case R.id.bGameRules:
			Intent aboutrules = new Intent(getApplicationContext(),
					AboutActivity.class);
			String titlerules = "Game Rules";
			String filerules = "file:///android_asset/rules.html";
			aboutrules.putExtra("title", titlerules);
			aboutrules.putExtra("file", filerules);
			startActivity(aboutrules);
			break;

		case R.id.bNew:
			Intent game = new Intent(Menu.this, OverflowActivity.class);
			Board board = new Board();
			game.putExtra("board", board);
			startActivity(game);
			break;

		case R.id.bSettings:
			Intent settings = new Intent(this, SettingsActivity.class);
			startActivity(settings);
			break;

		case R.id.bHighscore:
			Intent highscore = new Intent(Menu.this, Highscore.class);
			startActivity(highscore);
			break;
		}
	}
}
