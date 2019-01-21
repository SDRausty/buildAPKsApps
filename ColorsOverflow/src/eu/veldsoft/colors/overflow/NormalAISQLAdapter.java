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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

/**
 * This class is used to manage all the operations that are connected with the
 * SQLite database for the Normal AI.
 * 
 * @author Yuriy Stanchev
 * 
 * @email i_stanchev@ml1.net
 * 
 * @date 10 April 2012
 */
public class NormalAISQLAdapter extends Activity {

	/**
	 * Used to define the helper that manages the database creation and version
	 * management.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 10 April 2012
	 */
	private class SQLiteHelper extends SQLiteOpenHelper {

		/**
		 * Constructor of the helper.
		 * 
		 * @param context
		 *            Used to open or create the database.
		 * 
		 * @param name
		 *            Name of the database file, or null for an in-memory
		 *            database.
		 * 
		 * @param factory
		 *            Used for creating cursor objects, or null for the default.
		 * 
		 * @param version
		 *            Database version.
		 * 
		 * @author Yuriy Stanchev
		 * 
		 * @email i_stanchev@ml1.net
		 * 
		 * @date 10 April 2012
		 */
		public SQLiteHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		/**
		 * Called when the activity is first created. Creates the NormalAI
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
		 * @date 10 April 2012
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
		 * @date 10 April 2012
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
	private static final String DATABASE_NAME = "ai.db";

	/**
	 * Name of the table.
	 */
	private static final String DATABASE_TABLE = "normalai";

	/**
	 * Database version.
	 */
	private static final int DATABASE_VERSION = 1;

	/**
	 * ID that is used to identify each row. Auto-increments.
	 */
	private static final String KEY_ID = "id";

	/**
	 * Column used for the storage of the combination of moves.
	 */
	private static final String KEY_COMBINATION = "combination";

	/**
	 * Column used for the storage of the coefficient of the move.
	 */
	private static final String KEY_COEFF = "coefficient";

	/**
	 * Used to create the database.
	 */
	private static final String SCRIPT_CREATE_DATABASE = "create table "
			+ DATABASE_TABLE + " (" + KEY_ID
			+ " integer primary key autoincrement, " + KEY_COMBINATION
			+ " LONG not null, " + KEY_COEFF + " LONG NOT NULL );";

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
	 * @date 10 April 2012
	 */
	public NormalAISQLAdapter(Context contex) {
		this.context = contex;
	}

	/**
	 * Opens the database to be read. Used to open the NormalAI database so the
	 * coefficients of each move can be read.
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
	 * @date 10 April 2012
	 */
	public NormalAISQLAdapter openToRead() throws android.database.SQLException {
		sqLiteHelper = new SQLiteHelper(context, DATABASE_NAME, null,
				DATABASE_VERSION);

		sqLiteDatabase = sqLiteHelper.getReadableDatabase();

		return this;
	}

	/**
	 * Opens the database for writing. Used for changing coefficient and
	 * improvement of the game.
	 * 
	 * @return Returns a writable database object.
	 * 
	 * @throws SQLException
	 *             Throws this exception on database write problems.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 10 April 2012
	 */
	public NormalAISQLAdapter openToWrite()
			throws android.database.SQLException {
		sqLiteHelper = new SQLiteHelper(context, DATABASE_NAME, null,
				DATABASE_VERSION);

		sqLiteDatabase = sqLiteHelper.getWritableDatabase();

		return this;
	}

	/**
	 * Closes the database nice and clean.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 10 April 2012
	 */
	public void close() {
		sqLiteHelper.close();
	}

	/**
	 * Inserts the coefficients for the Normal AI database. Used only in
	 * database initialization process.
	 * 
	 * @param combination
	 *            Used for the combination of the move.
	 * 
	 * @param coefficient
	 *            Used for the coefficient of the move.
	 * 
	 * @return The row ID of the newly inserted row, or -1 if an error occurred.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 10 April 2012
	 */
	public long insert(Integer combination, Integer coefficient) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_COMBINATION, combination);
		contentValues.put(KEY_COEFF, coefficient);
		return sqLiteDatabase.insert(DATABASE_TABLE, null, contentValues);
	}

	/**
	 * Clears the Normal AI database.
	 * 
	 * @return the number of rows affected if a whereClause is passed in, 0
	 *         otherwise.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 10 April 2012
	 */
	public int deleteAll() {
		return sqLiteDatabase.delete(DATABASE_TABLE, null, null);
	}

	/**
	 * Update the coefficient by ID.
	 * 
	 * @param id
	 *            Holds the row number that has to be updated.
	 * 
	 * @param combination
	 *            Sets the combination that needs to be updated.
	 * 
	 * @param coeff
	 *            Sets the new coefficient.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 10 April 2012
	 */
	public void updateByID(int id, String combination, Integer coeff) {
		ContentValues values = new ContentValues();
		values.put(KEY_COMBINATION, combination);
		values.put(KEY_COEFF, coeff);
		sqLiteDatabase.update(DATABASE_TABLE, values, KEY_ID + "=" + id, null);
	}

	/**
	 * Get the coefficient of the current combination.
	 * 
	 * @param keys
	 *            Set of keys to be checked into the database.
	 * 
	 * @return Returns the coefficient of the combination.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 10 April 2012
	 */
	public Integer obtainCoefficient(long keys[]) {
		Integer value = null;

		Cursor cursor = this.sqLiteDatabase.rawQuery("SELECT " + KEY_COEFF
				+ " FROM " + DATABASE_TABLE + " WHERE " + KEY_COMBINATION
				+ " = " + keys[0] + " OR " + keys[1] + " OR " + keys[2]
				+ " OR " + keys[3] + " OR " + keys[4], null);

		if (cursor.moveToFirst() == true) {
			value = Integer.parseInt(cursor.getString(0));
		}

		return (value);
	}
}