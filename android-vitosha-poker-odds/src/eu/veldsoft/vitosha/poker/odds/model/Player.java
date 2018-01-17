package eu.veldsoft.vitosha.poker.odds.model;

/**
 * Player class.
 * 
 * @author Todor Balabanov
 * 
 * @email tdb@tbsoft.eu
 * 
 * @date 09 Aug 2012
 */
class Player {

	/**
	 * Player type enumeration.
	 * 
	 * @author Mihail Ernandes
	 */
	enum Type {

		REGULAR(0x1), DEALER(0x2), SMALL_BLIND(0x4), BIG_BLIND(0x8);

		/**
		 * Value of the constant.
		 */
		private int value;

		/**
		 * Constructor.
		 * 
		 * @param value
		 */
		private Type(int value) {
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
	 * Array of cards in player's hand.
	 */
	private Card[] hand = new Card[Constants.NUMBER_OF_HAND_CARDS];

	/**
	 * Amount of money the player has.
	 */
	private long money;

	/**
	 * Player type (regular, dealer, blind).
	 */
	private Type type;

	/**
	 * Player's hand score.
	 */
	private long handScore;

	/**
	 * Player's win counter.
	 */
	private long winsCounter;

	/**
	 * Player's tie counter.
	 */
	private long tiesCounter;

	/**
	 * Constructor without parameters.
	 * 
	 * @author Todor Balabanov
	 * 
	 * @email tdb@tbsoft.eu
	 * 
	 * @date 09 Aug 2012
	 */
	public Player() {
		for (int i = 0; i < hand.length; i++) {
			hand[i] = new Card();
		}
	}

	/**
	 * Get player's cards in hand.
	 * 
	 * @return Array of cards.
	 */
	public Card[] getHand() {
		return hand;
	}

	/**
	 * Set player's cards in hand.
	 * 
	 * @param Array
	 *            of cards.
	 */
	public void setHand(Card[] hand) {
		this.hand = hand;
	}

	/**
	 * Get player's money.
	 * 
	 * @return Money value.
	 */
	public long getMoney() {
		return money;
	}

	/**
	 * Set player's money.
	 * 
	 * @param money
	 *            Money value.
	 */
	public void setMoney(long money) {
		this.money = money;
	}

	/**
	 * Get player's type.
	 * 
	 * @return Player type.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Set player's type.
	 * 
	 * @param type
	 *            Player type.
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * Get player's hand score.
	 * 
	 * @return Score of the player's hand.
	 */
	public long getHandScore() {
		return handScore;
	}

	/**
	 * Set player's handScore.
	 * 
	 * @param handScore
	 *            Score for the player's hand.
	 */
	public void setHandScore(long handScore) {
		this.handScore = handScore;
	}

	/**
	 * Get player's wins counter.
	 * 
	 * @return How many wins the player has.
	 */
	public long getWinsCounter() {
		return winsCounter;
	}

	/**
	 * Increments the win counter.
	 */
	public void incrementWinsCounter() {
		this.winsCounter++;
	}

	/**
	 * Set player's win counter.
	 * 
	 * @param winsCounter
	 *            How many wins the player has.
	 */
	public void setWinsCounter(long winsCounter) {
		this.winsCounter = winsCounter;
	}

	/**
	 * Get player's tie counter.
	 * 
	 * @return How many ties the player has.
	 */
	public long getTiesCounter() {
		return tiesCounter;
	}

	/**
	 * Increments the tie counter.
	 */
	public void incrementTiesCounter() {
		this.tiesCounter++;
	}

	/**
	 * Set player's tie Counter.
	 * 
	 * @param tiesCounter
	 *            How many ties the player has.
	 */
	public void setTiesCounter(long tiesCounter) {
		this.tiesCounter = tiesCounter;
	}
}
