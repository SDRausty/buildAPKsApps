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
 * Artificial Intelligence based on Artificial Neural Networks.
 * 
 * @author Yuriy Stanchev
 * 
 * @email i_stanchev@ml1.net
 * 
 * @date 13 Mar 2012
 */
class HardAI extends AI {

	/**
	 * Creates a new instance of the neural network layer.
	 */
	private ANN3Layers ann = null;

	/**
	 * What kind of stones are on the board.
	 */
	private int stones[][];

	/**
	 * Uses +1 for the positive player and -1 for the negative player.
	 */
	private PlayerIndex who;

	/**
	 * Holds the coordinates for the AI move.
	 */
	private Point coordinates = new Point();

	/**
	 * Prepare the input information in an appropriate manner for the neural
	 * network.
	 * 
	 * @return values Scaled board in the range [0.0 - 1.0].
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 13 Mar 2012
	 */
	private double[] prepareAnnInput() {
		int size = 0;

		/*
		 * Calculate input size.
		 */
		for (int i = 0; i < stones.length; i++) {
			for (int j = 0; j < stones[i].length; j++) {
				// TODO Take in mind that ANN should consider positive and
				// negative player.
				switch (stones[i][j] & 0x3) {
				case 1:
					stones[i][j] = who.small();
					break;
				case 2:
					stones[i][j] = who.middle();
					break;
				case 3:
					stones[i][j] = who.large();
					break;
				}
				size++;
			}
		}

		double values[] = new double[size];

		/*
		 * Scale stones information to ANN input [0.0 - 1.0].
		 */
		for (int i = 0, k = 0; i < stones.length; i++) {
			for (int j = 0; j < stones[i].length; j++, k++) {
				values[k] = (stones[i][j] + 3.0) / 6.0;
			}
		}

		return (values);
	}

	/**
	 * Phase Two of the game. Select the best move using the output layer of the
	 * neural network.
	 * 
	 * @param annOutput
	 *            The output of the ANN with evaluation of each board cell.
	 * 
	 * @return Best selected cell.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 13 Mar 2012
	 */
	private Point calculateCoordinatesPhaseTwo(double[] annOutput) {
		Point coordinates = null;

		double best = 0.0;
		for (int i = 0, k = 0; i < stones.length; i++) {
			for (int j = 0; j < stones[i].length; j++, k++) {
				if (stones[i][j] != 0
						&& PlayerIndex.index(stones[i][j] >> 8) == who
						&& annOutput[k] > best) {
					coordinates = new Point(i, j);
					best = annOutput[k];
				}
			}
		}

		return (coordinates);
	}

	/**
	 * Phase One of the game. Select the best move using the output layer of the
	 * neural network.
	 * 
	 * @param annOutput
	 *            The output of the ANN with evaluation of each board cell.
	 * 
	 * @return Best selected cell.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 13 Mar 2012
	 */
	private Point calculateCoordinatesPhaseOne(double[] annOutput) {
		Point coordinates = null;

		double best = 0.0;
		for (int i = 0, k = 0; i < stones.length; i++) {
			for (int j = 0; j < stones[i].length; j++, k++) {
				if (stones[i][j] == Board.EMPTY_CELL && annOutput[k] > best) {
					coordinates = new Point(i, j);
					best = annOutput[k];
				}
			}
		}

		return (coordinates);
	}

	/**
	 * Sets the weights on the neural networks on a random basis.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 13 Mar 2012
	 */
	public HardAI() {
		// TODO Load ANN info from DB.

		ann = new ANN3Layers(0, 64, 65, 64);

		// String data = "-1.0 0.0 1.0";
		// String values[] = data.split( "\\s+" );

		double weights[] = ann.getWeights();
		for (int i = 0; i < weights.length; i++) {
			// TODO Set weights taken from DB.
			weights[i] = Math.random() - 0.5;
			// weights[i] = (new Double(values[i]));
		}
		ann.setWeights(weights);
	}

	/**
	 * Internal ANN reference getter.
	 * 
	 * @return Internal ANN reference.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 16 Mar 2012
	 */
	public ANN3Layers getAnn() {
		return (ann);
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
		coordinates = null;

		ann.loadInput(prepareAnnInput());
		ann.feedForward();

		/*
		 * Find empty cell.
		 */
		coordinates = calculateCoordinatesPhaseOne(ann.storeOutput());
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
		coordinates = null;

		ann.loadInput(prepareAnnInput());
		ann.feedForward();
		coordinates = calculateCoordinatesPhaseTwo(ann.storeOutput());

		if (coordinates == null) {
			throw (new Exception("There is not valid move!"));
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
	 * @date 13 Mar 2012
	 */
	public Point move(int stones[][], PlayerIndex who, int onMove)
			throws Exception {
		this.stones = stones;
		this.who = who;

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
	 * Store ANN fitness value for particular database id.
	 * 
	 * @param fitness
	 *            ANN fitness value.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 16 Mar 2012
	 */
	void storeAnnFitness(double fitness) {
		ann.setFitness(fitness);
	}

}
