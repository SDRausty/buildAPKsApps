/*
 * Copyright (C) 2011 Keith Kildare
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

import java.io.File;
import java.io.FilenameFilter;
import java.util.Comparator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class RestoreActivity extends ListActivity 
{
    private static final int DIALOG_RESTORE_WARN = 300;
    private static final String EXTENSION = ".simplydo";

    private ArrayAdapter<NameOnlyFile> adapter;
    private AlertDialog.Builder restoreWarningBuilder;
    private NameOnlyFile restoreFile;
    private FilenameFilter restoreFilenameFilter;
    private Comparator<NameOnlyFile> comparator;
    
    public RestoreActivity()
    {
        restoreFilenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename)
            {
                return filename.endsWith(EXTENSION);
            }
        };
        
        comparator = new Comparator<NameOnlyFile>() {
            @Override
            public int compare(NameOnlyFile object1, NameOnlyFile object2)
            {
                return object2.toString().compareTo(object1.toString());
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        Log.v(L.TAG, "RestoreActivity.onCreate() called");

        adapter = new ArrayAdapter<NameOnlyFile>(this, R.layout.restore_entry, R.id.RestoreName);
        
        refresh();
        
        setListAdapter(adapter);
        
        restoreWarningBuilder = new AlertDialog.Builder(this);
        restoreWarningBuilder.setMessage("This will overwrite all the existing lists and items. Continue?")
               .setCancelable(true)
               .setTitle("Restore database")
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       doRestore();
                       dialog.cancel();
                   }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                   }
               });

    }
    
    
    @Override
    protected Dialog onCreateDialog(int id)
    {
        Log.v(L.TAG, "RestoreActivity.onCreateDialog() called");
                
        switch(id)
        {
            case DIALOG_RESTORE_WARN:
            {
                AlertDialog dialog = restoreWarningBuilder.create();
                return dialog;
            }
        }
        
        return super.onCreateDialog(id);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        
        Log.i(L.TAG, "RestoreActivity.onListItemClick()");

        restoreFile = adapter.getItem(position);
        
        try
        {
            // test restore file
            SQLiteDatabase db = SQLiteDatabase.openDatabase(
                    restoreFile.file.getPath(), null, SQLiteDatabase.OPEN_READONLY);
            db.close();
            
            // Dialog: This will overwrite the existing items, continue?
            showDialog(DIALOG_RESTORE_WARN);
        }
        catch(Exception e)
        {
            Log.e(L.TAG, "Error testing user selected restore DB", e);
            Toast t = Toast.makeText(this, "That database was invalid or corrupted! Not restored.", Toast.LENGTH_LONG);
            t.show();
        }
        
    }

    
    private void refresh()
    {
        File backupDirectory = new File(
                Environment.getExternalStorageDirectory(), 
                "/Android/data/kdk.android.simplydo/files/");
        File[] files = backupDirectory.listFiles(restoreFilenameFilter);
        adapter.clear();
        for(File f : files)
        {
            adapter.add(new NameOnlyFile(f));
        }
        adapter.sort(comparator);
        adapter.notifyDataSetChanged();
    }
    
    
    private void doRestore()
    {
        Log.i(L.TAG, "RestoreActivity.doRestore() called");
        
        // Flush the database update queue        
        SimplyDoActivity.getInstance().getDataVeiwer().flush();
        
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) 
        {
            Toast.makeText(
                    this, 
                    "Media not mount or read-only", 
                    Toast.LENGTH_LONG
                    ).show();
            return;
        }        
        
        // backup old file
        File dbFile = getDatabasePath(DataManager.DATABASE_NAME);
        File dbBakFile = getDatabasePath(DataManager.DATABASE_NAME + ".bak");
        boolean moved = dbFile.renameTo(dbBakFile);
        if(!moved)
        {
            Toast.makeText(
                    this, 
                    "Unable to move old database out of the way.", 
                    Toast.LENGTH_LONG
                    ).show();
            return;
        }

        try
        {
            // copy new file into place
            SettingsActivity.fileCopy(restoreFile.file, dbFile);
            
            // delete backup
            dbBakFile.delete();
        }
        catch (Exception e)
        {
            // put the old database back
            dbFile.delete();
            dbBakFile.renameTo(dbFile);
            
            Log.e(L.TAG, "Failed to copy restore database into place", e);
            
            Toast.makeText(
                    this, 
                    "Failed to copy restore database into place. Nothing restored.", 
                    Toast.LENGTH_LONG
                    ).show();
            return;
        }
        
        SimplyDoActivity.getInstance().getDataVeiwer().invalidateCache();
        SimplyDoActivity.getInstance().cacheInvalidated();
        
        Toast.makeText(
                this, 
                "Database restored.", 
                Toast.LENGTH_LONG
                ).show();
        
        finish();
    }

    
    private static class NameOnlyFile
    {
        public File file;
        
        public NameOnlyFile(File f)
        {
            file = f;
        }

        @Override
        public String toString()
        {
            String name = file.getName();
            return name.substring(0, name.length() - EXTENSION.length());
        }
    }
}
