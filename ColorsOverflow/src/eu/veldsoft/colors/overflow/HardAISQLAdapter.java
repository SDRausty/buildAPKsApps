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
 * SQLite database for the Hard AI.
 * 
 * @author Yuriy Stanchev
 * 
 * @email i_stanchev@ml1.net
 * 
 * @date 10 April 2012
 */
public class HardAISQLAdapter extends Activity {

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
	private static final String DATABASE_TABLE = "hardai";

	/**
	 * Database version.
	 */
	private static final int DATABASE_VERSION = 1;

	/**
	 * ID that is used to identify each row. Auto-increments.
	 */
	public static final String KEY_ID = "id";

	/**
	 * Column used for the storage of the input layer data.
	 */
	public static final String SIZE_INPUT = "size_input";

	/**
	 * Column used for the storage of the hidden layer data.
	 */
	public static final String SIZE_HIDDEN = "size_hidden";

	/**
	 * Column used for the storage of the output layer data.
	 */
	public static final String SIZE_OUTPUT = "size_output";

	/**
	 * Column used for the storage of the weights.
	 */
	public static final String WEIGHTS = "weights";

	/**
	 * Column used for the fitness of the made decision.
	 */
	public static final String FITNESS = "fitness";

	/**
	 * Used to create the database.
	 */
	private static final String SCRIPT_CREATE_DATABASE = "create table "
			+ DATABASE_TABLE + " (" + KEY_ID
			+ " integer primary key autoincrement, " + SIZE_INPUT
			+ " integer not null, " + SIZE_HIDDEN + " integer not null, "
			+ SIZE_OUTPUT + " integer not null, " + WEIGHTS
			+ " text not null, " + FITNESS + " real not null );";

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
	public HardAISQLAdapter(Context contex) {
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
	public HardAISQLAdapter openToRead() throws android.database.SQLException {
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
	public HardAISQLAdapter openToWrite() throws android.database.SQLException {
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
	 * Inserts the data needed by the Hard AI database.
	 * 
	 * @param input
	 *            Input layer size.
	 * 
	 * @param hidden
	 *            Hidden layer size.
	 * 
	 * @param output
	 *            Output layer size.
	 * 
	 * @param weight
	 *            Weights for the neural network.
	 * 
	 * @param fitness
	 *            Fitness of the neural network.
	 * 
	 * @return the row ID of the newly inserted row, or -1 if an error occurred.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 15 April 2012
	 */
	public long insert(Integer input, Integer hidden, Integer output,
			String weight, Double fitness) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(SIZE_INPUT, input);
		contentValues.put(SIZE_HIDDEN, hidden);
		contentValues.put(SIZE_OUTPUT, output);
		contentValues.put(WEIGHTS, weight);
		contentValues.put(FITNESS, fitness);
		return sqLiteDatabase.insert(DATABASE_TABLE, null, contentValues);
	}

	/**
	 * Clears the Hard AI database.
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
	 * Update the fitness in the ANN Database by ID.
	 * 
	 * @param id
	 *            Holds the row number that has to be updated.
	 * 
	 * @param fitness
	 *            Sets the new fitness.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 10 April 2012
	 */
	public void updateByID(int id, Double fitness) {
		ContentValues values = new ContentValues();
		values.put(FITNESS, fitness);
		sqLiteDatabase.update(DATABASE_TABLE, values, KEY_ID + "=" + id, null);
	}

	/**
	 * Get weights for a random ID.
	 * 
	 * @return Returns the weights on a random id of the Hard AI database.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 19 April 2012
	 */
	public String getWeights() {
		String string = null;
		Cursor cursor = this.sqLiteDatabase.rawQuery("SELECT " + WEIGHTS
				+ " FROM " + DATABASE_TABLE + " ORDER BY RANDOM() LIMIT 1)",
				null);

		if (cursor.moveToFirst()) {
			string = cursor.getString(0);
		}
		return (string);
	}
}