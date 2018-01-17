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
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

/**
 * A cache of the database data which does the actual database calls lazily on
 * a separate thread. On my device a database write take 300ms-450ms which is
 * too log to hang onto the UI thread for.
 */
public class CachingDataViewer implements DataViewer
{
    private DataManager dataManager;
    
    private List<ItemDesc> itemData = new LinkedList<ItemDesc>();
    private List<ListDesc> listData = new ArrayList<ListDesc>();
    
    private Thread dbUpdateThread;
    private Object viewerLock = new Object();
    private LinkedList<ViewerTask> taskQueue = new LinkedList<ViewerTask>();

    // declared volatile since it is accessed in the db thread
    // without holding the viewerLock
    private volatile boolean running = false;
    
    private boolean interruptRequire = false;
    private ListDesc selectedList;

    
    public CachingDataViewer(DataManager dataManager)
    {
        this.dataManager = dataManager;
        
        dbUpdateThread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                dbUpdateLoop();
            }
        }, "DB Update");
    }
    
    
    public void start()
    {
        running = true;
        // start task queue
        
        dbUpdateThread.start();
    }
    
    @Override
    public void invalidateCache()
    {
        setSelectedList(null);
        fetchLists();
    }
    
    @Override
    public void flush()
    {
        flushTasks();
    }
    
    @Override
    public void close()
    {
        Log.v(L.TAG, "CachingDataView.close(): Entered");
        synchronized (viewerLock)
        {
            flushTasksNoLock();
            
            running = false;
            
            if(interruptRequire)
            {
                Log.d(L.TAG, "CachingDataView.close(): Close interrupt required");
                dbUpdateThread.interrupt();
            }
        }        
        
        try
        {
            dbUpdateThread.join();
        }
        catch (InterruptedException e)
        {
            Log.d(L.TAG, "CachingDataView.close(): shutdown join interrupted", e);
        }
        
        Log.v(L.TAG, "CachingDataView.close(): Exit");
    }


    @Override
    public List<ItemDesc> getItemData()
    {
        synchronized (viewerLock)
        {
            return itemData;
        }
    }

    
    @Override
    public List<ListDesc> getListData()
    {
        synchronized (viewerLock)
        {
            return listData;
        }
    }
    
    
    @Override
    public ListDesc getSelectedList()
    {
        return selectedList;
    }


    @Override
    public void setSelectedList(ListDesc selectedList)
    {
        flushTasks();
        
        // this is ok since this thread is the only source
        // of task and we've just flushed the task queue
        itemData.clear();
        if(selectedList != null)
        {
            dataManager.fetchItems(selectedList.getId(), itemData);
        }
        
        this.selectedList = selectedList;
    }


    @Override
    public void fetchLists()
    {
        Log.v(L.TAG, "CachingDataView.fetchLists(): Entered");
        ViewerTask task = new ViewerTask();
        task.taskId = ViewerTask.FETCH_LISTS;
        doTaskAndWait(task);
        Log.v(L.TAG, "CachingDataView.fetchLists(): Exited");
    }
    
    public ListDesc fetchList(int listId)
    {
        ListDesc rv = null;
        
        synchronized (viewerLock)
        {
            for(ListDesc list : listData)
            {
                if(listId == list.getId())
                {
                    rv = list;
                    break;
                }
            }
        }

        return rv;
    }

    
    @Override
    public void fetchItems(int listId)
    {
        Log.v(L.TAG, "CachingDataViewer.fetchItems(): Entered");
        ViewerTask task = new ViewerTask();
        task.taskId = ViewerTask.FETCH_ITEMS;
        task.args = new Object[]{listId};
        doTaskAndWait(task);
        Log.v(L.TAG, "CachingDataViewer.fetchItems(): Exited");
    }
    

    @Override
    public void createItem(String label)
    {
        if(selectedList == null)
        {
            Log.e(L.TAG, "CachingDataViewer.createItem(): called but no list is selected");
            return;
        }
        
        int listId = selectedList.getId();
        
        ItemDesc newItem = new ItemDesc(-1, label, true, false);
        
        ViewerTask createTask = new ViewerTask();
        createTask.taskId = ViewerTask.CREATE_ITEM;
        createTask.args = new Object[]{listId, label, newItem};
        
        synchronized (viewerLock)
        {
            // queue fetch lists
            taskQueue.add(createTask);
            viewerLock.notifyAll();
            
            itemData.add(0, newItem);
            
            updateListStats();
        }
    }


    @Override
    public void createList(String label)
    {
        ViewerTask createTask = new ViewerTask();
        createTask.taskId = ViewerTask.CREATE_LIST;
        createTask.args = new Object[]{label};

        ViewerTask fetchTask = new ViewerTask();
        fetchTask.taskId = ViewerTask.FETCH_LISTS;
        
        synchronized (viewerLock)
        {
            // queue fetch lists
            taskQueue.add(createTask);
            taskQueue.add(fetchTask);
            viewerLock.notifyAll();
            
            flushTasksNoLock();
        }
    }


    @Override
    public void deleteInactive()
    {
        if(selectedList == null)
        {
            Log.e(L.TAG, "CachingDataViewer.deleteInactive() called but no list is selected");
            return;
        }
        
        ViewerTask task = new ViewerTask();
        task.taskId = ViewerTask.DELETE_INACTIVE;
        task.args = new Object[]{selectedList.getId()};

        synchronized (viewerLock)
        {
            // queue fetch lists
            taskQueue.add(task);
            viewerLock.notifyAll();
            
            // update items data
            List<ItemDesc> toDelete = new ArrayList<ItemDesc>();
            for(ItemDesc i : itemData)
            {
                if(!i.isActive())
                {
                    toDelete.add(i);
                }
            }
            itemData.removeAll(toDelete);
            
            updateListStats();
        }
    }


    @Override
    public void deleteItem(ItemDesc item)
    {
        itemIdBarrier(item);
        
        int itemId = item.getId();        
        
        ViewerTask task = new ViewerTask();
        task.taskId = ViewerTask.DELETE_ITEM;
        task.args = new Object[]{itemId};
        
        synchronized (viewerLock)
        {
            // queue fetch lists
            taskQueue.add(task);
            viewerLock.notifyAll();
            
            // update items data
            itemData.remove(item);
            updateListStats();
        }
    }


    @Override
    public void deleteList(int listId)
    {
        ViewerTask task = new ViewerTask();
        task.taskId = ViewerTask.DELETE_LIST;
        task.args = new Object[]{listId};
        
        synchronized (viewerLock)
        {
            // queue fetch lists
            taskQueue.add(task);
            viewerLock.notifyAll();
            
            // update lists data
            ListDesc delete = null;
            for(ListDesc list : listData)
            {
                if(listId == list.getId())
                {
                    delete = list;
                    break;
                }
            }
            listData.remove(delete);
        }
        
    }


    @Override
    public void updateItemLabel(ItemDesc item, String newLabel)
    {
        itemIdBarrier(item);
        int itemId = item.getId();
        
        ViewerTask task = new ViewerTask();
        task.taskId = ViewerTask.UPDATE_ITEM_LABEL;
        task.args = new Object[]{itemId, newLabel};
        
        synchronized (viewerLock)
        {
            // queue fetch lists
            taskQueue.add(task);
            viewerLock.notifyAll();
            
            // update items data
            item.setLabel(newLabel);
        }        
    }


    @Override
    public void moveItem(ItemDesc item, int toListId)
    {
        itemIdBarrier(item);
        int itemId = item.getId();

        ViewerTask task = new ViewerTask();
        task.taskId = ViewerTask.MOVE_ITEM;
        task.args = new Object[]{itemId, toListId};
        
        synchronized (viewerLock)
        {
            // queue fetch lists
            taskQueue.add(task);
            viewerLock.notifyAll();
            
            // remove from items data
            ItemDesc itemInList = null;
            for(ItemDesc i : itemData)
            {
                if(itemId == i.getId())
                {
                    itemInList = i;
                    break;
                }
            }
            if(itemInList != null)
            {
                itemData.remove(itemInList);
                updateListStats();
                ListDesc toList = findList(toListId);
                toList.setTotalItems(toList.getTotalItems() + 1);
                if(itemInList.isActive())
                {
                    toList.setActiveItems(toList.getActiveItems() + 1);
                }
            }
            else
            {
                Log.w(L.TAG, "CachingDataViewer.moveItem(): Didn't find item in current item data");
            }
        }        
    }


    @Override
    public void updateListLabel(int listId, String newLabel)
    {
        ViewerTask task = new ViewerTask();
        task.taskId = ViewerTask.UPDATE_LIST_LABEL;
        task.args = new Object[]{listId, newLabel};
        
        synchronized (viewerLock)
        {
            // queue fetch lists
            taskQueue.add(task);
            viewerLock.notifyAll();
            
            // update items data
            for(ListDesc i : listData)
            {
                if(listId == i.getId())
                {
                    i.setLabel(newLabel);
                    break;
                }
            }
        }        
    }
    

    @Override
    public void updateItemActiveness(ItemDesc item, boolean active)
    {
        itemIdBarrier(item);
        int itemId = item.getId();
        
        ViewerTask task = new ViewerTask();
        task.taskId = ViewerTask.UPDATE_ACTIVENESS;
        task.args = new Object[]{itemId, active};
        
        synchronized (viewerLock)
        {
            // queue fetch lists
            taskQueue.add(task);
            viewerLock.notifyAll();
            
            // update items data
            item.setActive(active);
            
            // update lists data
            if(selectedList != null)
            {
                int activeItems = selectedList.getActiveItems();
                activeItems += active?1:-1;
                selectedList.setActiveItems(activeItems);
            }
        }
    }
    

    @Override
    public void updateItemStarness(ItemDesc item, boolean star)
    {
        itemIdBarrier(item);
        int itemId = item.getId();
        
        ViewerTask task = new ViewerTask();
        task.taskId = ViewerTask.UPDATE_STARNESS;
        task.args = new Object[]{itemId, star};
        
        synchronized (viewerLock)
        {
            // queue fetch lists
            taskQueue.add(task);
            viewerLock.notifyAll();
            
            // update items data
            item.setStar(star);
        }
    }
    
    
    private void itemIdBarrier(ItemDesc item)
    {
        if(item.getId() == -1)
        {
            Log.v(L.TAG, "CachingDataViewer.itemIdBarrier(): Used");
            flushTasks();
            
            if(item.getId() == -1)
            {
                Log.e(L.TAG, "CachingDataViewer.itemIdBarrier(): failed!");
            }
        }
    }
    
    
    private void flushTasksNoLock()
    {
        while(taskQueue.size() != 0)
        {
            try
            {
                viewerLock.wait(200);
            }
            catch (InterruptedException e)
            {
                Log.e(L.TAG, "CachingDataViewer.flushTasksNoLock(): Exception waiting for flushTasksNoLock()", e);
            }
        }
    }
    
    private void flushTasks()
    {
        synchronized (viewerLock)
        {
            flushTasksNoLock();
        }
    }
    
    private void updateListStats()
    {
        updateListStats(selectedList);
    }
    
    private ListDesc findList(int listId)
    {
        ListDesc rvList = null;
        for(ListDesc list : listData)
        {
            if(list.getId() == listId)
            {
                rvList = list;
                break;
            }
        }

        return rvList;
    }
    
    
    private void updateListStats(ListDesc listDesc)
    {
        listDesc.setTotalItems(itemData.size());
        int active = 0;
        for(ItemDesc item : itemData)
        {
            if(item.isActive())
            {
                active++;
            }
        }
        listDesc.setActiveItems(active);
    }
    
    
    private void doTaskAndWait(ViewerTask task)
    {
        synchronized (viewerLock)
        {
            taskQueue.add(task);
            viewerLock.notifyAll();
            
            try
            {
                while(!task.done)
                {
                    viewerLock.wait();
                }
            }
            catch(InterruptedException e)
            {
                Log.e(L.TAG, "CachingDataViewer.doTaskAndWait(): Error waiting for task", e);
            }
        }
    }
    
    
    private void dbUpdateLoop()
    {
        Log.v(L.TAG, "CachingDataViewer.dbUpdateLoop(): Entered");
        while(running)
        {
            try
            {
                // get task
                ViewerTask task;
                synchronized (viewerLock)
                {
                    while(taskQueue.size() == 0)
                    {
                        interruptRequire = true;
                        viewerLock.wait();
                        interruptRequire = false;
                    }
                    task = taskQueue.peek();
                }
                
                boolean doNotify = true;
                try
                {
                    // do it
                    switch(task.taskId)
                    {
                    case ViewerTask.FETCH_LISTS:
                    {
                        List<ListDesc> lists = dataManager.fetchLists();
                        synchronized (viewerLock)
                        {
                            listData = lists;
                            task.done = true;
                            viewerLock.notifyAll();
                        }
                        doNotify = false;
                        break;
                    }
                    case ViewerTask.FETCH_ITEMS:
                    {
                        synchronized (viewerLock)
                        {
                            itemData.clear();
                            dataManager.fetchItems((Integer)task.args[0], itemData);
                            task.done = true;
                            viewerLock.notifyAll();
                        }
                        doNotify = false;
                        break;
                    }
                    case ViewerTask.UPDATE_ACTIVENESS:
                    {
                        dataManager.updateItemActiveness((Integer)task.args[0], (Boolean)task.args[1]);
                        task.done = true;
                        break;
                    }
                    case ViewerTask.UPDATE_STARNESS:
                    {
                        dataManager.updateItemStarness((Integer)task.args[0], (Boolean)task.args[1]);
                        task.done = true;
                        break;
                    }
                    case ViewerTask.UPDATE_ITEM_LABEL:
                    {
                        dataManager.updateItemLabel((Integer)task.args[0], (String)task.args[1]);
                        task.done = true;
                        break;
                    }
                    case ViewerTask.UPDATE_LIST_LABEL:
                    {
                        dataManager.updateListLabel((Integer)task.args[0], (String)task.args[1]);
                        task.done = true;
                        break;
                    }
                    case ViewerTask.MOVE_ITEM:
                    {
                        dataManager.moveItem((Integer)task.args[0], (Integer)task.args[1]);
                        task.done = true;
                        break;
                    }
                    case ViewerTask.DELETE_LIST:
                    {
                        dataManager.deleteList((Integer)task.args[0]);
                        task.done = true;
                        break;
                    }
                    case ViewerTask.DELETE_ITEM:
                    {
                        dataManager.deleteItem((Integer)task.args[0]);
                        task.done = true;
                        break;
                    }
                    case ViewerTask.DELETE_INACTIVE:
                    {
                        dataManager.deleteInactive((Integer)task.args[0]);
                        task.done = true;
                        break;
                    }
                    case ViewerTask.CREATE_LIST:
                    {
                        dataManager.createList((String)task.args[0]);
                        task.done = true;
                        break;
                    }
                    case ViewerTask.CREATE_ITEM:
                    {
                        ItemDesc item = (ItemDesc)task.args[2];
                        int id = dataManager.createItem((Integer)task.args[0], (String)task.args[1]);
                        item.setId(id);
                        task.done = true;
                        break;
                    }
                    default:
                        Log.w(L.TAG, "CachingDataViewer.dbUpdateLoop(): Unknown task enumeration " + task.taskId);
                    }
                }
                finally
                {
                    synchronized (viewerLock)
                    {
                        taskQueue.remove();                
                        if(doNotify)
                        {
                            viewerLock.notifyAll();
                        }
                    }
                }                
            }
            catch(InterruptedException e)
            {
                if(running)
                {
                    Log.e(L.TAG, "CachingDataViewer.dbUpdateLoop(): Interrupted in DB update loop", e);
                }
                else
                {
                    Log.d(L.TAG, "CachingDataViewer.dbUpdateLoop(): interrupt exit");
                }
            }
            catch(Exception e)
            {
                Log.e(L.TAG, "CachingDataViewer.dbUpdateLoop(): Exception in DB update loop", e);
            }
        }
        Log.v(L.TAG, "CachingDataViewer.dbUpdateLoop(): Exit");
    }
    
    
    private static class ViewerTask
    {
        private static final int FETCH_LISTS = 0;
        private static final int FETCH_ITEMS = 1;
        private static final int UPDATE_ACTIVENESS = 2;
        private static final int UPDATE_ITEM_LABEL = 3;
        private static final int UPDATE_LIST_LABEL = 4;
        private static final int DELETE_LIST = 5;
        private static final int DELETE_ITEM = 6;
        private static final int DELETE_INACTIVE = 7;
        private static final int CREATE_LIST = 8;
        private static final int CREATE_ITEM = 9;
        private static final int UPDATE_STARNESS = 10;
        private static final int MOVE_ITEM = 11;
        
        private int taskId;
        private Object[] args;
        private boolean done = false;
    }

}
