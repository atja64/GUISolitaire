package guisolitaire;

/**
 *
 * @author Ashley Allen
 */
public class Card {
	private final Suit suit;
	private final Value value;
	private final CardColor color;
	private boolean revealed = false, selected = false;
	
	/**
	 * Construct a new card with specified Suit and Value
	 * @param suit the Suit for the card to have
	 * @param value the Value for the card to have
	 */
	Card(Suit suit, Value value) {
		this.suit = suit;
		this.value = value;
		if (suit == Suit.HEART || suit == Suit.DIAMOND) {
			color = CardColor.RED;
		} else {
			color = CardColor.BLACK;
		}
	}
	
	/**
	 * Get the card's Suit
	 * @return the card's Suit
	 */
	public Suit getSuit() {
		return suit;
	}
	
	/**
	 * Get the card's value
	 * @return the card's Value
	 */
	public Value getValue() {
		return value;
	}
	
	public CardColor getColor() {
		return color;
	}
	
	/**
	 * Set this card's revealed to true
	 */
	public void reveal() {
		revealed = true;
	}
	
	/**
	 * Get the value of revealed
	 * @return the value of revealed
	 */
	public boolean isRevealed() {
		return revealed;
	}
	
	public void toggleSelected() {
		selected = !selected;
	}
	
	public boolean isSelected() {
		return selected;
	}
}
