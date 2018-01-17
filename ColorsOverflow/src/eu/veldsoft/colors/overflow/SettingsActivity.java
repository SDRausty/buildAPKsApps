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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;

/**
 * This class is used to handle the user settings.
 * 
 * @author Yuriy Stanchev
 * 
 * @email i_stanchev@ml1.net
 * 
 * @date 11 Mar 2012
 */
public class SettingsActivity extends Activity implements OnClickListener {

	/**
	 * Used to display the sound setting.
	 */
	private CheckBox sound;

	/**
	 * Used to display the vibration setting.
	 */
	private CheckBox vibration;

	/**
	 * A radio button to enable the Easy AI agains the Human.
	 */
	private RadioButton easy;

	/**
	 * A radio button to enable the Easy AI agains the Human.
	 */
	private RadioButton normal;

	/**
	 * A radio button to enable the Easy AI agains the Human.
	 */
	private RadioButton hard;

	/**
	 * A radio button to enable the Human vs Human game.
	 */
	private RadioButton human;

	/**
	 * A back button. Returns the user from settings to menu.
	 */
	private Button back;

	/**
	 * Boolean array that holds if a setting is enabled or not.
	 */
	private boolean[] settingsHolder = new boolean[5];

	/**
	 * Sets the settings from one state to another.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	private void setText() {
		sound.setText(settingsHolder[0] ? "Sound on" : "Sound off");
		sound.setChecked(settingsHolder[0]);
		vibration.setText(settingsHolder[1] ? "Vibrate on" : "Vibrate off");
		vibration.setChecked(settingsHolder[1]);
		easy.setChecked(settingsHolder[2]);
		human.setChecked(!settingsHolder[2]);
		normal.setChecked(settingsHolder[3]);
		hard.setChecked(settingsHolder[4]);

	}

	/**
	 * Get all settings booleans.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	private void getSettings() {
		SharedPreferences settings = getSharedPreferences(Menu.PREFS_NAME, 0);

		settingsHolder[0] = settings.getBoolean("sound", true);

		settingsHolder[1] = settings.getBoolean("vibrate", true);

		settingsHolder[2] = settings.getBoolean("easy", true);

		settingsHolder[3] = settings.getBoolean("normal", false);

		settingsHolder[4] = settings.getBoolean("hard", false);
	}

	/**
	 * Called when the activity is first created. Creates the user interface for
	 * the settings.
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
		setContentView(R.layout.settings);

		/*
		 * Assign the text views to the corresponding elements in the layout
		 */
		sound = (CheckBox) findViewById(R.id.sound);

		vibration = (CheckBox) findViewById(R.id.vibrate);

		easy = (RadioButton) findViewById(R.id.easy);

		normal = (RadioButton) findViewById(R.id.normal);

		hard = (RadioButton) findViewById(R.id.hard);

		human = (RadioButton) findViewById(R.id.human);

		back = (Button) findViewById(R.id.back);

		/*
		 * Set the Click Listeners so we can switch on and off the settings
		 */
		sound.setOnClickListener(this);

		vibration.setOnClickListener(this);

		easy.setOnClickListener(this);

		normal.setOnClickListener(this);

		hard.setOnClickListener(this);

		human.setOnClickListener(this);

		back.setOnClickListener(this);

		/*
		 * Get the settings state.
		 */
		getSettings();

		/*
		 * Set the settings.
		 */
		setText();
	}

	/**
	 * User interface that controls if the settings are enabled or not.
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
	@Override
	public void onClick(View view) {
		SharedPreferences settings = getSharedPreferences(Menu.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();

		switch (view.getId()) {
		case R.id.sound:
			editor.putBoolean("sound", !settingsHolder[0]);
			break;

		case R.id.vibrate:
			editor.putBoolean("vibrate", !settingsHolder[1]);
			break;

		case R.id.easy:
			editor.putBoolean("easy", true);
			editor.putBoolean("normal", false);
			editor.putBoolean("hard", false);
			break;

		case R.id.normal:
			editor.putBoolean("normal", true);
			editor.putBoolean("easy", false);
			editor.putBoolean("hard", false);
			break;

		case R.id.hard:
			editor.putBoolean("hard", true);
			editor.putBoolean("easy", false);
			editor.putBoolean("normal", false);
			break;

		case R.id.human:
			editor.putBoolean("easy", false);
			editor.putBoolean("normal", false);
			editor.putBoolean("hard", false);
			break;

		case R.id.back:
			finish();
			break;
		}

		editor.commit();

		getSettings();
		setText();
	}
}
