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

import android.content.Context; // needed for translation strings

import java.util.Random;

/** DiceSet is the implementation of a set of dice
 *
 * a dice set is specified by three parameters:
 *
 * @param count number of dice
 * @param sides number of sides each die has
 * @param modifier value to add/subtract to the result
 */
public abstract class DiceSet {
	// 3D20 is special: three attribute probes together instead of a sum
	public static final String DSA = "3D20";
	// FUDGE is special: showing +, 0, and - symbols
	public static final String FUDGE = "4dF";
	int count;
	int sides;
	int modifier;

	public static DiceSet getDiceSet(int c, int s, int m) {
		return new StandardDiceSet(c, s, m);
	}

	public static DiceSet getDiceSet() {
		return new StandardDiceSet(1, 6, 0);
	}

	public static DiceSet getDiceSet(String set) {
		int c;
		int s;
		int m;
		if (set.equals(DSA)) {
			return new DSADiceSet();
		} else if (set.equals(FUDGE)) {
			return new FUDGEDiceSet();
		} else {
			try {
				String[] parts = set.split("[d+-]");
				c = Integer.parseInt(parts[0]);
				s = Integer.parseInt(parts[1]);
				if (parts.length > 2) {
					m = Integer.parseInt(parts[2]);
					if (set.indexOf('-') >= 0)
						m = -m;
				} else {
					m = 0;
				}
				if (c == 1 && s == 2 && m == 0)
					return new Coin();
				else
					return getDiceSet(c, s, m);
			} catch (IndexOutOfBoundsException e) {
				return getDiceSet();
			} catch (NumberFormatException e) {
				return getDiceSet();
			}
		}
	}

	public abstract String toString();

	public abstract String roll(Context ctx, Random gen);

	// utility methods
	public int hashCode() {
		return count*10000 + sides*100 + modifier;
	}
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		DiceSet d = (DiceSet)o;
		return (count == d.count && sides == d.sides && modifier == d.modifier);
	}
}

