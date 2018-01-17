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

public class Coin extends DiceSet {
	public Coin() {
		count = 1;
		sides = 2;
		modifier = 0;
	}

	public String toString() {
		return "1d2";
	}

	public String roll(Context ctx, Random gen) {
		return ctx.getString((gen.nextInt(2) == 1) ?
			R.string.binary_yes : R.string.binary_no);
	}
}
