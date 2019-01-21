package com.uwetrottmann.maelstro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.uwetrottmann.maelstro.AppDatabase.NumberColumns;
import com.uwetrottmann.maelstro.AppDatabase.Tables;

public class DataHelper {

	private AppDatabase mDatabaseHelper;

	public DataHelper(Context context) {
		mDatabaseHelper = new AppDatabase(context);
	}

	/**
	 * Will return false if the number was not inserted because it already
	 * exists.
	 */
	public boolean insertNumber(int pin) {
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

		// check if number already was chosen
		final SelectionBuilder builder = new SelectionBuilder().table(
				Tables.NUMBERS).where(NumberColumns.NUMBER + "=" + pin);
		final Cursor numberQuery = builder.query(db, new String[] {
				NumberColumns._ID, NumberColumns.NUMBER }, null);
		if (numberQuery != null) {
			int count = numberQuery.getCount();
			numberQuery.close();
			if (count != 0) {
				return false;
			}
		}

		// store new number
		ContentValues values = new ContentValues();
		values.put(NumberColumns.NUMBER, pin);
		values.put(NumberColumns.TIMESTAMP, System.currentTimeMillis());
		db.insertOrThrow(Tables.NUMBERS, null, values);

		return true;
	}

	public void clearDatabase() {
		mDatabaseHelper.onUpgrade(mDatabaseHelper.getWritableDatabase(), 0, 0);
	}
}
