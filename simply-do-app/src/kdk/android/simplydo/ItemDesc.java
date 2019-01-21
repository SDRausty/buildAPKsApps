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

public class ItemDesc
{
	private volatile int id;
	private String label;
	private boolean active;
    private boolean star;
    private boolean sorted = false;
	
	public ItemDesc(int id, String label, boolean active, boolean star)
	{
		this.id = id;
		this.label = label;
		this.active = active;
        this.star = star;
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

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

    public boolean isStar()
    {
        return star;
    }

    public void setStar(boolean star)
    {
        this.star = star;
    }
	
    public boolean isSorted()
    {
        return sorted;
    }
    
    public void setSorted(boolean sorted)
    {
        this.sorted = sorted;
    }
	
}
