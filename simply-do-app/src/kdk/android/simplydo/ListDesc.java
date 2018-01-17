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

import java.io.Serializable;

public class ListDesc implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    
    private int id;
	private String label;
	private int activeItems;
	private int totalItems;
	
    public ListDesc()
    {
        // Do nothing
    }
    
    
	public ListDesc(int id, String label, int activeItems, int totalItems)
	{
		this.id = id;
		this.label = label;
		this.activeItems = activeItems;
		this.totalItems = totalItems;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}
	
	public int getActiveItems()
	{
		return activeItems;
	}

	public void setActiveItems(int inactiveItems)
	{
		this.activeItems = inactiveItems;
	}

	public int getTotalItems()
	{
		return totalItems;
	}

	public void setTotalItems(int totalItems)
	{
		this.totalItems = totalItems;
	}
	
	
	
}
