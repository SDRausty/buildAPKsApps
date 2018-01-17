package eu.veldsoft.vitosha.poker.odds.model;

/**
 * Card class.
 * 
 * @author Todor Balabanov
 * 
 * @email tdb@tbsoft.eu
 * 
 * @date 09 Aug 2012
 */
class Card {

	/**
	 * Card rank enumeration.
	 * 
	 * @author Mihail Ernandes
	 */
	enum Rank {

		WEAK_ACE(0x1), TWO(0x2), THREE(0x3), FOUR(0x4), FIVE(0x5), SIX(0x6), SEVEN(0x7), EIGHT(0x8), NINE(0x9), TEN(
				0xA), JACK(0xB), QUEEN(0xC), KING(0xD), ACE(0xE);

		/**
		 * Value of the constant.
		 */
		private int value;

		/**
		 * Constructor.
		 * 
		 * @param value
		 */
		private Rank(int value) {
			this.value = value;
		}

		/**
		 * Value of the constant getter.
		 * 
		 * @return Value of the constant.
		 */
		public int value() {
			return value;
		}

	}

	/**
	 * Card suit enumeration.
	 * 
	 * @author Mihail Ernandes
	 */
	enum Suit {

		CLUBS(0x10), DIAMONDS(0x20), HEARTS(0x30), SPADES(0x40);

		/**
		 * Value of the constant.
		 */
		private int value;

		/**
		 * 
		 * Constructor.
		 * 
		 * @param value
		 */
		private Suit(int value) {
			this.value = value;
		}

		/**
		 * 
		 * Value of the constant getter.
		 * 
		 * @return Value of the constant.
		 */
		public int value() {
			return value;
		}

	}

	/**
	 * Suit of the card.
	 */
	private Suit suit;

	/**
	 * Kind of the card.
	 */
	private Rank rank;

	/**
	 * Is the card revealed flag.
	 */
	private boolean known;

	/**
	 * Constructor without parameters.
	 */
	public Card() {
	}

	/**
	 * Constructor with all parameters.
	 * 
	 * @param suit
	 *            Card suit.
	 * @param kind
	 *            Card kind.
	 * @param known
	 *            Is the card known flag.
	 */
	public Card(Suit suit, Rank rank, boolean known) {
		this.suit = suit;
		this.rank = rank;
		this.known = known;
	}

	/**
	 * Get the card suit.
	 * 
	 * @return Card suit.
	 */
	public Suit getSuit() {
		return suit;
	}

	/**
	 * Set the suit of the current card.
	 * 
	 * @param suit
	 *            Card suit.
	 */
	public void setSuit(Suit suit) {
		this.suit = suit;
	}

	/**
	 * Get the card rank.
	 * 
	 * @return Card rank.
	 */
	public Rank getRank() {
		return rank;
	}

	/**
	 * Set the kind of the current card.
	 * 
	 * @param rank
	 *            Card kind.
	 */
	public void setRank(Rank rank) {
		this.rank = rank;
	}

	/**
	 * Get the known state of the card.
	 * 
	 * @return Is the card known.
	 */
	public boolean isKnown() {
		return known;
	}

	/**
	 * Set the known state of the current card.
	 * 
	 * @param known
	 *            Is the card known flag.
	 */
	public void setKnown(boolean known) {
		this.known = known;
	}

}
