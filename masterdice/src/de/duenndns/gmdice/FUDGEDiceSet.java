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

public class FUDGEDiceSet extends DiceSet {

	public FUDGEDiceSet() {
		count = 4;
		sides = 3; // The purist in me wants 6, but there's no good reason to.
		modifier = 0;
	}

	public String roll(Context ctx, Random gen) {
		StringBuilder sb = new StringBuilder();
		int total;
		total = 0;
		for (int i = 0; i < count; i++) {
			int roll1 = gen.nextInt(sides) + 1;
			if (roll1 == 1) {
				sb.append("-");
				total--;
			} else if (roll1 == 3) {
				sb.append("+");
				total++;
			} else {
				sb.append("0");
			}
			if (i < count-1)
				sb.append(" ");
		}
		if (total > 0)
			sb.append(" = +");
		else
			sb.append(" = ");
		sb.append(total);
		return sb.toString();
	}

	public String toString() {
		return FUDGE;
	}

	public int hashCode() {
		return 10040300;
	}
}
