// GameMaster Dice
// Copyright (C) 2014 David Pflug
// Copyright (C) 2011-2014 Georg Lukas
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

package de.duenndns.gmdice;

import java.util.ArrayList;
import java.util.List;

/** DiceCache implements a least-recently-used cache strategy
 */
public class DiceCache {
	int count;
	ArrayList<DiceSet> dscache;

	public DiceCache(int cnt) {
		count = cnt;
		dscache = new ArrayList<DiceSet>();
		dscache.add(DiceSet.getDiceSet("1d2"));
		dscache.add(DiceSet.getDiceSet("1d6"));
		dscache.add(DiceSet.getDiceSet("1d6+1"));
		dscache.add(DiceSet.getDiceSet("1d10"));
		dscache.add(DiceSet.getDiceSet("1d20"));
	}

	public void add(FUDGEDiceSet ds) {
		return;
	}

	public void add(DSADiceSet ds) {
		return;
	}

	public void add(DiceSet ds) {
		// flush entry to list start
		dscache.remove(ds);
		dscache.add(0, ds);
		while (dscache.size() > count)
			dscache.remove(count);
	}

	public void populate(android.widget.ArrayAdapter<CharSequence> list, List<DiceSet> except) {
		for (DiceSet i : dscache) {
			if (!except.contains(i))
				list.add(i.toString());
		}
		if (!except.contains(DiceSet.getDiceSet(DiceSet.DSA)))
			list.add(DiceSet.DSA);
		if (!except.contains(DiceSet.getDiceSet(DiceSet.FUDGE)))
			list.add(DiceSet.FUDGE);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (DiceSet i : dscache) {
			sb.append(i.toString());
			sb.append("|");
		}
		return sb.toString();
	}

	public void loadFromString(String s) {
		if (s == null)
			return;
		String[] values = s.split("\\|");
		dscache.clear();
		for (String v : values) {
			if (v.length() > 0)
				dscache.add(DiceSet.getDiceSet(v));
		}
	}
}
