/*==============================================================================
 =                                                                             =
 = Overflow is very simple but very addictive board game. The game is for two  =
 = players who try to conquer all stones of the opposite player. The game was  =
 = developed as master thesis in New Bulgarian University, Sofia, Bulgaria.    =
 =                                                                             =
 = Copyright (C) 2012 by Yuriy Stanchev  ( i_stanchev@ml1.net )                =
 =                                                                             =
 = This program is free software: you can redistribute it and/or modify        =
 = it under the terms of the GNU General Public License as published by        =
 = the Free Software Foundation, either version 3 of the License, or           =
 = (at your option) any later version.                                         =
 =                                                                             =
 = This program is distributed in the hope that it will be useful,             =
 = but WITHOUT ANY WARRANTY; without even the implied warranty of              =
 = MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               =
 = GNU General Public License for more details.                                =
 =                                                                             =
 = You should have received a copy of the GNU General Public License           =
 = along with this program. If not, see <http://www.gnu.org/licenses/>.        =
 =                                                                             =
 =============================================================================*/

package eu.veldsoft.colors.overflow;

import android.graphics.Point;

/**
 * This is the class that implements the easy AI and enables users to play
 * against the computer.
 * 
 * @author Yuriy Stanchev
 * 
 * @email i_stanchev@ml1.net
 * 
 * @date 27 Mar 2012
 */
class EasyAI extends AI {

	/**
	 * What kind of stones are on the board.
	 */
	private int stones[][];

	/**
	 * Uses +1 for the positive player and -1 for the negative player.
	 */
	private PlayerIndex who;

	/**
	 * Defines the number of turns.
	 */
	private int onMove;

	/**
	 * Holds the coordinates for the AI move.
	 */
	private Point coordinates = new Point();

	/**
	 * This method finds a random empty cell and sets the coordinates for the
	 * move of the computer in the first phase of the game.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 9 April 2012
	 */
	@Override
	protected void phaseOneMove() {
		/*
		 * Find empty cell.
		 */
		do {
			coordinates.x = (int) (Math.random() * stones.length);
			coordinates.y = (int) (Math.random() * stones[coordinates.x].length);
		} while (onMove < Board.NUMBER_OF_DEPLOYMENT_MOVES
				&& stones[coordinates.x][coordinates.y] != Board.EMPTY_CELL);
	}

	/**
	 * Randomly select an occupied from the AI cell.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 9 April 2012
	 */
	@Override
	protected void phaseTwoMove() throws Exception {
		/*
		 * Check for possible move.
		 */
		boolean found = false;
		for (int i = 0; i < stones.length && found == false; i++) {
			for (int j = 0; j < stones[i].length && found == false; j++) {
				if (stones[i][j] != Board.EMPTY_CELL
						&& PlayerIndex.index(stones[i][j] >> 8) == who) {
					found = true;
				}
			}
		}

		/*
		 * Handle not available move situation.
		 */
		if (found == false) {
			throw (new Exception("There is not valid move!"));
		}

		/*
		 * We find which cell is occupied by the AI player and if we have found
		 * an occupied cell we move there.
		 */
		while (found == true) {
			coordinates.x = (int) (Math.random() * stones.length);
			coordinates.y = (int) (Math.random() * stones[coordinates.x].length);

			if (stones[coordinates.x][coordinates.y] != Board.EMPTY_CELL
					&& PlayerIndex
							.index(stones[coordinates.x][coordinates.y] >> 8) == who) {
				break;
			}
		}
	}

	/**
	 * Implements the move of the Artificial Intelligence.
	 * 
	 * @param stones
	 *            Defines the stones on the board.
	 * 
	 * @param who
	 *            Defines who will be next on the move.
	 * 
	 * @param onMove
	 *            Defines the number of turns.
	 * 
	 * @return Returns the coordinates on which the AI will move its piece.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 27 Mar 2012
	 */
	public Point move(int stones[][], PlayerIndex who, int onMove)
			throws Exception {
		this.stones = stones;
		this.who = who;
		this.onMove = onMove;

		/*
		 * We check if the board has been incorrectly initialized.
		 */
		if (stones == null) {
			throw (new Exception("Incorrect board!"));
		}

		/*
		 * We check if the player that is on the board is correctly initialized.
		 */
		if (who == null) {
			throw (new Exception("Incorrect player!"));
		}

		/*
		 * Phase one A.I.
		 */
		if (onMove < Board.NUMBER_OF_DEPLOYMENT_MOVES) {
			phaseOneMove();
		}

		/*
		 * If it is deployment move do not calculate rising position.
		 */
		if (onMove < Board.NUMBER_OF_DEPLOYMENT_MOVES) {
			return (coordinates);
		}

		/*
		 * Select valid move on phase two.
		 */
		if (onMove >= Board.NUMBER_OF_DEPLOYMENT_MOVES) {
			phaseTwoMove();
		}

		return (coordinates);
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public boolean hasMove() {
		/*
		 * Phase one A.I.
		 */
		if (onMove < Board.NUMBER_OF_DEPLOYMENT_MOVES) {
			return true;
		} else {
			/*
			 * Phase two A.I.
			 */
			for (int i = 0; i < stones.length; i++) {
				for (int j = 0; j < stones[i].length; j++) {
					if (stones[i][j] != Board.EMPTY_CELL
							&& PlayerIndex.index(stones[i][j] >> 8) == who) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
