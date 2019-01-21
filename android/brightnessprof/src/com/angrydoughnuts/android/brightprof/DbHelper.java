/****************************************************************************
 * Copyright 2009 kraigs.android@gmail.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 ****************************************************************************/

package com.angrydoughnuts.android.brightprof;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
  public static final String DB_NAME = "brightprof";
  public static final String DB_TABLE_PROFILES = "profiles";
  public static final String DB_TABLE_CALIBRATE = "calibrate";
  public static final int DB_VERSION = 3;
  public static final String PROF_ID_COL = "_id";
  public static final String PROF_NAME_COL = "name";
  public static final String PROF_VALUE_COL = "value";
  public static final String CALIB_MIN_BRIGHT_COL = "min_bright";

  public DbHelper(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    // Table to hold information for each profile.
    db.execSQL("CREATE TABLE " + DB_TABLE_PROFILES + " (" + PROF_ID_COL
        + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PROF_NAME_COL
        + " TEXT NOT NULL," + PROF_VALUE_COL + " UNSIGNED INTEGER (0, 100))");
    db.execSQL("INSERT INTO " + DB_TABLE_PROFILES + "( " + PROF_NAME_COL + ", "
        + PROF_VALUE_COL + ") VALUES ('Low', 0)");
    db.execSQL("INSERT INTO " + DB_TABLE_PROFILES + "( " + PROF_NAME_COL + ", "
        + PROF_VALUE_COL + ") VALUES ('Normal', 15)");
    db.execSQL("INSERT INTO " + DB_TABLE_PROFILES + "( " + PROF_NAME_COL + ", "
        + PROF_VALUE_COL + ") VALUES ('High', 100)");

    createCalibrationTable(db);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // DB version 3 added a table for keeping track of minimum brightness.
    // It is no longer necessary to enforce this minimum in the profile table
    // (as values 0 through 100 are now valid).
    // This creates the new minimum brightness table for old installs.
    if (oldVersion < 3) {
      createCalibrationTable(db);
    }
  }

  void createCalibrationTable(SQLiteDatabase db) {
    // Table to hold calibration settings.
    db.execSQL("CREATE TABLE " + DB_TABLE_CALIBRATE + " ("
        + CALIB_MIN_BRIGHT_COL + " UNSIGNED INTEGER (1, 255))");
    db.execSQL("INSERT INTO " + DB_TABLE_CALIBRATE + "( "
        + CALIB_MIN_BRIGHT_COL + ") VALUES (10)");
  }
}
