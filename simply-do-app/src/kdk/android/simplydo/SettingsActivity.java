/*
 * Copyright (C) 2010 Keith Kildare
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity
{
    private DecimalFormat twoDigits = new DecimalFormat("00");
    private File backupDirectory;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        
        // TODO offer list of backup locations as preference
        // at least suggested location and somewhere on ext. sdcard
        backupDirectory = new File(
                Environment.getExternalStorageDirectory(), 
                "/Android/data/kdk.android.simplydo/files/");
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference)
    {
        Log.v(L.TAG, "onPreferenceTreeClick() for key " + preference.getKey());
        
        if("backupDb".equals(preference.getKey()))        
        {
            // TODO dialog: This will backup you current SimplyDo database. You
            // can access and manage these backups through mass storage 
            // access to you device. Make backup now to the file ....
            backupDbSelected();
        }
        else if("restoreDb".equals(preference.getKey()))        
        {
            Intent restoreActivity = new Intent(getBaseContext(), RestoreActivity.class);
            startActivity(restoreActivity);

        }
        
        // the android docs give no clues as to what the returned bool does
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
    
    private void backupDbSelected()
    {
        Calendar cal = Calendar.getInstance();
        int seconds = (cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)) * 60 + cal.get(Calendar.SECOND);
        String filename = 
            "SimplyDo_" + 
            cal.get(Calendar.YEAR) + 
            twoDigits.format(cal.get(Calendar.MONTH) + 1) + 
            twoDigits.format(cal.get(Calendar.DAY_OF_MONTH)) + "_" + 
            seconds + ".simplydo";
        
        String state = Environment.getExternalStorageState();
        
        final File dbFile = getDatabasePath(DataManager.DATABASE_NAME);

        if (Environment.MEDIA_MOUNTED.equals(state)) 
        {
            // We can read and write the media
            backupDirectory.mkdirs();
            final File outFile = new File(backupDirectory, filename);
            
            Log.d(L.TAG, "Backing up to " + outFile.getAbsolutePath() + "\n" + dbFile.getAbsolutePath());
            
            copyDb(dbFile, outFile);
        }
        else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) 
        {
            // We can only read the media and we need to write
            Toast t = Toast.makeText(this, "Storage media is read-only", Toast.LENGTH_LONG);
            t.show();            
        }
        else 
        {
            // Something else is wrong.
            Toast t = Toast.makeText(this, "Unable to access storage media. Not mounted?", Toast.LENGTH_LONG);
            t.show();            
        }
    }
    
    
    private void copyDb(File src, File dst)
    {
        try
        {
            SimplyDoActivity.getInstance().getDataVeiwer().flush();
            
            fileCopy(src, dst);
            Toast t = Toast.makeText(this, "Backed up to " + dst.getName(), Toast.LENGTH_SHORT);
            t.show();
        }
        catch (IOException e)
        {
            Log.d(L.TAG, "Failed to copy files: " + e.getMessage(), e);
            Toast t = Toast.makeText(this, "Backup failed: " + e.getMessage(), Toast.LENGTH_SHORT);
            t.show();
        }
    }
    
    /**
     * Copies the specified source file to the destination file 
     * location.
     * @param src Source file
     * @param dst Destination file location
     * @throws IOException On error.
     */
    public static void fileCopy(File src, File dst) throws IOException
    {
        FileInputStream srcStream = new FileInputStream(src);
        FileOutputStream dstStream = new FileOutputStream(dst);
        
        byte[] buffer = new byte[1024];
        
        int bytesRead = srcStream.read(buffer);
        while(bytesRead > 0)
        {
            dstStream.write(buffer, 0, bytesRead);
            bytesRead = srcStream.read(buffer);
        }
        
        dstStream.close();
        srcStream.close();
    }
    

}
