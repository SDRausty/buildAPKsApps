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

/**
 * Artificial Neural Network with 3 layers.
 * 
 * @author Yuriy Stanchev
 * 
 * @email i_stanchev@ml1.net
 * 
 * @date 13 Mar 2012
 */
class ANN3Layers {

	/**
	 * Database record id.
	 */
	int id;

	/**
	 * Holds the input layer of ANN. In this case it is the board of the game
	 * (stones) scaled in the range of [0.0 - 1.0].
	 */
	double inputLayer[] = null;

	/**
	 * Holds the weights between input and hidden layer.
	 */
	double inputHiddenWeights[][] = null;

	/**
	 * Holds the ANN. hidden layer.
	 */
	double hiddenLayer[] = null;

	/**
	 * Holds the weights between hidden and output layer.
	 */
	double hiddenOutputWeights[][] = null;

	/**
	 * Holds the output layer of ANN. In this case it is evaluation of each
	 * position on the board.
	 */
	double outputLayer[] = null;

	/**
	 * Fitness value is a value showing how well the ANN is performing.
	 */
	double fitness;

	/**
	 * Class constructor of the neural network.
	 * 
	 * @param inputLayerSize
	 *            defines the size of the input layer.
	 * 
	 * @param hiddenLayerSize
	 *            defines the size of the hidden layer.
	 * 
	 * @param outputLayerSize
	 *            defines the size of the output layer.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 13 Mar 2012
	 */
	public ANN3Layers(int id, int inputLayerSize, int hiddenLayerSize,
			int outputLayerSize) {

		this.id = id;

		/*
		 * Plus one for the bias neuron.
		 */
		inputLayer = new double[inputLayerSize + 1];
		inputLayer[0] = 1.0;

		/*
		 * Plus one for the bias neuron.
		 */
		hiddenLayer = new double[hiddenLayerSize + 1];
		hiddenLayer[0] = 1.0;

		/*
		 * Plus one for the bias neuron.
		 */
		outputLayer = new double[outputLayerSize + 1];
		outputLayer[0] = 1.0;

		inputHiddenWeights = new double[inputLayer.length][hiddenLayer.length];

		hiddenOutputWeights = new double[hiddenLayer.length][outputLayer.length];
	}

	/**
	 * ANN database id getter.
	 * 
	 * @return ANN database id.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 13 Mar 2012
	 */
	public int getId() {
		return (id);
	}

	/**
	 * ANN fitness value getter.
	 * 
	 * @return ANN fitness value.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 13 Mar 2012
	 */
	public double getFitness() {
		return (fitness);
	}

	/**
	 * ANN fitness value setter.
	 * 
	 * @param fitness
	 *            ANN fitness value.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 13 Mar 2012
	 */
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	/**
	 * Obtain weights from both matrices.
	 * 
	 * @return Linear weights array.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 13 Mar 2012
	 */
	public double[] getWeights() {
		double[] weights = new double[inputLayer.length * hiddenLayer.length
				+ hiddenLayer.length * outputLayer.length];

		int k = 0;

		for (int j = 0; j < hiddenLayer.length; j++) {
			for (int i = 0; i < inputLayer.length; i++) {
				weights[k++] = inputHiddenWeights[i][j];
			}
		}

		for (int j = 0; j < outputLayer.length; j++) {
			for (int i = 0; i < hiddenLayer.length; i++) {
				weights[k++] = hiddenOutputWeights[i][j];
			}
		}

		return (weights);
	}

	/**
	 * Sets the weights of the input and hidden layer.
	 * 
	 * @param weights
	 *            Linear weights array.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 13 Mar 2012
	 */
	public void setWeights(double[] weights) {
		if (weights.length != inputLayer.length * hiddenLayer.length
				+ hiddenLayer.length * outputLayer.length) {
			// TODO Implement exception.
			return;
		}

		int k = 0;

		for (int j = 0; j < hiddenLayer.length; j++) {
			for (int i = 0; i < inputLayer.length; i++) {
				inputHiddenWeights[i][j] = weights[k++];
			}
		}

		for (int j = 0; j < outputLayer.length; j++) {
			for (int i = 0; i < hiddenLayer.length; i++) {
				hiddenOutputWeights[i][j] = weights[k++];
			}
		}
	}

	/**
	 * Loads the input values.
	 * 
	 * @param values
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 13 Mar 2012
	 */
	public void loadInput(double values[]) {
		if (inputLayer.length - 1 != values.length) {
			// TODO Implement exception.
			return;
		}

		for (int i = 0; i < values.length; i++) {
			/*
			 * Plus one because of the bias neuron.
			 */
			inputLayer[i + 1] = values[i];
		}
	}

	/**
	 * Feed the input information trough ANN to output layer.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 13 Mar 2012
	 */
	public void feedForward() {
		/*
		 * Miss bias neuron.
		 */
		for (int h = 1; h < hiddenLayer.length; h++) {
			double sum = 0.0;

			/*
			 * Do not miss the bias neuron.
			 */
			for (int i = 0; i < inputLayer.length; i++) {
				sum += inputLayer[i] * inputHiddenWeights[i][h];
			}

			hiddenLayer[h] = 1.0 / (1 + Math.exp(-sum));
		}

		/*
		 * Miss bias neuron.
		 */
		for (int o = 1; o < outputLayer.length; o++) {
			double sum = 0.0;

			/*
			 * Do not miss the bias neuron.
			 */
			for (int h = 1; h < hiddenLayer.length; h++) {
				sum += hiddenLayer[h] * hiddenOutputWeights[h][o];
			}

			outputLayer[o] = 1.0 / (1 + Math.exp(-sum));
		}
	}

	/**
	 * Used to return the results of the output layer.
	 * 
	 * @return Returns the values of the output layer.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 13 Mar 2012
	 */
	public double[] storeOutput() {
		/*
		 * Minus one because of the bias neuron.
		 */
		double values[] = new double[outputLayer.length - 1];

		for (int i = 0; i < values.length; i++) {
			/*
			 * Plus one because of the bias neuron.
			 */
			values[i] = outputLayer[i + 1];
		}

		return (values);
	}
}
