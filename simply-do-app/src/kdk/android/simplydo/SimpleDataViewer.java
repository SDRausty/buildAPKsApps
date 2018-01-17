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

import android.util.Log;

public class SimpleDataViewer implements DataViewer
{
    private DataManager dataManager;
    
    private List<ItemDesc> itemData = new ArrayList<ItemDesc>();
    private List<ListDesc> listData = new ArrayList<ListDesc>();
    
    private ListDesc selectedList;

    
    public SimpleDataViewer(DataManager dataManager)
    {
        this.dataManager = dataManager;
    }
    
    
    @Override
    public List<ItemDesc> getItemData()
    {
        return itemData;
    }

    
    @Override
    public List<ListDesc> getListData()
    {
        return listData;
    }
    
    @Override
    public void setSelectedList(ListDesc selectedList)
    {
        this.selectedList = selectedList;
        if(selectedList == null)
        {
            itemData.clear();
        }
        else
        {
            fetchItems(selectedList.getId());
        }           
    }
    
    @Override
    public ListDesc getSelectedList()
    {
        return selectedList;
    }
    
    @Override
    public void fetchLists()
    {
        listData = dataManager.fetchLists();
    }
    
    @Override
    public ListDesc fetchList(int listId)
    {
        ListDesc rv = null;
        
        for(ListDesc list : listData)
        {
            if(list.getId() == listId)
            {
                rv = list;
                break;
            }
        }
        
        return rv;
    }
    
    @Override
    public void fetchItems(int listId)
    {
        itemData.clear();
        dataManager.fetchItems(listId, itemData);
    }
    
    @Override
    public void updateItemActiveness(ItemDesc item, boolean active)
    {
        int itemId = item.getId();
        dataManager.updateItemActiveness(itemId, active);
        if(selectedList != null)
        {
            fetchItems(selectedList.getId());
        }
        fetchLists();
    }
    
    @Override
    public void updateItemStarness(ItemDesc item, boolean star)
    {
        int itemId = item.getId();
        dataManager.updateItemStarness(itemId, star);
        if(selectedList != null)
        {
            fetchItems(selectedList.getId());
        }
        fetchLists();
    }
    
    @Override
    public void updateItemLabel(ItemDesc item, String newLabel)
    {
        int itemId = item.getId();
        dataManager.updateItemLabel(itemId, newLabel);
        if(selectedList != null)
        {
            fetchItems(selectedList.getId());
        }
    }
    
    @Override
    public void updateListLabel(int listId, String newLabel)
    {
        dataManager.updateListLabel(listId, newLabel);
        fetchLists();
    }
    
    @Override
    public void moveItem(ItemDesc item, int toListId)
    {
        int itemId = item.getId();
        dataManager.moveItem(itemId, toListId);
        if(selectedList != null)
        {
            fetchItems(selectedList.getId());
        }
        fetchLists();
    }
    
    @Override
    public void createList(String label)
    {
        dataManager.createList(label);
        fetchLists();
    }
    
    @Override
    public void createItem(String label)
    {
        int listId = selectedList.getId();
        dataManager.createItem(listId, label);
        fetchItems(listId);
    }
    
    @Override
    public void deleteInactive()
    {
        if(selectedList == null)
        {
            Log.e(L.TAG, "deleteInactive() called but no list is selected");
            return;
        }

        int listId = selectedList.getId();
        dataManager.deleteInactive(listId);
        fetchItems(listId);
        fetchLists();    
    }
    
    @Override
    public void deleteList(int listId)
    {
        dataManager.deleteList(listId);
        fetchLists();
    }
    
    @Override
    public void deleteItem(ItemDesc item)
    {
        int itemId = item.getId();
        dataManager.deleteItem(itemId);
        if(selectedList != null)
        {
            fetchItems(selectedList.getId());
        }
        fetchLists();
    }
    
    @Override
    public void invalidateCache()
    {
        // no cache to void
    }
    
    @Override
    public void flush()
    {
        // Do nothing
    }
    
    @Override
    public void close()
    {
        // Do nothing
    }

}
