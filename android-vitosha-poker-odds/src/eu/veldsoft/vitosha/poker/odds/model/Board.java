package eu.veldsoft.vitosha.poker.odds.model;

/**
 * Bame board class.
 * 
 * @author Todor Balabanov
 * 
 * @email tdb@tbsoft.eu
 * 
 * @date 09 Aug 2012
 */
class Board {

	/**
	 * Array of cards contained in the flop.
	 */
	private Card[] flopCards = new Card[Constants.NUMBER_OF_FLOP_CARDS];

	/**
	 * Turn card.
	 */
	private Card turnCard = new Card();

	/**
	 * River card.
	 */
	private Card riverCard = new Card();

	/**
	 * Get the flop cards.
	 * 
	 * @return flop.
	 */
	public Card[] getFlop() {
		return flopCards;
	}

	/**
	 * Set the flop cards.
	 * 
	 * @param flop
	 */
	public void setFlop(Card[] flop) {
		this.flopCards = flop;
	}

	/**
	 * Get the turn card.
	 * 
	 * @return turn.
	 */
	public Card getTurn() {
		return turnCard;
	}

	/**
	 * Set the turn Card.
	 * 
	 * @param turn
	 */
	public void setTurn(Card turn) {
		this.turnCard = turn;
	}

	/**
	 * Get the river Card.
	 * 
	 * @return riverCard
	 */
	public Card getRiver() {
		return riverCard;
	}

	/**
	 * Set the river Card.
	 * 
	 * @param river
	 */
	public void setRiver(Card river) {
		this.riverCard = river;
	}

	/**
	 * Constructor without parameters.
	 * 
	 * @author Todor Balabanov
	 * 
	 * @email tdb@tbsoft.eu
	 * 
	 * @date 09 Aug 2012
	 */
	public Board() {
		for (int i = 0; i < flopCards.length; i++) {
			flopCards[i] = new Card();
		}
	}
}
