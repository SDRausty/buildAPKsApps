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
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

/**
 * This class is used to manage all the operations that are connected with the
 * SQLite database used by the High Score Activity.
 * 
 * @author Yuriy Stanchev
 * 
 * @email i_stanchev@ml1.net
 * 
 * @date 11 Mar 2012
 */
public class SQLiteAdapter extends Activity {

	/**
	 * Used to define the helper that manages the database creation and version
	 * management.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	private class SQLiteHelper extends SQLiteOpenHelper {

		/**
		 * Constructor of the helper.
		 * 
		 * @param context
		 *            To use to open or create the database.
		 * 
		 * @param name
		 *            Name of the database file, or null for an in-memory
		 *            database.
		 * 
		 * @param factory
		 *            Factory to use for creating cursor objects, or null for
		 *            the default.
		 * 
		 * @param version
		 *            Database version.
		 * 
		 * @author Yuriy Stanchev
		 * 
		 * @email i_stanchev@ml1.net
		 * 
		 * @date 11 Mar 2012
		 */
		public SQLiteHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		/**
		 * Called when the activity is first created. Creates the high score
		 * table.
		 * 
		 * @param db
		 *            Holds an SQLiteDatabase object used to create the database
		 *            table.
		 * 
		 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database
		 *      .sqlite.SQLiteDatabase)
		 * 
		 * @author Yuriy Stanchev
		 * 
		 * @email i_stanchev@ml1.net
		 * 
		 * @date 11 Mar 2012
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SCRIPT_CREATE_DATABASE);
		}

		/**
		 * Method to upgrade the database from one version to another.
		 * 
		 * @param db
		 *            Holds the database object which will have a new version.
		 * 
		 * @param oldVersion
		 *            Old version number.
		 * 
		 * @param newVersion
		 *            New version number.
		 * 
		 * @author Yuriy Stanchev
		 * 
		 * @email i_stanchev@ml1.net
		 * 
		 * @date 11 Mar 2012
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}

	/**
	 * A helper to manage database creation and version management.
	 */
	private SQLiteHelper sqLiteHelper;

	/**
	 * SQLiteDatabase object used to create, delete, execute SQL commands, and
	 * perform other common database management tasks.
	 */
	private SQLiteDatabase sqLiteDatabase;

	/**
	 * Context object used for the database operations.
	 */
	private Context context;

	/**
	 * Name of the database file.
	 */
	private static final String DATABASE_NAME = "highscores.db";

	/**
	 * Name of the table.
	 */
	private static final String DATABASE_TABLE = "highscore";

	/**
	 * Database version.
	 */
	private static final int DATABASE_VERSION = 1;

	/**
	 * ID that is used to identify each row. Auto-increments.
	 */
	private static final String KEY_ID = "id";

	/**
	 * Column used for the name of the player that achieved the score.
	 */
	public static final String KEY_NAME = "name";

	/**
	 * Column used for the score achieved record.
	 */
	public static final String KEY_SCORE = "score";

	/**
	 * Used to create the database.
	 */
	private static final String SCRIPT_CREATE_DATABASE = "create table "
			+ DATABASE_TABLE + " (" + KEY_ID
			+ " integer primary key autoincrement, " + KEY_NAME
			+ " text not null, " + KEY_SCORE + " LONG NOT NULL );";

	/**
	 * Constructor.
	 * 
	 * @param contex
	 *            Takes and keeps a reference of the passed context in order to
	 *            access to the application assets and resources.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	public SQLiteAdapter(Context contex) {
		this.context = contex;
	}

	/**
	 * Opens the database to be read. Used for displaying the high scores.
	 * 
	 * @return Returns a readable database object.
	 * 
	 * @throws SQLException
	 *             Throws this exception on database connect problems.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	public SQLiteAdapter openToRead() throws android.database.SQLException {
		sqLiteHelper = new SQLiteHelper(context, DATABASE_NAME, null,
				DATABASE_VERSION);

		sqLiteDatabase = sqLiteHelper.getReadableDatabase();

		return (this);
	}

	/**
	 * Opens the database for writing. Used for the writing of high scores.
	 * 
	 * @return Returns a writable database object.
	 * 
	 *         * @throws SQLException Throws this exception on database write
	 *         problems.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	public SQLiteAdapter openToWrite() throws android.database.SQLException {
		sqLiteHelper = new SQLiteHelper(context, DATABASE_NAME, null,
				DATABASE_VERSION);

		sqLiteDatabase = sqLiteHelper.getWritableDatabase();

		return (this);
	}

	/**
	 * Closes the database nice and clean.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	public void close() {
		sqLiteHelper.close();
	}

	/**
	 * Inserts the content of the database.
	 * 
	 * @param content
	 *            Used for the players name.
	 * 
	 * @param score
	 *            Used for the achieved score.
	 * 
	 * @return the row ID of the newly inserted row, or -1 if an error occurred.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	public long insert(String content, Integer score) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_NAME, content);
		contentValues.put(KEY_SCORE, score);
		return sqLiteDatabase.insert(DATABASE_TABLE, null, contentValues);
	}

	/**
	 * Clears the high score table.
	 * 
	 * @return The number of rows affected if a whereClause is passed in, 0
	 *         otherwise.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	public int deleteAll() {
		return (sqLiteDatabase.delete(DATABASE_TABLE, null, null));
	}

	/**
	 * Get everything that is inside the Table.
	 * 
	 * @return Returns a cursor that can be parsed from the high-score activity
	 *         and displayed to the user.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	public Cursor queueAll() {
		return (sqLiteDatabase.rawQuery("SELECT * FROM " + DATABASE_TABLE
				+ " ORDER BY " + KEY_SCORE + " ASC", null));
	}

	/**
	 * Update the score by ID.
	 * 
	 * @param id
	 *            Holds the row number that has to be updated.
	 * 
	 * @param content
	 *            Sets the name of the player.
	 * 
	 * @param score
	 *            Sets the new score.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	public void updateByID(int id, String content, Integer score) {
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, content);
		values.put(KEY_SCORE, score);
		sqLiteDatabase.update(DATABASE_TABLE, values, KEY_ID + "=" + id, null);
	}

	/**
	 * Count the number of Entries in the high score.
	 * 
	 * @return Returns the number of rows in the database.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	public long count() {
		return DatabaseUtils.queryNumEntries(sqLiteDatabase, DATABASE_TABLE);
	}

	/**
	 * Get the minimal score.
	 * 
	 * @return Returns the minimal achieved score from the database.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	public int minimal() {
		String s = null;
		Cursor c = this.sqLiteDatabase.rawQuery("SELECT MIN(" + KEY_SCORE
				+ ") FROM " + DATABASE_TABLE, null);

		if (c.moveToFirst()) {
			s = c.getString(0);
		}

		return (Integer.parseInt(s));
	}

	/**
	 * Get the ID of the minimal score.
	 * 
	 * @return Returns the row ID of the achieved minimal score.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	public int minimalId() {
		String string = null;

		Cursor cursor = this.sqLiteDatabase.rawQuery("SELECT id FROM "
				+ DATABASE_TABLE + " WHERE " + KEY_SCORE + " = (SELECT MIN("
				+ KEY_SCORE + ") FROM " + DATABASE_TABLE + ")", null);

		if (cursor.moveToFirst()) {
			string = cursor.getString(0);
		}

		return (Integer.parseInt(string));
	}
}