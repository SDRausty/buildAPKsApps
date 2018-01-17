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

public class ListListSorter
{
    public static final String PREF_NONE = "none";
    public static final String PREF_ALPHA = "alphabetical";
    
    private static final int NONE = 0;
    private static final int ALPHA = 1;
    
    private int sortingMode;
    
    private Comparator<ListDesc> idCompare;
    private Comparator<ListDesc> alphaCompare;
    
    
    public ListListSorter()
    {
        idCompare = new Comparator<ListDesc>() {
            @Override
            public int compare(ListDesc object1, ListDesc object2)
            {
                return object2.getId() - object1.getId();
            }
        };
        alphaCompare = new Comparator<ListDesc>() {
            @Override
            public int compare(ListDesc object1, ListDesc object2)
            {
                return object1.getLabel().compareToIgnoreCase(object2.getLabel());
            }
        };
    }
    
    
    public void setSortingMode(String mode)
    {
        if(PREF_NONE.equals(mode))
        {
            sortingMode = NONE;
        }
        else if(PREF_ALPHA.equals(mode))
        {
            sortingMode = ALPHA;
        }
        else
        {
            sortingMode = NONE;
            Log.w(L.TAG, "Unknown list sorting mode " + mode);
        }
    }
    
    
    public void sort(List<ListDesc> list)
    {
        switch(sortingMode)
        {
        default:
            Log.w(L.TAG, "Unknown list sorting enum " + sortingMode);
            // fall through
        case NONE:
            // actually sorted by db id
            Collections.sort(list, idCompare);
            break;
        case ALPHA:
            Collections.sort(list, alphaCompare);
            break;
        }
    }
}
