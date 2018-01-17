package com.ariwilson.seismo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// Singleton.
public class SeismoDbAdapter {
  public static final String KEY_TITLE = "title";
  public static final String KEY_BODY = "body";
  public static final String KEY_ROWID = "_id";

  public static SeismoDbAdapter getAdapter() {
    return instance_;
  }

  public SeismoDbAdapter open(Context ctx) throws SQLException {
    db_helper_ = new DatabaseHelper(ctx);
    db_ = db_helper_.getWritableDatabase();
    return this;
  }
  
  public void close() {
    db_helper_.close();
  }

  public long createGraph(String name, ArrayList<ArrayList<Float>> graph) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try {
      ObjectOutputStream objects = new ObjectOutputStream(bytes);
      objects.writeObject(graph);
      objects.close();
      bytes.close();
    } catch (IOException e) {
      // Do nothing.
    }

    ContentValues initial_values = new ContentValues();
    initial_values.put(KEY_TITLE, name);
    initial_values.put(KEY_BODY, bytes.toByteArray());

    return db_.insert(DATABASE_TABLE, null, initial_values);
  }

  public boolean deleteGraph(String graph_name) {
    return db_.delete(DATABASE_TABLE, KEY_TITLE + "=\"" + graph_name + "\"",
                      null) > 0;
  }

  public ArrayList<String> fetchGraphNames() {
    Cursor cursor = db_.query(DATABASE_TABLE, new String[] {KEY_ROWID,
                              KEY_TITLE, KEY_BODY}, null, null, null, null,
                              null);
    ArrayList<String> graph_names = null;
    if (cursor != null) {
      cursor.moveToFirst();
      graph_names = new ArrayList<String>();
      int name_index = cursor.getColumnIndex(KEY_TITLE);
      for (; !cursor.isAfterLast(); cursor.moveToNext()) {
        graph_names.add(cursor.getString(name_index));
      }
      cursor.close();
    }
    return graph_names;
  }

  @SuppressWarnings("unchecked")
  public ArrayList<ArrayList<Float>> fetchGraph(String graph_name) {
    Cursor cursor =
        db_.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                  KEY_BODY}, KEY_TITLE + "=\"" + graph_name + "\"", null, null, null,
                  null, null);
    ArrayList<ArrayList<Float>> graph = null;
    if (cursor != null) {
      cursor.moveToFirst();
      int graph_index = cursor.getColumnIndex(KEY_BODY);
      ByteArrayInputStream bytes = new ByteArrayInputStream(cursor.getBlob(
          graph_index));
      try {
        ObjectInputStream objects = new ObjectInputStream(bytes);
        graph = (ArrayList<ArrayList<Float>>)objects.readObject();
      } catch (Exception e) {
        // Do nothing.
      }
      cursor.close();
    }
    return graph;
  }

  private SeismoDbAdapter() {}

  private static class DatabaseHelper extends SQLiteOpenHelper {
    DatabaseHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int old_version, int new_version) {
      Log.w(TAG, "Upgrading database from version " + old_version + " to " +
                 new_version + ", which will destroy all old data");
      db.execSQL("DROP TABLE IF EXISTS seismo");
      onCreate(db);
    }
  }

  private static final String TAG = "SeismoDbAdapter";
  private DatabaseHelper db_helper_;
  private SQLiteDatabase db_;
  
  private static final String DATABASE_CREATE =
      "create table seismo (_id integer primary key autoincrement, " +
      "title text not null, body blob not null);";

  private static final String DATABASE_NAME = "data";
  private static final String DATABASE_TABLE = "seismo";
  private static final int DATABASE_VERSION = 2;

  private static final SeismoDbAdapter instance_ = new SeismoDbAdapter();
}
