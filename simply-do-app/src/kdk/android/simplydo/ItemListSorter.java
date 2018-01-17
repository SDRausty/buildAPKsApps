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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.util.Log;

public class ItemListSorter
{
    public static final String PREF_NONE = "none";
    public static final String PREF_STARRED = "starred";
    public static final String PREF_ACTIVE_STARRED = "activityStarred";
    
    private static final int NONE = 0;
    private static final int STARRED = 1;
    private static final int ACTIVE_STARRED = 2;

    private int sortingMode;
    
    private Comparator<ItemDesc> idCompare;
    private Comparator<ItemDesc> starredCompare;
    private Comparator<ItemDesc> activeStarredCompare;

    
    public ItemListSorter()
    {
        idCompare = new Comparator<ItemDesc>() {
            @Override
            public int compare(ItemDesc object1, ItemDesc object2)
            {
                return object2.getId() - object1.getId();
            }
        };
        
        starredCompare = new Comparator<ItemDesc>() {
            @Override
            public int compare(ItemDesc o1, ItemDesc o2)
            {
                int w1 = o1.isStar()?1:0;
                int w2 = o2.isStar()?1:0;
                if(w2 == w1)
                {
                    return o1.getLabel().compareToIgnoreCase(o2.getLabel());
                }
                else
                {
                    return w2 - w1;
                }
            }
        };
        
        activeStarredCompare = new Comparator<ItemDesc>() {
            @Override
            public int compare(ItemDesc o1, ItemDesc o2)
            {
                int w1 = (o1.isStar()?1:0) + (o1.isActive()?0:-2);
                int w2 = (o2.isStar()?1:0) + (o2.isActive()?0:-2);
                if(w2 == w1)
                {
                    return o1.getLabel().compareToIgnoreCase(o2.getLabel());
                }
                else
                {
                    return w2 - w1;
                }
            }
        };
    }
    
    public void setSortingMode(String mode)
    {
        if(PREF_NONE.equals(mode))
        {
            sortingMode = NONE;
        }
        else if(PREF_STARRED.equals(mode))
        {
            sortingMode = STARRED;
        }
        else if(PREF_ACTIVE_STARRED.equals(mode))
        {
            sortingMode = ACTIVE_STARRED;
        }
        else
        {
            sortingMode = NONE;
            Log.w(L.TAG, "Unknown item sorting mode " + mode);
        }
    }
    
    
    public void markEditUpdate(ItemDesc item)
    {
        switch(sortingMode)
        {
        case ACTIVE_STARRED:
        case STARRED:
            item.setSorted(false);
            break;
        }        
    }
    
    
    public void markActivityUpdate(ItemDesc item)
    {
        switch(sortingMode)
        {
        case ACTIVE_STARRED:
            item.setSorted(false);
            break;
        }        
    }
    
    
    public void markStarredUpdate(ItemDesc item)
    {
        switch(sortingMode)
        {
        case ACTIVE_STARRED:
        case STARRED:
            item.setSorted(false);
            break;
        }        
    }
    
    
    public void sort(List<ItemDesc> list)
    {
        if(list == null)
        {
            return;
        }
        
        switch(sortingMode)
        {
        default:
            Log.w(L.TAG, "Unknown item sorting enum " + sortingMode);
            // fall through
        case NONE:
            // actually sorted by db id
            Collections.sort(list, idCompare);
            break;
        case STARRED:
            Collections.sort(list, starredCompare);
            break;
        case ACTIVE_STARRED:
            Collections.sort(list, activeStarredCompare);
            break;
        }
        
        markAsSorted(list);
    }

    private void markAsSorted(List<ItemDesc> items)
    {
        for(ItemDesc item : items)
        {
            item.setSorted(true);
        }
    }
}
