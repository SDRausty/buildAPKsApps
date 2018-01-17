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

import java.util.List;

public interface DataViewer
{
    public List<ItemDesc> getItemData();
    public List<ListDesc> getListData();
    public void setSelectedList(ListDesc selectedList);
    public ListDesc getSelectedList();
    public void fetchLists();
    public ListDesc fetchList(int listId);
    public void fetchItems(int listId);
    public void updateItemActiveness(ItemDesc item, boolean active);
    public void updateItemStarness(ItemDesc item, boolean star);
    public void updateItemLabel(ItemDesc item, String newLabel);
    public void updateListLabel(int listId, String newLabel);
    public void moveItem(ItemDesc item, int toListId);
    public void createList(String label);
    public void createItem(String label);
    
    /**
     * Deletes inactive items from the current list. 
     */
    public void deleteInactive();
    
    public void deleteList(int listId);
    public void deleteItem(ItemDesc item);
    public void flush();
    public void invalidateCache();
    public void close();
}
