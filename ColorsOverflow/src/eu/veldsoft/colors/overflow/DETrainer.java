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
 * This class is used to train the Neural Network of the Hard AI.
 * 
 * @author Yuriy Stanchev
 * 
 * @email i_stanchev@ml1.net
 * 
 * @date 19 April 2012
 */
class DETrainer {

	/**
	 * Defines the population.
	 */
	private double population[][];

	/**
	 * Rates the fitness of the population.
	 */
	private double fitness[];

	/**
	 * Result chromosome index.
	 */
	private int resultIndex;

	/**
	 * First parent chromosome index.
	 */
	private int firstIndex;

	/**
	 * Second parent chromosome index.
	 */
	private int secondIndex;

	/**
	 * Defines an initial population size.
	 */
	public static final int INITIAL_POPULATION_SIZE = 10;

	/**
	 * Defines the minimal range of the population.
	 */
	public static final double MIN_RANDOM_VALUE = -10.0;

	/**
	 * Defines the maximum range of the population.
	 */
	public static final double MAX_RANDOM_VALUE = 10.0;

	/**
	 * This is the percentage of probability that the result of crossover can be
	 * written into a chromosome with best fitness.
	 */
	public static final int CROSSOVER_RESULT_INTO_BEST_PERCENT = 5;

	/**
	 * This is the percentage of probability that the result of crossover can be
	 * written into a chromosome with middle fitness.
	 */
	public static final int CROSSOVER_RESULT_INTO_MIDDLE_PERCENT = 40;

	/**
	 * This is the percentage of probability that the result of crossover can be
	 * written into a chromosome with worst fitness.
	 */
	public static final int CROSSOVER_RESULT_INTO_WORST_PERCENT = 55;

	/**
	 * Genetic algorithm selection operator.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 22 April 2012
	 */
	private void select() {
		int index;
		resultIndex = (int) (Math.random() * population.length);
		firstIndex = (int) (Math.random() * population.length);
		secondIndex = (int) (Math.random() * population.length);

		/*
		 * Random value should be between 0 and sum of all percents (general
		 * 100%).
		 */
		int percent = (int) (Math.random() * (CROSSOVER_RESULT_INTO_WORST_PERCENT
				+ CROSSOVER_RESULT_INTO_MIDDLE_PERCENT + CROSSOVER_RESULT_INTO_BEST_PERCENT));

		if (percent < CROSSOVER_RESULT_INTO_WORST_PERCENT) {
			/*
			 * Worst fitness value is the highest.
			 */
			if (fitness[resultIndex] < fitness[firstIndex]) {
				index = resultIndex;
				resultIndex = firstIndex;
				firstIndex = index;
			}
			if (fitness[resultIndex] < fitness[secondIndex]) {
				index = resultIndex;
				resultIndex = secondIndex;
				secondIndex = index;
			}
		} else if (percent < (CROSSOVER_RESULT_INTO_WORST_PERCENT + CROSSOVER_RESULT_INTO_MIDDLE_PERCENT)) {
			/*
			 * Middle fitness value is between the others.
			 */
			if (fitness[secondIndex] < fitness[firstIndex]) {
				index = secondIndex;
				secondIndex = firstIndex;
				firstIndex = index;
			}
			if (fitness[resultIndex] < fitness[firstIndex]) {
				index = resultIndex;
				resultIndex = firstIndex;
				firstIndex = index;
			}
			if (fitness[resultIndex] > fitness[secondIndex]) {
				index = resultIndex;
				resultIndex = secondIndex;
				secondIndex = index;
			}
		} else if (percent < (CROSSOVER_RESULT_INTO_WORST_PERCENT
				+ CROSSOVER_RESULT_INTO_MIDDLE_PERCENT + CROSSOVER_RESULT_INTO_BEST_PERCENT)) {
			/*
			 * Best fitness value is the smallest.
			 */
			if (fitness[resultIndex] > fitness[firstIndex]) {
				index = resultIndex;
				resultIndex = firstIndex;
				firstIndex = index;
			}
			if (fitness[resultIndex] > fitness[secondIndex]) {
				index = resultIndex;
				resultIndex = secondIndex;
				secondIndex = index;
			}
		}
	}

	/**
	 * Crosses the population at the indexes in the select method.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 19 April 2012
	 */
	private void crossover() {
		int index = (int) (Math.random() * population[resultIndex].length + 1);

		for (int i = 0; i < index; i++) {
			population[resultIndex][i] = population[firstIndex][i];
		}
		for (int i = index; i < population[resultIndex].length; i++) {
			population[resultIndex][i] = population[secondIndex][i];
		}
	}

	/**
	 * Mutates the population at a random index.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 19 April 2012
	 */
	private void mutate() {
		double coefficient = 0.001 * Math.random();

		firstIndex = (int) (Math.random() * population.length);
		secondIndex = (int) (Math.random() * population.length);

		for (int i = 0; i < population[resultIndex].length; i++) {
			population[resultIndex][i] += coefficient
					* (population[firstIndex][i] - population[secondIndex][i]);
		}
	}

	/**
	 * This is the constructor of the class.
	 * 
	 * @param populationSize
	 *            Defines the size of the used population.
	 * 
	 * @param chromosomeSize
	 *            Defines the chromosome size for the population.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 19 April 2012
	 */
	public DETrainer(int populationSize, int chromosomeSize) {
		populationSize = Math.max(populationSize, INITIAL_POPULATION_SIZE);

		population = new double[populationSize][];
		for (int p = 0; p < population.length; p++) {
			population[p] = new double[chromosomeSize];
		}

		fitness = new double[populationSize];

		randomInit();
	}

	/**
	 * Initializes randomly a population.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 19 April 2012
	 */
	public void randomInit() {
		for (int p = 0; p < population.length; p++) {
			for (int i = 0; i < population[p].length; i++) {
				population[p][i] = MIN_RANDOM_VALUE + Math.random()
						* (MAX_RANDOM_VALUE - MIN_RANDOM_VALUE);
			}

			fitness[p] = 0.0;
		}
	}

	/**
	 * This Method will load the population.
	 * 
	 * @param population
	 *            Holds the actual population.
	 * 
	 * @param fitness
	 *            Holds the fitness of the population.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 19 April 2012
	 */
	void loadPopulation(double population[][], double fitness[]) {
		for (int p = 0; p < this.population.length && p < population.length; p++) {
			if (this.population[p].length != population[p].length) {
				// TODO Rise exception.
				continue;
			}

			for (int i = 0; i < population[p].length; i++) {
				this.population[p][i] = population[p][i];
				this.fitness[i] = fitness[i];
			}
		}
	}

	/**
	 * This method creates the evolution of the population.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 19 April 2012
	 */
	public void evolve() {
		for (int k = 0; k < population.length * population.length; k++) {
			select();
			crossover();
			mutate();
		}
	}

	/**
	 * This method will return the population.
	 * 
	 * @return Returns the population.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 19 April 2012
	 */
	public double[][] obtainPopulation() {
		return (population);
	}
}
