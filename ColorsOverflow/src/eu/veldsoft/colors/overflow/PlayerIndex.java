package eu.veldsoft.colors.overflow;

/**
 * 
 * @author Todor Balabanov
 */
enum PlayerIndex {
	FIRST(1, "Red Player"), SECOND(2, "Blue Player"), THIRD(3, "Purple Player"), FOURTH(
			4, "Green Player"), FIFTH(5, "Yellow Player"), SIXTH(6,
			"Brown Player");

	private int index;

	private String tag;

	static public PlayerIndex index(int index) {
		if (index == FIRST.index) {
			return FIRST;
		}
		if (index == SECOND.index) {
			return SECOND;
		}
		if (index == THIRD.index) {
			return THIRD;
		}
		if (index == FOURTH.index) {
			return FOURTH;
		}
		if (index == FIFTH.index) {
			return FIFTH;
		}
		if (index == SIXTH.index) {
			return SIXTH;
		}

		return null;
	}

	private PlayerIndex(int index, String tag) {
		this.index = index;
		this.tag = tag;
	}

	public String tag() {
		return tag;
	}

	public PlayerIndex next() {
		if (this == FIRST) {
			return SECOND;
		} else if (this == SECOND) {
			return THIRD;
		} else if (this == THIRD) {
			return FOURTH;
		} else if (this == FOURTH) {
			return FIFTH;
		} else if (this == FIFTH) {
			return SIXTH;
		} else if (this == SIXTH) {
			return FIRST;
		}

		return null;
	}

	public int empty() {
		return index << 8;
	}

	public int small() {
		return (index << 8) + 1;
	}

	public int middle() {
		return (index << 8) + 2;
	}

	public int large() {
		return (index << 8) + 3;
	}
}