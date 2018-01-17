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

import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageSwitcher;
import android.widget.TextView;

public class ItemPropertiesAdapter extends BaseAdapter
{
    private SimplyDoActivity context;
    private DataViewer dataViewer;

    
    public ItemPropertiesAdapter(SimplyDoActivity context, DataViewer dataViewer)
    {
        this.context = context;
        this.dataViewer = dataViewer;
    }
    
    
    @Override
    public int getCount()
    {
        //Log.v(L.TAG, "ItemPropertiesAdapter.getCount() called");
        return dataViewer.getItemData().size();
    }

    @Override
    public Object getItem(int position)
    {
        //Log.v(L.TAG, "ItemPropertiesAdapter.getItem() called");
        return dataViewer.getItemData().get(position);
    }

    @Override
    public long getItemId(int position)
    {
        //Log.v(L.TAG, "ItemPropertiesAdapter.getItemId() called");
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        //Log.v(L.TAG, "ItemPropertiesAdapter.getView() called");
        
        View rv = null;
        
        try
        {
            if(convertView == null)
            {
                rv = View.inflate(context, R.layout.item_entry, null);
            }
            else
            {
                rv = convertView;
            }
            
            ItemDesc it = dataViewer.getItemData().get(position);
            TextView t1 = (TextView)rv.findViewById(R.id.text1);
            t1.setText(it.getLabel());
            t1.setTextAppearance(context, it.isActive()?R.style.ActiveText:R.style.InactiveText);
            if(it.isActive())
            {
                t1.setPaintFlags(t1.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);                
            }
            else
            {
                t1.setPaintFlags(t1.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            
            ImageSwitcher starSwitch = (ImageSwitcher)rv.findViewById(R.id.StarSwitcher);
            starSwitch.setVisibility(it.isStar()?View.VISIBLE:View.INVISIBLE);
            starSwitch.setDisplayedChild(it.isActive()?0:1);
            
            View sortedMarker = (View)rv.findViewById(R.id.SortedMarker);
            if(it.isSorted())
            {
                sortedMarker.setVisibility(View.INVISIBLE);
            }
            else
            {
                sortedMarker.setVisibility(View.VISIBLE);
            }

        }
        catch(Exception e)
        {
            Log.e(L.TAG, "Error in getView()", e);
        }

        return rv;
    }

}
