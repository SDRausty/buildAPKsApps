package eu.veldsoft.vitosha.poker.odds.model;

/**
 * General application constants.
 * 
 * @author Todor Balabanov
 * 
 * @email tdb@tbsoft.eu
 * 
 * @date 09 Aug 2012
 */
class Constants {
	static final int HUMAN_PLAYER_INDEX = 0;

	static final long MIN_NUMBER_OF_LOOPS = 1L;
	static final long MAX_NUMBER_OF_LOOPS = 100000L;

	static final int MIN_NUMBER_OF_PLAYERS = 2;
	static final int MAX_NUMBER_OF_PLAYERS = 10;

	static final int NUMBER_OF_CARDS = 52;
	static final int NUMBER_OF_EVALUATION_HAND_CARDS = 5;
	static final int NUMBER_OF_POSSIBLE_HANDS_FOR_EVALUATION = 21;
	static final int NUMBER_OF_HAND_CARDS = 2;
	static final int NUMBER_OF_FLOP_CARDS = 3;
	static final int NUMBER_OF_KNOWN_CARDS = 7;

	static final long MIN_PLAYER_MONEY = 100L;
	static final long MAX_PLAYER_MONEY = 100000L;
}
