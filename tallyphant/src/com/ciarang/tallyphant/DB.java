/*
 * Copyright (C) 2012  Ciaran Gultnieks, ciaran@ciarang.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.ciarang.tallyphant;

import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DB {

    private static final String DATABASE_NAME = "tallyphant";

    private SQLiteDatabase db;

    private Context mContext;

    // The TABLE_ITEM table stores details of all the items we're counting.
    private static final String TABLE_ITEM = "tally_item";
    private static final String CREATE_TABLE_ITEM = "create table "
            + TABLE_ITEM + " ( " + "name text not null, "
            + "count int not null," + "primary key(name));";

    // SQL to update the database to versions beyond the first. Here is
    // how the database works:
    //
    // * The SQL to create the database tables always creates version
    // 1. This SQL will never be altered.
    // * In the array below there is SQL for each subsequent version
    // from 2 onwards.
    // * For a new install, the database is always initialised to version
    // 1.
    // * Then, whether it's a new install or not, all the upgrade SQL in
    // the array below is executed in order to bring the database up to
    // the latest version.
    // * The current version is tracked by an entry in the TABLE_VERSION
    // table.
    //
    private static final String[][] DB_UPGRADES = {
    // Version 2...
    {
            "alter table " + TABLE_ITEM
                    + " add buttonstyle int not null default 0",
            "alter table " + TABLE_ITEM
                    + " add labelstyle int not null default 0",
            "alter table " + TABLE_ITEM + " add pname text" } };

    // The available button styles. The localised names for these styles are
    // in the 'buttonstyles' String Array in res/values/strings.xml.
    public enum ButtonStyle {
        // - on left, + on right. Long-press for 'any'.
        MinusPlus,
        // Just a + on the right. Long-press for 'any'
        Plus
    }

    // The available label styles. The localised names for these styles are
    // in the 'labelstyles' String Array in res/values/strings.xml.
    public enum LabelStyle {
        // Simple item
        Item,
        // Score
        Score,
        // Item with plural
        Items
    }

    public class Item {
        public String name;
        public int count;
        public String pname;
        public ButtonStyle buttonstyle;
        public LabelStyle labelstyle;
        
        public String getFormatted() {
            switch(labelstyle) {
                case Item:
                    return Integer.toString(count) + " " + name;
                case Items:
                    // TODO: Localisation, e.g. French wants singular for 0
                    if(count==1)
                        return Integer.toString(count) + " " + name;
                    else
                        return Integer.toString(count) + " " + pname;
                case Score:
                    return name + ": " + Integer.toString(count);
                default:
                    return "Invalid label style";
            }
                   
        }
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DB_UPGRADES.length + 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_ITEM);
            onUpgrade(db, 1, DB_UPGRADES.length + 1);
            ContentValues values = new ContentValues();
            values.put("name", mContext.getString(R.string.elephant));
            values.put("count", 1);
            values.put("pname", mContext.getString(R.string.elephants));
            values.put("buttonstyle", ButtonStyle.MinusPlus.ordinal());
            values.put("labelstyle", LabelStyle.Item.ordinal());
            db.insert(TABLE_ITEM, null, values);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            for (int v = oldVersion + 1; v <= newVersion; v++)
                for (int i = 0; i < DB_UPGRADES[v - 2].length; i++)
                    db.execSQL(DB_UPGRADES[v - 2][i]);
        }

    }

    public DB(Context ctx) {

        mContext = ctx;
        DBHelper h = new DBHelper(ctx);
        db = h.getWritableDatabase();
    }

    public void close() {
        db.close();
        db = null;
    }

    // Get an item by name...
    public Item getItem(String name) {
        // TODO: Just being lazy...
        Vector<Item> items = getItems();
        for (Item item : items)
            if (item.name.equals(name))
                return item;
        return null;
    }

    // Get all items...
    public Vector<Item> getItems() {
        Vector<Item> items = new Vector<Item>();
        Cursor c = null;
        try {
            c = db.rawQuery(
                    "select name,count,pname,buttonstyle,labelstyle from "
                            + TABLE_ITEM, null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                Item item = new Item();
                item.name = c.getString(0);
                item.count = c.getInt(1);
                item.pname = c.getString(2);
                if (item.pname == null)
                    item.pname = "";
                item.buttonstyle = ButtonStyle.values()[c.getInt(3)];
                item.labelstyle = LabelStyle.values()[c.getInt(4)];
                items.add(item);
                c.moveToNext();
            }
        } catch (Exception e) {
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return items;
    }

    // Update the given counter by the specified amount, returning the new
    // counter value.
    public int updateCount(String name, int amount) {
        // I couldn't figure out how to do this as a single query in sqlite
        // although surely it must be possible? Patches welcome!
        Cursor cursor = db.rawQuery("UPDATE " + TABLE_ITEM
                + " SET count = count + " + amount + " WHERE name = ?",
                new String[] { name });
        // If you just close the cursor, without doing the moveToFirst(), the
        // update doesn't happen.
        cursor.moveToFirst();
        cursor.close();
        cursor = db.rawQuery("SELECT count FROM " + TABLE_ITEM
                + " WHERE name = ?", new String[] { name });
        cursor.moveToFirst();
        int newcount = cursor.getInt(0);
        cursor.close();
        return newcount;
    }

    // Delete an item.
    public void deleteItem(String name) {
        db.delete(TABLE_ITEM, "name = ?", new String[] { name });
    }

    // Update an item. Throws an exception if something goes wrong.
    // Pass null for oldname to create a new item.
    public void updateItem(String oldname, String newname, int count,
            String pname, ButtonStyle buttonstyle, LabelStyle labelstyle)
            throws Exception {
        Cursor c = null;
        try {
            if (oldname == null || !oldname.equals(newname)) {
                c = db.rawQuery("select name from " + TABLE_ITEM
                        + " where name = ?", new String[] { newname });
                if (c.getCount() > 0)
                    throw new Exception("An item with that name already exists");
                c.close();
                c = null;
            }
            ContentValues values = new ContentValues();
            values.put("name", newname);
            values.put("count", count);
            values.put("pname", pname);
            values.put("buttonstyle", buttonstyle.ordinal());
            values.put("labelstyle", labelstyle.ordinal());
            if (oldname == null)
                db.insert(TABLE_ITEM, null, values);
            else
                db.update(TABLE_ITEM, values, "name = ?",
                        new String[] { oldname });
        } finally {
            if (c != null)
                c.close();
        }
    }

    // Reset all items to their initial values. (Currently this means 0, but
    // it won't later)
    public void resetAll() {
        ContentValues values = new ContentValues();
        values.put("count", 0);
        db.update(TABLE_ITEM, values, null, null);
    }

}
