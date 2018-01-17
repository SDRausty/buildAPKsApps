package eu.veldsoft.vitosha.poker.odds.model;

/**
 * Pot class.
 * 
 * @author Todor Balabanov
 * 
 * @email tdb@tbsoft.eu
 * 
 * @date 09 Aug 2012
 */
class Pot {
	/**
	 * Array of player bets.
	 */
	private long[] bets = new long[Constants.MAX_NUMBER_OF_PLAYERS];

	/**
	 * Number of splits between players.
	 */
	private int numberOfSplits;

	/**
	 * Get current bets.
	 * 
	 * @return Bets amount.
	 */
	public long[] getBets() {
		return bets;
	}

	/**
	 * Set the current bets.
	 * 
	 * @param bets
	 *            Bets amount.
	 */
	public void setBets(long[] bets) {
		this.bets = bets;
	}

	/**
	 * Get the amount of splits.
	 * 
	 * @return Number of splits.
	 */
	public int getNumberOfSplits() {
		return numberOfSplits;
	}

	/**
	 * Set the amount of splits.
	 * 
	 * @param numberOfSplits
	 *            Number of splits.
	 */
	public void setNumberOfSplits(int numberOfSplits) {
		this.numberOfSplits = numberOfSplits;
	}
}
