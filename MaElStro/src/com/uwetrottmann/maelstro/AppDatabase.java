package com.uwetrottmann.maelstro;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class AppDatabase extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "maelstro.db";

	public static final int DATABASE_VERSION = 1;

	public interface Tables {
		String NUMBERS = "numbers";
	}

	public interface NumberColumns extends BaseColumns {
		String NUMBER = "number";
		String TIMESTAMP = "timestamp";
	}

	private static final String CREATE_NUMBERS_TABLE = "CREATE TABLE "
			+ Tables.NUMBERS + " ("

			+ BaseColumns._ID + " INTEGER PRIMARY KEY,"

			+ NumberColumns.NUMBER + " INTEGER NOT NULL,"

			+ NumberColumns.TIMESTAMP + " INTEGER DEFAULT 0"

			+ ");";

	public AppDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_NUMBERS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + Tables.NUMBERS);
		onCreate(db);
	}

}
