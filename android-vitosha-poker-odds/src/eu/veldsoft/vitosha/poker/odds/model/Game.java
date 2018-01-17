package eu.veldsoft.vitosha.poker.odds.model;

/**
 * Game class.
 * 
 * @author Todor Balabanov
 * 
 * @email tdb@tbsoft.eu
 * 
 * @date 09 Aug 2012
 */
class Game {
	/**
	 * Deck of cards used in game.
	 */
	private Deck deck = new Deck();

	/**
	 * Game board.
	 */
	private Board board = new Board();

	/**
	 * Players in game.
	 */
	private Player[] players = new Player[Constants.MAX_NUMBER_OF_PLAYERS];

	/**
	 * Number of players.
	 */
	private int numberOfPlayers;

	/**
	 * The pot on board.
	 */
	private Pot pot = new Pot();

	/**
	 * Constructor without parameters.
	 * 
	 * @author Todor Balabanov
	 * 
	 * @email tdb@tbsoft.eu
	 * 
	 * @date 09 Aug 2012
	 */
	public Game() {
		for (int i = 0; i < players.length; i++) {
			players[i] = new Player();
		}
	}

	/**
	 * Get the deck of cards.
	 * 
	 * @return Deck reference.
	 */
	public Deck getDeck() {
		return deck;
	}

	/**
	 * Set the deck of cards.
	 * 
	 * @param deck
	 *            Deck reference.
	 */
	public void setDeck(Deck deck) {
		this.deck = deck;
	}

	/**
	 * Get the board.
	 * 
	 * @return Board reference.
	 */
	public Board getBoard() {
		return board;
	}

	/**
	 * Set the game board.
	 * 
	 * @param board
	 *            Board reference.
	 */
	public void setBoard(Board board) {
		this.board = board;
	}

	/**
	 * Get the players playing.
	 * 
	 * @return Array with players objects.
	 */
	public Player[] getPlayers() {
		return players;
	}

	/**
	 * Set the players in game.
	 * 
	 * @param players
	 *            Array with players objects.
	 */
	public void setPlayers(Player[] players) {
		this.players = players;
	}

	/**
	 * Get the number of players.
	 * 
	 * @return Number of players.
	 */
	public int getNumberOfPlayers() {
		return numberOfPlayers;
	}

	/**
	 * Set the number of players in game.
	 * 
	 * @param numberOfPlayers
	 *            Number of players.
	 */
	public void setNumberOfPlayers(int numberOfPlayers) {
		this.numberOfPlayers = numberOfPlayers;
	}

	/**
	 * Get the pot on board.
	 * 
	 * @return Pot reference.
	 */
	public Pot getPot() {
		return pot;
	}

	/**
	 * Set the pot.
	 * 
	 * @param pot
	 *            Pot reference.
	 */
	public void setPot(Pot pot) {
		this.pot = pot;
	}
}
