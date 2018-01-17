package eu.veldsoft.vitosha.poker.odds.model;

/**
 * 
 * @author Todor Balabanov
 * 
 * @email tdb@tbsoft.eu
 * 
 * @date 09 Aug 2012
 */
class Deck {
	/**
	 * Deck consisting of array of cards.
	 */
	private Card[] cards = new Card[Constants.NUMBER_OF_CARDS];

	/**
	 * Constructor without parameters.
	 * 
	 * @author Todor Balabanov
	 * 
	 * @email tdb@tbsoft.eu
	 * 
	 * @date 09 Aug 2012
	 */
	public Deck() {
		for (int i = 0; i < cards.length; i++) {
			cards[i] = new Card();
		}
	}

	/**
	 * Get the cards in deck.
	 * 
	 * @return All deck cards.
	 */
	public Card[] getCards() {
		return cards;
	}

	/**
	 * Set the cards in deck.
	 * 
	 * @param cards
	 *            Set of cards.
	 */
	public void setCards(Card[] cards) {
		this.cards = cards;
	}
}