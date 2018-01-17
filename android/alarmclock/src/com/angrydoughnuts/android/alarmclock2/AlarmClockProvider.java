/****************************************************************************
 * Copyright 2016 kraigs.android@gmail.com
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

package com.angrydoughnuts.android.alarmclock;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

public final class AlarmClockProvider extends ContentProvider {
  private SQLiteDatabase db = null;

  @Override
  public boolean onCreate() {
    db = new DbAlarmClockHelper(getContext()).getWritableDatabase();
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection,
                      String[] selectionArgs, String sortOrder) {
    Cursor c;
    long alarmid;

    switch (matcher.match(uri)) {
    case ALARMS:
      c = db.query(AlarmEntry.TABLE_NAME, projection, selection, selectionArgs,
                   null, null, sortOrder);
      c.setNotificationUri(getContext().getContentResolver(), uri);
      return c;
    case ALARM_ID:
      alarmid = ContentUris.parseId(uri);
      c = db.query(AlarmEntry.TABLE_NAME, projection,
                   AlarmEntry._ID + " == " + alarmid, null, null, null, null);
      c.setNotificationUri(getContext().getContentResolver(), uri);
      return c;
    case SETTINGS_ID:
      alarmid = ContentUris.parseId(uri);
      c = db.query(SettingsEntry.TABLE_NAME, projection,
                   SettingsEntry.ALARM_ID + " == " + alarmid,
                   null, null, null, null);
      c.setNotificationUri(getContext().getContentResolver(), uri);
      return c;
    default:
      throw new IllegalArgumentException("Unknown URI " + uri);
    }
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    switch (matcher.match(uri)) {
    case ALARMS:
      if (!values.containsKey(AlarmEntry.TIME))
        throw new IllegalArgumentException("Missing time");
      // New alarms are always created enabled, but without a label, repeat,
      // nor optional settings.
      values.put(AlarmEntry.ENABLED, true);
      values.put(AlarmEntry.NAME, "");
      values.put(AlarmEntry.DAY_OF_WEEK, 0);
      Uri result = ContentUris.withAppendedId(
          ALARMS_URI, db.insertOrThrow(AlarmEntry.TABLE_NAME, null, values));
      getContext().getContentResolver().notifyChange(uri, null);
      return result;
    case SETTINGS_ID:
      final long alarmid = ContentUris.parseId(uri);
      if (values.containsKey(SettingsEntry.ALARM_ID)) {
        if (values.getAsLong(SettingsEntry.ALARM_ID) != alarmid)
          throw new IllegalArgumentException(
              "ID does not match url: " + alarmid + " vs " + uri);
      } else {
        values.put(SettingsEntry.ALARM_ID, alarmid);
      }
      // Fill in missing fields with defaults.
      final DbUtil.Settings defaults =
        DbUtil.Settings.get(getContext(), DbUtil.Settings.DEFAULTS_ID);
      if (!values.containsKey(SettingsEntry.TONE_URL))
        values.put(SettingsEntry.TONE_URL, defaults.tone_url.toString());
      if (!values.containsKey(SettingsEntry.TONE_NAME))
        values.put(SettingsEntry.TONE_NAME, defaults.tone_name);
      if (!values.containsKey(SettingsEntry.SNOOZE))
        values.put(SettingsEntry.SNOOZE, defaults.snooze);
      if (!values.containsKey(SettingsEntry.VIBRATE))
        values.put(SettingsEntry.VIBRATE, defaults.vibrate);
      if (!values.containsKey(SettingsEntry.VOLUME_STARTING))
        values.put(SettingsEntry.VOLUME_STARTING, defaults.volume_starting);
      if (!values.containsKey(SettingsEntry.VOLUME_ENDING))
        values.put(SettingsEntry.VOLUME_ENDING, defaults.volume_ending);
      if (!values.containsKey(SettingsEntry.VOLUME_TIME))
        values.put(SettingsEntry.VOLUME_TIME, defaults.volume_time);

      db.insertOrThrow(SettingsEntry.TABLE_NAME, null, values);
      getContext().getContentResolver().notifyChange(uri, null);
      return uri;
    default:
      throw new IllegalArgumentException("Unknown URI " + uri);
    }
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection,
                    String[] selectionArgs) {
    long alarmid;
    int count;

    switch (matcher.match(uri)) {
    case ALARMS:
       count = db.update(
          AlarmEntry.TABLE_NAME, values, selection, selectionArgs);
      if (count > 0)
        getContext().getContentResolver().notifyChange(uri, null);
      return count;
    case ALARM_ID:
      alarmid = ContentUris.parseId(uri);
       count = db.update(
          AlarmEntry.TABLE_NAME, values,
          AlarmEntry._ID + " == " + alarmid, null);
      if (count > 0)
        getContext().getContentResolver().notifyChange(uri, null);
      return count;
    case SETTINGS_ID:
      alarmid = ContentUris.parseId(uri);
      count = db.update(
          SettingsEntry.TABLE_NAME, values,
          SettingsEntry.ALARM_ID + " == " + alarmid, null);
      // If the row did not exist, automatically fall back to insert behavior.
      if (count == 0)
        insert(uri, values);
      getContext().getContentResolver().notifyChange(uri, null);
      return 1;
    default:
      throw new IllegalArgumentException("Unknown URI " + uri);
    }
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    long alarmid;
    int count;

    switch (matcher.match(uri)) {
    case ALARMS:
      count = db.delete(AlarmEntry.TABLE_NAME, null, null);
      // Also delete corresponding entries in the settings table.
      // (But not the default alarm settings)
      count += db.delete(
          SettingsEntry.TABLE_NAME,
          SettingsEntry.ALARM_ID + " != " + DbUtil.Settings.DEFAULTS_ID, null);
      getContext().getContentResolver().notifyChange(uri, null);
      return count;
    case ALARM_ID:
      alarmid = ContentUris.parseId(uri);
      count = db.delete(
          AlarmEntry.TABLE_NAME, AlarmEntry._ID + " == " + alarmid, null);
      // Also delete corresponding entries in the settings table.
      if (count > 0) {
        getContext().getContentResolver().notifyChange(uri, null);
        count += db.delete(
            SettingsEntry.TABLE_NAME,
            SettingsEntry.ALARM_ID + " == " + alarmid, null);
      }
      return count;
    case SETTINGS_ID:
      alarmid = ContentUris.parseId(uri);
      count = db.delete(
          SettingsEntry.TABLE_NAME,
          SettingsEntry.ALARM_ID + " == " + alarmid, null);
      if (count > 0)
        getContext().getContentResolver().notifyChange(uri, null);
      return count;
    default:
      throw new IllegalArgumentException("Unknown URI " + uri);
    }
  }

  @Override
  public String getType(Uri uri) {
    switch (matcher.match(uri)) {
    case ALARMS:
      return "vnd.android.cursor.dir/vnd." + AlarmEntry.TABLE_NAME;
    case ALARM_ID:
      return "vnd.android.cursor.item/vnd." + AlarmEntry.TABLE_NAME;
    case SETTINGS_ID:
      return "vnd.android.cursor.item/vnd." + SettingsEntry.TABLE_NAME;
    default:
      throw new IllegalArgumentException("Unknown URI " + uri);
    }
  }

  private static final String AUTHORITY =
    "com.angrydoughnuts.android.alarmclock.provider";
  private static final int ALARMS = 1;
  private static final int ALARM_ID = 2;
  private static final int SETTINGS_ID = 3;
  private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
  static {
    matcher.addURI(AUTHORITY, AlarmEntry.TABLE_NAME, ALARMS);
    matcher.addURI(AUTHORITY, AlarmEntry.TABLE_NAME + "/#", ALARM_ID);
    matcher.addURI(AUTHORITY, SettingsEntry.TABLE_NAME + "/#", SETTINGS_ID);
  }

  public static final Uri ALARMS_URI = new Uri.Builder()
    .scheme(ContentResolver.SCHEME_CONTENT)
    .authority(AUTHORITY)
    .appendPath(AlarmEntry.TABLE_NAME)
    .build();
  public static final Uri SETTINGS_URI = new Uri.Builder()
    .scheme(ContentResolver.SCHEME_CONTENT)
    .authority(AUTHORITY)
    .appendPath(SettingsEntry.TABLE_NAME)
    .build();

  public static class AlarmEntry implements BaseColumns {
    public static final String TABLE_NAME = "alarms";

    public static final String TIME = "time";
    public static final String ENABLED = "enabled";
    public static final String NAME = "name";
    public static final String DAY_OF_WEEK = "dow";
    public static final String NEXT_SNOOZE = "next_snooze";
  }

  public static class SettingsEntry implements BaseColumns {
    public static final String TABLE_NAME = "settings";

    public static final String ALARM_ID = "id";
    public static final String TONE_URL = "tone_url";
    public static final String TONE_NAME = "tone_name";
    public static final String SNOOZE = "snooze";
    public static final String VIBRATE = "vibrate";
    public static final String VOLUME_STARTING = "vol_start";
    public static final String VOLUME_ENDING = "vol_end";
    public static final String VOLUME_TIME = "vol_time";
  }

  private static class DbAlarmClockHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "alarmclock";
    private static final int DB_VERSION = 2;

    public DbAlarmClockHelper(Context context) {
      super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      // Alarm metadata table:
      // |(auto primary) | (0 to 86399) | (boolean) | (string) | (bitmask(7)) |  (millisUTC) |
      // |     _id       |    time      |  enabled  |   name   |     dow      |  next_snooze |
      // time is seconds past midnight.
      db.execSQL(
          "CREATE TABLE " + AlarmEntry.TABLE_NAME + " (" +
          AlarmEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
          AlarmEntry.TIME + " UNSIGNED INTEGER (0, 86399)," +
          AlarmEntry.ENABLED + " UNSIGNED INTEGER (0, 1)," +
          AlarmEntry.NAME + " TEXT, " +
          AlarmEntry.DAY_OF_WEEK + " UNSIGNED INTEGER (0, 127)," +
          AlarmEntry.NEXT_SNOOZE + " UNSIGNED INTEGER DEFAULT 0)");

      // |(primary) | (string) | (string)  | (1 to 60) | (boolean) | (0 to 100) | (0 to 100) | (0 to 60) |
      // |   id     | tone_url | tone_name |   snooze  |  vibrate  |  vol_start |  vol_end   | vol_time  |
      // snooze is in minutes.
      db.execSQL(
          "CREATE TABLE " + SettingsEntry.TABLE_NAME + " (" +
          SettingsEntry.ALARM_ID + " INTEGER PRIMARY KEY, " +
          SettingsEntry.TONE_URL + " TEXT," +
          SettingsEntry.TONE_NAME + " TEXT," +
          SettingsEntry.SNOOZE + " UNSIGNED INTEGER (1, 60)," +
          SettingsEntry.VIBRATE + " UNSIGNED INTEGER (0, 1)," +
          SettingsEntry.VOLUME_STARTING + " UNSIGNED INTEGER (1, 100)," +
          SettingsEntry.VOLUME_ENDING + " UNSIGNED INTEGER (1, 100)," +
          SettingsEntry.VOLUME_TIME + " UNSIGNED INTEGER (1, 60))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      // Application version 1.x -> 2.0 added a column to the alarm table
      // for tracking next snooze trigger (rather that trying to keep this
      // information in memory).
      if (oldVersion < 2) {
        db.execSQL(
            "ALTER TABLE " + AlarmEntry.TABLE_NAME + " ADD COLUMN " +
            AlarmEntry.NEXT_SNOOZE + " UNSIGNED INTEGER DEFAULT 0");
      }
      // Application versions 1.x stored default alarm settings in row
      // id -1.  Since UriMatcher can only match positive numbers, this
      // data is now stored in row id Long.MAX_VALUE.  This move the row.
      if (oldVersion < 2) {
        db.execSQL(
            "UPDATE " + SettingsEntry.TABLE_NAME + " SET " +
            SettingsEntry.ALARM_ID + " = " + DbUtil.Settings.DEFAULTS_ID +
            " WHERE " + SettingsEntry.ALARM_ID + " == -1");
      }
    }
  }
}
