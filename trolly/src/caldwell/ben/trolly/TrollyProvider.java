/**
	<Trolly is a simple shopping list application for android phones.>
	Copyright (C) 2009  Ben Caldwell
 	
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package caldwell.ben.trolly;

import caldwell.ben.provider.Trolly;
import caldwell.ben.provider.Trolly.ShoppingList;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

/**
 * Provides access to a database of notes. Each note has a title, the note
 * itself, a creation date and a modified data.
 */
public class TrollyProvider extends ContentProvider {

    private static final String TAG = "TrollyProvider";

    private static final String DATABASE_NAME = "trolly.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "shopping_list";

    private static HashMap<String, String> sProjectionMap;

    private static final int ITEMS = 1;
    private static final int ITEM_ID = 2;

    private static final UriMatcher sUriMatcher;

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                    + ShoppingList._ID + " INTEGER PRIMARY KEY,"
                    + ShoppingList.ITEM + " TEXT,"
                    + ShoppingList.STATUS + " INTEGER,"
                    + ShoppingList.CREATED_DATE + " INTEGER,"
                    + ShoppingList.MODIFIED_DATE + " INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
        case ITEMS:
            qb.setTables(TABLE_NAME);
            qb.setProjectionMap(sProjectionMap);
            break;

        case ITEM_ID:
            qb.setTables(TABLE_NAME);
            qb.setProjectionMap(sProjectionMap);
            qb.appendWhere(ShoppingList._ID + "=" + uri.getPathSegments().get(1));
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = ShoppingList.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case ITEMS:
            return ShoppingList.CONTENT_TYPE;

        case ITEM_ID:
            return ShoppingList.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != ITEMS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis());

        // Make sure that the fields are all set
        if (values.containsKey(ShoppingList.CREATED_DATE) == false) {
            values.put(ShoppingList.CREATED_DATE, now);
        }

        if (values.containsKey(ShoppingList.MODIFIED_DATE) == false) {
            values.put(ShoppingList.MODIFIED_DATE, now);
        }

        if (values.containsKey(ShoppingList.ITEM) == false) {
            Resources r = Resources.getSystem();
            values.put(ShoppingList.ITEM, r.getString(android.R.string.untitled));
        }

        if (values.containsKey(ShoppingList.STATUS) == false) {
            values.put(ShoppingList.STATUS, Trolly.ShoppingList.ON_LIST);
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(TABLE_NAME, ShoppingList.ITEM, values);
        if (rowId > 0) {
            Uri itemUri = ContentUris.withAppendedId(ShoppingList.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(itemUri, null);
            return itemUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case ITEMS:
            count = db.delete(TABLE_NAME, where, whereArgs);
            break;

        case ITEM_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.delete(TABLE_NAME, ShoppingList._ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues initialValues, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis());

        // Update the modified field
        if (values.containsKey(ShoppingList.MODIFIED_DATE) == false) {
            values.put(ShoppingList.MODIFIED_DATE, now);
        }
        
        switch (sUriMatcher.match(uri)) {
        case ITEMS:
            count = db.update(TABLE_NAME, values, where, whereArgs);
            break;

        case ITEM_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.update(TABLE_NAME, values, ShoppingList._ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Trolly.AUTHORITY, "shoppinglist", ITEMS);
        sUriMatcher.addURI(Trolly.AUTHORITY, "shoppinglist/#", ITEM_ID);

        sProjectionMap = new HashMap<String, String>();
        sProjectionMap.put(ShoppingList._ID, ShoppingList._ID);
        sProjectionMap.put(ShoppingList.ITEM, ShoppingList.ITEM);
        sProjectionMap.put(ShoppingList.STATUS, ShoppingList.STATUS);
        sProjectionMap.put(ShoppingList.CREATED_DATE, ShoppingList.CREATED_DATE);
        sProjectionMap.put(ShoppingList.MODIFIED_DATE, ShoppingList.MODIFIED_DATE);
    }
}
