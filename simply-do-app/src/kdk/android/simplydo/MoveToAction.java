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

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

public class MoveToAction
{
    private Context context;
    private DataViewer dataViewer;
    private ListPropertiesAdapter listPropertiesAdapter;
    private ItemPropertiesAdapter itemPropertiesAdapter;
    
    private ArrayAdapter<String> aa;
    private Button okButton;
    private DialogInterface.OnClickListener itemClickedListener;
    private DialogInterface.OnClickListener listSelectedListener;
    private DialogInterface.OnClickListener cancelClickedListener;
    private Integer selectedItem = null;
    private List<ListDesc> dataViewList = new ArrayList<ListDesc>();
    private ItemDesc ctxItem;
    
    public MoveToAction(
            Context context, 
            DataViewer dataViewer, 
            ListPropertiesAdapter listPropertiesAdapter, 
            ItemPropertiesAdapter itemPropertiesAdapter)
    {
        this.context = context;
        this.dataViewer = dataViewer;
        this.listPropertiesAdapter = listPropertiesAdapter;
        this.itemPropertiesAdapter = itemPropertiesAdapter;
        
        itemClickedListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                itemClicked(dialog, which);
            }
        };
        listSelectedListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                listSelected();
            }
        };
        cancelClickedListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.v(L.TAG, "MoveToAction.cancelClickedListener: Cancel");
                endDialog();
            }
        };
    }
    
    
    public Dialog createDialog()
    {
        aa = new ArrayAdapter<String>(context, android.R.layout.select_dialog_singlechoice);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Move To");
        builder.setSingleChoiceItems(aa, -1, itemClickedListener);
        builder.setPositiveButton("OK", listSelectedListener);
        builder.setNegativeButton("Cancel", cancelClickedListener);
        builder.setCancelable(true);
        
        AlertDialog alertDialog = builder.create();
        return alertDialog;
    }
    
    
    public void prepareDialog(Dialog dialog, ItemDesc ctxItem)
    {
        if(aa == null)
        {
            Log.e(L.TAG, "MoveToAction.prepareDialog() called before createDialog()");
            return;
        }
        

        ListDesc selectedListDesc = dataViewer.getSelectedList();
        dataViewList.clear();
        dataViewList.addAll(dataViewer.getListData());
        dataViewList.remove(selectedListDesc);
        
        aa.clear();
        int selectedId = selectedListDesc.getId();
        for(int i = 0; i < dataViewList.size(); i++)
        {
            ListDesc listDesc = dataViewList.get(i);
            if(listDesc.getId() != selectedId)
            {
                aa.add(listDesc.getLabel());
            }
        }
        
        aa.notifyDataSetChanged();
        
        AlertDialog alertDialog = (AlertDialog)dialog;
        okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        okButton.setEnabled(selectedItem != null);
        
        this.ctxItem = ctxItem;
    }
    
    
    private void itemClicked(DialogInterface dialog, int which)
    {
        Log.d(L.TAG, "MoveToAction.itemClicked(): Selected " + which);

        if(!okButton.isEnabled())
        {
            okButton.setEnabled(true);
        }
        
        selectedItem = which;
    }
    
    
    private void listSelected()
    {
        if(selectedItem == null)
        {
            Log.e(L.TAG, "MoveToAction.listSelected(): called without a selected item");
            return;
        }
        Log.d(L.TAG, "MoveToAction.listSelected(): called");
        
        ListDesc listDesc = dataViewList.get(selectedItem);
        dataViewer.moveItem(ctxItem, listDesc.getId());
        itemPropertiesAdapter.notifyDataSetChanged();
        listPropertiesAdapter.notifyDataSetChanged();
        
        Toast t = Toast.makeText(context, "Item " + ctxItem.getLabel() + " moved to " + listDesc.getLabel(), Toast.LENGTH_SHORT);
        t.show();
        
        endDialog();
    }
    
    
    private void endDialog()
    {
        // make sure left overs from prepareDialog don't hold unneeded objects
        ctxItem = null;
        okButton = null;
        dataViewList.clear();
        aa.clear();
    }
}
