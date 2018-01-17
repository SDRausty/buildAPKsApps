package eu.veldsoft.vitosha.poker.odds.model;

/**
 * Hands strength class.
 * 
 * @author Todor Balabanov
 * 
 * @email tdb@tbsoft.eu
 * 
 * @date 09 Aug 2012
 */
class HandStrength {
	/**
	 * Strength of a fifth kicker.
	 */
	private long fifthKicker;

	/**
	 * Strength of a Fourth kicker.
	 */
	private long fourthKicker;

	/**
	 * Strength of a Third kicker.
	 */
	private long thirdKicker;

	/**
	 * Strength of a Second kicker.
	 */
	private long secondKicker;

	/**
	 * Strength of a First kicker.
	 */
	private long firstKicker;

	/**
	 * Strength of a One pair.
	 */
	private long onePair;

	/**
	 * Strength of a Two pair.
	 */
	private long twoPairs;

	/**
	 * Strength of a Three of a kind.
	 */
	private long threeOfKind;

	/**
	 * Strength of a Straight.
	 */
	private long straight;

	/**
	 * Strength of a Flush.
	 */
	private long flush;

	/**
	 * Strength of a full house.
	 */
	private long fullHouse;

	/**
	 * Strength of a four of a kind.
	 */
	private long fourOfKind;

	/**
	 * Strength of a straight flush.
	 */
	private long straightFlush;

	/**
	 * Fifth kicker getter.
	 * 
	 * @return Value of the fifth kicker.
	 */
	public long getFifthKicker() {
		return fifthKicker;
	}

	/**
	 * Fifth kicker setter.
	 * 
	 * @param fifthKicker
	 *            Value of the fifth kicker.
	 */
	public void setFifthKicker(long fifthKicker) {
		this.fifthKicker = fifthKicker;
	}

	/**
	 * Fourth kicker getter.
	 * 
	 * @return Value of the fourth kicker.
	 */
	public long getFourthKicker() {
		return fourthKicker;
	}

	/**
	 * Fourth kicker setter.
	 * 
	 * @param fourthKicker
	 *            Value of the fourth kicker.
	 */
	public void setFourthKicker(long fourthKicker) {
		this.fourthKicker = fourthKicker;
	}

	/**
	 * Third kicker getter.
	 * 
	 * @return Value of of the third kicker.
	 */
	public long getThirdKicker() {
		return thirdKicker;
	}

	/**
	 * Third kicker setter.
	 * 
	 * @param thirdKicker
	 *            Value of the third kicker.
	 */
	public void setThirdKicker(long thirdKicker) {
		this.thirdKicker = thirdKicker;
	}

	/**
	 * Second kicker getter.
	 * 
	 * @return Value of the second kicker.
	 */
	public long getSecondKicker() {
		return secondKicker;
	}

	/**
	 * Second kicker setter.
	 * 
	 * @param secondKicker
	 *            Value of the second kicker.
	 */
	public void setSecondKicker(long secondKicker) {
		this.secondKicker = secondKicker;
	}

	/**
	 * First kicker getter.
	 * 
	 * @return Value of the first kicker.
	 */
	public long getFirstKicker() {
		return firstKicker;
	}

	/**
	 * Fist kicker setter.
	 * 
	 * @param firstKicker
	 *            Value of the first kicker.
	 */
	public void setFirstKicker(long firstKicker) {
		this.firstKicker = firstKicker;
	}

	/**
	 * One pair getter.
	 * 
	 * @return Value of one pair.
	 */
	public long getOnePair() {
		return onePair;
	}

	/**
	 * One pair setter.
	 * 
	 * @param onePair
	 *            Value of one pair.
	 */
	public void setOnePair(long onePair) {
		this.onePair = onePair;
	}

	/**
	 * Two pairs getter.
	 * 
	 * @return Value of two pairs.
	 */
	public long getTwoPairs() {
		return twoPairs;
	}

	/**
	 * Two paris setter.
	 * 
	 * @param twoPairs
	 *            Value of two pairs.
	 */
	public void setTwoPairs(long twoPairs) {
		this.twoPairs = twoPairs;
	}

	/**
	 * Three of a kind getter.
	 * 
	 * @return Value for three of a kind.
	 */
	public long getThreeOfKind() {
		return threeOfKind;
	}

	/**
	 * Three of a kind setter.
	 * 
	 * @param threeOfKind
	 *            Value for three of a kind.
	 */
	public void setThreeOfKind(long threeOfKind) {
		this.threeOfKind = threeOfKind;
	}

	/**
	 * Straight getter.
	 * 
	 * @return Value of straight.
	 */
	public long getStraight() {
		return straight;
	}

	/**
	 * Straight setter.
	 * 
	 * @param straight
	 *            Value of straight.
	 */
	public void setStraight(long straight) {
		this.straight = straight;
	}

	/**
	 * Flush getter.
	 * 
	 * @return Value of the flush.
	 */
	public long getFlush() {
		return flush;
	}

	/**
	 * Flush setter.
	 * 
	 * @param flush
	 *            Value of the flush.
	 */
	public void setFlush(long flush) {
		this.flush = flush;
	}

	/**
	 * Full house getter.
	 * 
	 * @return Value of the full house.
	 */
	public long getFullHouse() {
		return fullHouse;
	}

	/**
	 * Full house setter.
	 * 
	 * @param fullHouse
	 *            Value of the full house.
	 */
	public void setFullHouse(long fullHouse) {
		this.fullHouse = fullHouse;
	}

	/**
	 * Four of a kind getter.
	 * 
	 * @return Value for four of a kind.
	 */
	public long getFourOfKind() {
		return fourOfKind;
	}

	/**
	 * Four of a kind setter.
	 * 
	 * @param fourOfKind
	 *            Value for four of a kind.
	 */
	public void setFourOfKind(long fourOfKind) {
		this.fourOfKind = fourOfKind;
	}

	/**
	 * Straight flush getter.
	 * 
	 * @return Value of straight flush.
	 */
	public long getStraightFlush() {
		return straightFlush;
	}

	/**
	 * Straight flush setter.
	 * 
	 * @param straightFlush
	 *            Value of straight flush.
	 */
	public void setStraightFlush(long straightFlush) {
		this.straightFlush = straightFlush;
	}

	/**
	 * Total value getter.
	 * 
	 * @return Total value of the hand.
	 * 
	 * @author Todor Balabanov
	 * 
	 * @email tdb@tbsoft.eu
	 * 
	 * @date 09 Aug 2012
	 */
	long getValue() {
		long result = 0L;

		int offset = 0;

		/*
		 * long fifthKicker:(4);
		 */
		result |= (fifthKicker << offset);
		offset += 4;

		/*
		 * long fourthKicker:(4);
		 */
		result |= (fourthKicker << offset);
		offset += 4;

		/*
		 * long thirdKicker:(4);
		 */
		result |= (thirdKicker << offset);
		offset += 4;

		/*
		 * long secondKicker:(4);
		 */
		result |= (secondKicker << offset);
		offset += 4;

		/*
		 * long firstKicker:(4);
		 */
		result |= (firstKicker << offset);
		offset += 4;

		/*
		 * long onePair:(4);
		 */
		result |= (onePair << offset);
		offset += 4;

		/*
		 * long twoPair:(4);
		 */
		result |= (twoPairs << offset);
		offset += 4;

		/*
		 * long threeOfKind:(4);
		 */
		result |= (threeOfKind << offset);
		offset += 4;

		/*
		 * long straight:(4);
		 */
		result |= (straight << offset);
		offset += 4;

		/*
		 * long flush:(1);
		 */
		result |= (flush << offset);
		offset += 1;

		/*
		 * long fullHouse:(1);
		 */
		result |= (fullHouse << offset);
		offset += 1;

		/*
		 * long fourOfKind:(1);
		 */
		result |= (fourOfKind << offset);
		offset += 1;

		/*
		 * long straightFlush:(1);
		 */
		result |= (straightFlush << offset);
		offset += 1;

		return (result);
	}
}
