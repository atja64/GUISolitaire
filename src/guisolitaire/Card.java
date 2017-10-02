package guisolitaire;

/**
 * A class to store a card including its value, suit and color.
 * @author Ashley Allen
 */
class Card {
	private final Suit suit;
	private final Value value;
	
	private boolean revealed = false, selected = false;
	
	/**
	 * Construct a new card with specified Suit and Value
	 * @param suit the Suit for the card to have
	 * @param value the Value for the card to have
	 */
	Card(Suit suit, Value value) {
		this.suit = suit;
		this.value = value;
	}
	
	/**
	 * Get the card's Suit
	 * @return the card's Suit
	 */
	Suit getSuit() {
		return suit;
	}
	
	/**
	 * Get the card's value
	 * @return the card's Value
	 */
	Value getValue() {
		return value;
	}
	
	/**
	 * Get the card's color
	 * @return the card's color
	 */
	CardColor getColor() {
		if (suit == Suit.HEART || suit == Suit.DIAMOND) {
			return CardColor.RED;
		} else {
			return CardColor.BLACK;
		}
	}
	
	/**
	 * Set this card's revealed to true
	 */
	void reveal() {
		revealed = true;
	}
	
	/**
	 * Get the value of revealed
	 * @return the value of revealed
	 */
	boolean isRevealed() {
		return revealed;
	}
	
	/**
	 * Toggle whether this card is selected or not
	 */
	void toggleSelected() {
		selected = !selected;
	}
	
	/**
	 * Get whether this card is selected
	 * @return true if this card is selected
	 */
	boolean isSelected() {
		return selected;
	}
	
	/**
	 * Get the name of this card 
	 * @return the name of the card in the format [value]of[suit]s
	 */
	String getName() {
		return value.toString().toLowerCase() + "of" + suit.toString().toLowerCase() + "s";
	}
}
