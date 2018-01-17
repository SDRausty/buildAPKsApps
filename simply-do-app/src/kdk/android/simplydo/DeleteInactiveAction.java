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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class DeleteInactiveAction
{
    private Activity activity;
    private DataViewer dataViewer;
    private ListPropertiesAdapter listPropertiesAdapter;
    private ItemPropertiesAdapter itemPropertiesAdapter;
    
    private AlertDialog.Builder dialogBuilder;

    public DeleteInactiveAction(
            Activity activity, 
            DataViewer dataViewer,
            ListPropertiesAdapter listPropertiesAdapter, 
            ItemPropertiesAdapter itemPropertiesAdapter)
    {
        this.activity = activity;
        this.dataViewer = dataViewer;
        this.listPropertiesAdapter = listPropertiesAdapter;
        this.itemPropertiesAdapter = itemPropertiesAdapter;
        
        dialogBuilder = new AlertDialog.Builder(this.activity);
        dialogBuilder.setMessage("Are you sure you want to delete all inactive items in this list?")
               .setCancelable(true)
               .setTitle("Delete Inactive?")
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       deleteInactive();
                   }
               })
               .setNegativeButton("No", null);
    }

    public void deleteInactive(int dialogId)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity.getBaseContext());
        boolean confirmDeleteInactive = prefs.getBoolean("confirmDeleteInactive", true);
        
        if(confirmDeleteInactive)
        {
            activity.showDialog(dialogId);
        }
        else
        {
            deleteInactive();
        }
    }
    
    public Dialog createDialog()
    {
        return dialogBuilder.create();
    }
    
    private void deleteInactive()
    {
        dataViewer.deleteInactive();
        itemPropertiesAdapter.notifyDataSetChanged();
        listPropertiesAdapter.notifyDataSetChanged();
    }
}
