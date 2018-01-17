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

import java.util.Vector;

/**
 * This class is used for the implementation of the Normal AI of the game.
 * 
 * @author Yuriy Stanchev
 * 
 * @email i_stanchev@ml1.net
 * 
 * @date 09 April 2012
 */
class NormalAI extends AI {

	/**
	 * Defines the maximum number of valid moves to be generated.
	 */
	private static final int MAX_NUMBER_CELLS_CHECK = 10;

	/**
	 * This determines the size for the aperture size, used for move evaluation.
	 */
	private static final int APERTURE_SIZE = 3;

	/**
	 * What kind of stones are on the board.
	 */
	// TODO Change int to proper object.
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
	 * Finds randomly one valid move and returns the coordinates.
	 * 
	 * @return Valid move coordinates.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 9 April 2012
	 */
	private Point findRandomValidMove() {
		Point coordinates = new Point();

		/*
		 * We find which cell is occupied by the AI player and if we have found
		 * an occupied cell we move there.
		 */
		while (true) {
			coordinates.x = (int) (Math.random() * stones.length);
			coordinates.y = (int) (Math.random() * stones[coordinates.x].length);

			if (stones[coordinates.x][coordinates.y] != Board.EMPTY_CELL
					&& PlayerIndex
							.index(stones[coordinates.x][coordinates.y] >> 8)
							== who) {
				break;
			}
		}

		return (coordinates);
	}

	/**
	 * This method returns a set of valid possible moves.
	 * 
	 * @return Returns a valid set of moves.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 9 April 2012
	 */
	private Vector<Point> generateSetOfValidMoves() {
		Vector<Point> moves = new Vector<Point>();

		for (int i = 0; i < MAX_NUMBER_CELLS_CHECK; i++) {
			moves.add(findRandomValidMove());
		}

		return (moves);
	}

	/**
	 * This method selects the best possible move from the generated possible
	 * moves.
	 * 
	 * @return Returns coordinates of the best possible move.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 9 April 2012
	 */
	private Point selectBestMove() {
		Vector<Point> moves = generateSetOfValidMoves();

		Point best = moves.get(0);
		int bestEvaluation = 0;
		for (int k = 0; k < moves.size(); k++) {
			Point current = moves.get(k);

			int aperture[][] = new int[APERTURE_SIZE][APERTURE_SIZE];
			for (int j = 0; j < APERTURE_SIZE; j++) {
				for (int i = 0; i < APERTURE_SIZE; i++) {
					aperture[i][j] = Board.EMPTY_CELL;
				}
			}

			/*
			 * Fill aperture.
			 */
			int x = current.x;
			int y = current.y;
			for (int j = 0; j < APERTURE_SIZE; j++) {
				for (int i = 0; i < APERTURE_SIZE; i++) {
					int m = x - APERTURE_SIZE / 2 + i;
					int n = y - APERTURE_SIZE / 2 + j;

					if (m < 0 || n < 0 || m >= stones.length) {
						continue;
					} else if (n >= stones[m].length) {
						continue;
					}

					aperture[i][j] = stones[m][n];
				}
			}

			/*
			 * Aperture should be only for the positive player.
			 */
			if (aperture[APERTURE_SIZE / 2][APERTURE_SIZE / 2] < 0) {
				for (int j = 0; j < APERTURE_SIZE; j++) {
					for (int i = 0; i < APERTURE_SIZE; i++) {
						aperture[i][j] = -aperture[i][j];
					}
				}
			}

			/*
			 * Convert aperture to map key value for all five combinations.
			 */
			long keys[] = new long[5];

			/*
			 * Flip horizontal combination.
			 */
			keys[0] = (aperture[1][2] + 3) << 11 | (aperture[0][1] + 3) << 8
					| (aperture[1][1] - 1) << 6 | (aperture[2][1] + 3) << 3
					| (aperture[1][0] + 3) << 0;

			/*
			 * Flip vertical combination.
			 */
			keys[1] = (aperture[1][0] + 3) << 11 | (aperture[2][1] + 3) << 8
					| (aperture[1][1] - 1) << 6 | (aperture[0][1] + 3) << 3
					| (aperture[1][2] + 3) << 0;

			/*
			 * Flip primary diagonal combination.
			 */
			keys[2] = (aperture[0][1] + 3) << 11 | (aperture[1][0] + 3) << 8
					| (aperture[1][1] - 1) << 6 | (aperture[1][2] + 3) << 3
					| (aperture[2][1] + 3) << 0;

			/*
			 * Flip secondary diagonal combination.
			 */
			keys[3] = (aperture[2][1] + 3) << 11 | (aperture[1][2] + 3) << 8
					| (aperture[1][1] - 1) << 6 | (aperture[1][0] + 3) << 3
					| (aperture[0][1] + 3) << 0;

			/*
			 * Original combination.
			 */
			keys[4] = (aperture[1][0] + 3) << 11 | (aperture[0][1] + 3) << 8
					| (aperture[1][1] - 1) << 6 | (aperture[2][1] + 3) << 3
					| (aperture[1][2] + 3) << 0;

			/*
			 * Evaluate move.
			 */
			Integer evaluation = GameView.obtainNormalAi(keys);

			/*
			 * If current move is better than the known until now save current
			 * move info as best.
			 */
			if (evaluation != null && bestEvaluation < evaluation.intValue()) {
				best = current;
				bestEvaluation = evaluation;
			}
		}

		return (best);
	}

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
	 * This method checks if there is a possible move and selects the best in
	 * the second phase of the game.
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
			throw (new Exception("There is no valid move!"));
		}

		/*
		 * Select best move on phase two.
		 */
		coordinates = selectBestMove();
	}

	/**
	 * This method gathers all stages together and returns the result of them as
	 * a valid move.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 9 April 2012
	 */
	@Override
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
