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

import java.util.ArrayList;
import java.util.List;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;

/**
 * This class is used to create a list activity so that a list of achieved
 * scores can be shown to the user.
 * 
 * @author Yuriy Stanchev
 * 
 * @email i_stanchev@ml1.net
 * 
 * @date 11 Mar 2012
 */
public class Highscore extends ListActivity {

	/**
	 * Create an SQLiteAdapter so we can handle (read/write/delete from) the
	 * database, in this case read the high score records.
	 */
	private SQLiteAdapter sqlitehandle = null;

	/**
	 * Array that holds the achieved results.
	 */
	private List<String> results = new ArrayList<String>();

	/**
	 * Called when the activity is first created. Set the high-score layout and
	 * get&show the results.
	 * 
	 * @param savedInstanceState
	 *            It is used to save the state of the created Activity.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 4 Apr 2012
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.highscore);

		this.setListAdapter(new ArrayAdapter<String>(this,
				R.layout.highscore_list_item, results));

		/*
		 * Open the same SQLite database and read all it's content.
		 */
		sqlitehandle = new SQLiteAdapter(this);
		sqlitehandle.openToRead();

		Cursor cursor = sqlitehandle.queueAll();
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					String personName = cursor.getString(cursor
							.getColumnIndex(SQLiteAdapter.KEY_NAME));
					int age = cursor.getInt(cursor
							.getColumnIndex(SQLiteAdapter.KEY_SCORE));
					results.add("" + personName + ", " + age);
				} while (cursor.moveToNext());
			}

			cursor.close();
		}

		sqlitehandle.close();
	}
}
