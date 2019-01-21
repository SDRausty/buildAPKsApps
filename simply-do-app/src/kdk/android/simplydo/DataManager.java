/*
 * Copyright (C) 2010, 2011 Keith Kildare
 * 
 * This file is part of SimplyDo.
 * 
 * SimplyDo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * SimplyDo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SimplyDo.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package kdk.android.simplydo;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class DataManager
{
    public static final String DATABASE_NAME = "simplydo.db";
    private static final int DATABASE_VERSION = 1;

    private static class DatabaseHelper extends SQLiteOpenHelper
    {

        DatabaseHelper(Context context) 
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) 
        {
            db.execSQL("CREATE TABLE lists ("
                    + "id INTEGER PRIMARY KEY,"
                    + "label TEXT"
                    + ");");
            db.execSQL("CREATE TABLE items ("
                    + "id INTEGER PRIMARY KEY,"
                    + "list_id INTEGER,"
                    + "label TEXT,"
                    + "active INTEGER,"
                    + "star INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
        {
            Log.d(L.TAG, "Got callback to upgrading database from version " + 
                    oldVersion + 
                    " to "
                    + newVersion);
        }
    }
    
    private DatabaseHelper helper;
    
    
    public DataManager(Context ctx)
    {
    	helper = new DatabaseHelper(ctx);
    }
    
    public List<ListDesc> fetchLists()
    {
    	List<ListDesc> rv = new ArrayList<ListDesc>();
    	SQLiteDatabase db = helper.getReadableDatabase();
    	Cursor cursor = db.query("lists", new String[] { "id", "label" },
    		null, null, null, null, "id desc");
    	
		if (cursor.moveToFirst()) 
		{
		    do
		    {
		        ListDesc list = new ListDesc(cursor.getInt(0), cursor.getString(1), 0, 0);
		        list.setTotalItems(countItemsInList(db, list.getId()));
                list.setActiveItems(countInactiveItemsInList(db, list.getId()));
		    	rv.add(list);
		    } while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		
		Log.d(L.TAG, "fetchLists returned " + rv.size() + " items");

		db.close();
    	return rv;
    }
    
    public void fetchItems(int listId, List<ItemDesc> rv)
    {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("items", new String[] { "id", "label", "active", "star" },
            "list_id=?", new String[]{"" + listId}, null, null, "id desc");
        
        if (cursor.moveToFirst()) 
        {
            do
            {
                ItemDesc list = new ItemDesc(
                        cursor.getInt(0), 
                        cursor.getString(1), 
                        cursor.getInt(2) != 0, 
                        cursor.getInt(3) != 0);
                rv.add(list);
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed())
        {
            cursor.close();
        }
        
        Log.d(L.TAG, "fetchItems returned " + rv.size() + " items");

        db.close();
    }
    
    
    public void updateItemActiveness(int itemId, boolean active)
    {
        Log.d(L.TAG, "Setting active property of " + itemId + " to " + active);

        //long t = System.currentTimeMillis();
        SQLiteDatabase db = helper.getWritableDatabase();
        //long t2 = System.currentTimeMillis();
        //Log.d(TAG, "Open DB took " + ((t2-t)/1000.0));
        //t = t2;
        SQLiteStatement stmt = db.compileStatement("update items set active=? where id=?");        
        stmt.bindLong(1, active?1:0);
        stmt.bindLong(2, itemId);
        //t2 = System.currentTimeMillis();
        //Log.d(TAG, "stmt creation took " + ((t2-t)/1000.0));
        //t = t2;
        stmt.execute();
        //t2 = System.currentTimeMillis();
        //Log.d(TAG, "execute() took " + ((t2-t)/1000.0));
        //t = t2;
        stmt.close();
        db.close();
        //t2 = System.currentTimeMillis();
        //Log.d(TAG, "close() took " + ((t2-t)/1000.0));
        //t = t2;
    }
    
    
    public void updateItemStarness(int itemId, boolean star)
    {
        Log.d(L.TAG, "Setting star property of  " + itemId + " to " + star);

        SQLiteDatabase db = helper.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement("update items set star=? where id=?");        
        stmt.bindLong(1, star?1:0);
        stmt.bindLong(2, itemId);
        stmt.execute();
        stmt.close();
        db.close();
    }
    
    
    public void updateItemLabel(int itemId, String newLabel)
    {
        Log.d(L.TAG, "Updating label of item " + itemId + " to " + newLabel);

        SQLiteDatabase db = helper.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement("update items set label=? where id=?");        
        stmt.bindString(1, newLabel);
        stmt.bindLong(2, itemId);
        stmt.execute();
        stmt.close();
        db.close();
    }
    
    
    public void updateListLabel(int listId, String newLabel)
    {
        Log.d(L.TAG, "Updating label of list " + listId + " to " + newLabel);

        SQLiteDatabase db = helper.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement("update lists set label=? where id=?");        
        stmt.bindString(1, newLabel);
        stmt.bindLong(2, listId);
        stmt.execute();
        stmt.close();
        db.close();
    }
    
    public void moveItem(int itemId, int toListId)
    {
        Log.d(L.TAG, "Moving item " + itemId + " to list " + toListId);
        
        SQLiteDatabase db = helper.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement("update items set list_id=? where id=?");        
        stmt.bindLong(1, toListId);
        stmt.bindLong(2, itemId);
        stmt.execute();
        stmt.close();
        db.close();
    }
    
    public void createList(String label)
    {
        Log.d(L.TAG, "Insert list " + label);

        SQLiteDatabase db = helper.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement("insert into lists (label) values (?)");        
        stmt.bindString(1, label);
        stmt.executeInsert();
        stmt.close();
        db.close();
    }
    
    public int createItem(int list_id, String label)
    {
        Log.v(L.TAG, "DataManager.createItem(): Insert item " + label);

        SQLiteDatabase db = helper.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement("insert into items (list_id,label,active) values (?,?,?)");        
        stmt.bindLong(1, list_id);
        stmt.bindString(2, label);
        stmt.bindLong(3, 1);
        long id = stmt.executeInsert();
        stmt.close();
        db.close();
        
        Log.d(L.TAG, "DataManager.createItem(): Inserted item and got id " + id);
        if(id == -1)
        {
            Log.e(L.TAG, "DataManager.createItem(): Attempt to insert item failed. Got " + id + " from executeInsert()");
        }
        return (int)id;
    }
    
    public void deleteInactive(int list_id)
    {
        Log.d(L.TAG, "Deleting inactive items from list " + list_id);

        SQLiteDatabase db = helper.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement("delete from items where list_id=? and active=0");        
        stmt.bindLong(1, list_id);
        stmt.execute();
        stmt.close();
        db.close();
    }
    
    public void deleteList(int list_id)
    {
        Log.d(L.TAG, "Deleting list " + list_id);

        SQLiteDatabase db = helper.getWritableDatabase();
        
        SQLiteStatement stmt = db.compileStatement("delete from items where list_id=?");        
        stmt.bindLong(1, list_id);
        stmt.execute();
        stmt.close();
        
        SQLiteStatement stmt2 = db.compileStatement("delete from lists where id=?");        
        stmt2.bindLong(1, list_id);
        stmt2.execute();
        stmt2.close();
        db.close();
    }
    
    public void deleteItem(int itemId)
    {
        Log.d(L.TAG, "Deleting item " + itemId);

        SQLiteDatabase db = helper.getWritableDatabase();
        
        SQLiteStatement stmt = db.compileStatement("delete from items where id=?");        
        stmt.bindLong(1, itemId);
        stmt.execute();
        stmt.close();
        db.close();
    }
    
    
    private int countItemsInList(SQLiteDatabase db, int listId)
    {
        SQLiteStatement stmt = db.compileStatement("select count(*) from items where list_id=?");        
        stmt.bindLong(1, listId);
        int rv = (int)stmt.simpleQueryForLong();
        stmt.close();
        
        return rv;
    }
    
    
    private int countInactiveItemsInList(SQLiteDatabase db, int listId)
    {
        SQLiteStatement stmt = db.compileStatement("select count(*) from items where list_id=? and active=1");        
        stmt.bindLong(1, listId);
        int rv = (int)stmt.simpleQueryForLong();
        stmt.close();
        
        return rv;
    }
}
