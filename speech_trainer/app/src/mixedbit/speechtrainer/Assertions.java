/**
 * This file is part of Speech Trainer.
 * Copyright (C) 2011 Jan Wrobel <wrr@mixedbit.org>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package mixedbit.speechtrainer;

/**
 * Assertions for checking pre and post conditions. Unlike built in assert
 * keyword, these are never turned off, so give more confidence that things that
 * should always be true are always true.
 */
public class Assertions {

    public static void check(boolean shouldBeTrue) {
        if (shouldBeTrue == false) {
            throw new AssertionError();
        }
    }

    public static void illegalStateIfFalse(boolean shouldBeTrue) {
        if (shouldBeTrue == false) {
            throw new IllegalStateException();
        }
    }

    public static void illegalStateIfFalse(boolean shouldBeTrue, String errorMessage) {
        if (shouldBeTrue == false) {
            throw new IllegalStateException(errorMessage);
        }
    }
}
