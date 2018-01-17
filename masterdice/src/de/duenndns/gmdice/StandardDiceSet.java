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

public class StandardDiceSet extends DiceSet {

	public StandardDiceSet(int c, int s, int m) {
		count = c;
		sides = s;
		modifier = m;
	}

	public String roll(Context ctx, Random gen) {
		int result = 0;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			int roll1 = gen.nextInt(sides) + 1;
			sb.append(roll1);
			result += roll1;
			if (i < count-1)
				sb.append("+");
		}
		if (modifier != 0) {
			sb.append(String.format("%+d", modifier));
			result += modifier;
		}
		if (count > 1 || modifier != 0) {
			sb.append(" = ");
			sb.append(result);
		}
		return sb.toString();
	}

	public String toString() {
		if (modifier == 0)
			return String.format("%dd%d", count, sides);
		else
			return String.format("%dd%d%+d", count, sides, modifier);
	}

}
