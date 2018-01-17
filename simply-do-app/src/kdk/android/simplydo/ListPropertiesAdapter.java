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

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListPropertiesAdapter extends BaseAdapter
{
    private SimplyDoActivity context;
    private DataViewer dataViewer;
    
    public ListPropertiesAdapter(SimplyDoActivity context, DataViewer dataViewer)
    {
        this.context = context;
        this.dataViewer = dataViewer;
    }
    
    
    @Override
    public int getCount()
    {
        return dataViewer.getListData().size();
    }

    @Override
    public Object getItem(int position)
    {
        return dataViewer.getListData().get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        View rv = null;
        
        try
        {
            if(convertView == null)
            {
                rv = View.inflate(context, R.layout.list_entry, null);
            }
            else
            {
                rv = convertView;
            }

            TextView t1 = (TextView)rv.findViewById(R.id.text1);
            ListDesc listDesc = dataViewer.getListData().get(position);
            t1.setText(listDesc.getLabel());
            
            TextView t2 = (TextView)rv.findViewById(R.id.text2);
            t2.setText("("+ listDesc.getActiveItems() + "/" + listDesc.getTotalItems() + ")");
        }
        catch(Exception e)
        {
            Log.e(L.TAG, "Error in getView()", e);
        }

        return rv;
    }
    
}
