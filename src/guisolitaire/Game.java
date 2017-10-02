package guisolitaire;

import java.util.*;

import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * The main game controller class that handles all game functions and inputs
 * @author Ashley Allen
 */
class Game {
    //Constants
	private final double CARD_WIDTH = 100, CARD_HEIGHT = 145.2,
		ROUNDING_FACTOR = 10, PADDING = 25;	

	//Variables used to store the cards and bounds for the game
	private final Stack<Card> hand = new Stack<>(),
		waste = new Stack<>();
	private final List<Stack<Card>> board = new ArrayList<>(), foundations = new ArrayList<>();
	private final BoundingBox handBounds = new BoundingBox(PADDING, PADDING, CARD_WIDTH, CARD_HEIGHT),
		wasteBounds = new BoundingBox(PADDING * 2 + CARD_WIDTH, PADDING, CARD_WIDTH, CARD_HEIGHT);
	private final List<BoundingBox> foundationsBounds = new ArrayList<>();
	private final List<List<BoundingBox>> boardBounds = new ArrayList<>();

	private final Map<String, Image> imageCache = new HashMap<>();
	private final Random random = new Random();
	private final GraphicsContext gc;

	//Variables that can change during the game which need global access
	private String alertText = null;
	private Card selected = null;

    /**
     * The main constructor which initialises the game in its entirety and handles all the card movement before drawing
     * the game
     * @param gc the GraphicsContext to write to, should be gotten from a Canvas object.
     */
	Game(GraphicsContext gc) {
		this.gc = gc;
		initVars();
		loadImages();
		fillPack();
		shufflePack();
		layBoard();
		generateBoardBounds();
		revealCards();
		drawGame();
	}
	
	/**
	 * Initialises the 2D ArrayLists to avoid NullPointer exceptions
	 */
	private void initVars() {
		for (int i = 0; i < 4; i++) {
			foundations.add(new Stack<>());
			foundationsBounds.add(new BoundingBox(PADDING + (CARD_WIDTH + PADDING) * (3 + i), PADDING, CARD_WIDTH, CARD_HEIGHT));
		}
	}
	
	/**
	 * Fill the pack Stack with each card in a 52 card deck
	 */
	private void fillPack() {
		hand.clear();
		for (Suit suit : Suit.values()) {
			for (Value value : Value.values()) {
				hand.push(new Card(suit, value));
			}
		}
	}
	
	/**
	 * Shuffle the order of the pack. Uses a Fisher-Yates shuffle.
	 */
	private void shufflePack() {
		for (int i = 0; i < 52; i++) {
			swapCard(random.nextInt(52 - i), 51 - i);
		}
	}
	
	/**
	 * Swap the card at i1 in the pack with the card at i2
	 * @param i1 the index of the first card to be swapped
	 * @param i2 the index of the second card to be swapped
	 */
	private void swapCard(int i1, int i2) {
		Card temp = hand.get(i1);
		hand.set(i1, hand.get(i2));
		hand.set(i2, temp);
	}
	
	/**
	 * Lay out the board from the pack of cards. Essentially places
	 * sequentially increasing stacks of cards on each space of the board
	 */
	private void layBoard() {
		board.clear();
		for (int i = 0; i < 7; i++) {
			Stack<Card> stack = new Stack<>();
			for (int j = 0; j < i + 1; j++) {
				stack.push(hand.pop());
			}
			board.add(stack);
		}
	}

    /**
     * Generate a BoundingBox for each card on the board.
     * TODO: Possibly change to each Card having a BoundingBox
     */
	private void generateBoardBounds() {
		boardBounds.clear();
		for (int i = 0; i < 7; i++) {
			boardBounds.add(new ArrayList<>());
			Stack<Card> stack = board.get(i);
			for (int j = 0; j < stack.size(); j++) {
				boardBounds.get(i).add(new BoundingBox(PADDING + (CARD_WIDTH + PADDING) * i, PADDING * (2 + j) + CARD_HEIGHT, CARD_WIDTH, CARD_HEIGHT));
			}
		}
	}
	
	/**
	 * Turns the hand by taking the top card of the pack and placing it in the
	 * waste face up.
	 */
	private void turnHand() {
		waste.push(hand.pop());
	}
	
	/**
	 * Place all the cards in the waste back into the pack.
	 */
	private void resetHand() {
		int size = waste.size();
		for (int i = 0; i < size; i++) {
			hand.push(waste.pop());
		}
	}

    /**
     * Check whether placing the child on the parent would be a valid move according to the board rules
     * @param parent the parent Card to check
     * @param child the child Card to check
     * @return true if the move is valid
     */
	private boolean isValidBoardMove(Card parent, Card child) {
	    //Explicit if statements preserved for clarity
		if (parent == null) {
			return true;
		}
		if (parent.getValue() == Value.ACE) {
			return false;
		}
		if (parent.getColor() == child.getColor()) {
			return false;
		}
		if (parent.getValue().ordinal() != child.getValue().ordinal() + 1) {
			return false;
		}
		return true;
	}

    /**
     * Move the selected card to the specified Stack
     * TODO: Make more efficient than checking each Stack for the presence of the selected card
     * @param stack the Stack to move to
     */
	private void moveCard(Stack<Card> stack) {
		waste.remove(selected);
		for (Stack<Card> boardStack : board) {
			boardStack.remove(selected);
		}
		for (Stack<Card> foundStack : foundations) {
			foundStack.remove(selected);
		}
		stack.push(selected);
	}

    /**
     * Draw the game to the provided GraphicsContext
     */
	private void drawGame() {
	    //Clear the game canvas
		gc.clearRect(0, 0, 900, 600);
		
		//Draw the hand
		double x = PADDING, y = PADDING;
		if (hand.isEmpty()) {
			drawEmpty(x, y);
		} else {
			drawCardBack(x, y);
		}
		
		//Draw the waste
		x = PADDING * 2 + CARD_WIDTH;
		y = PADDING;
		if (waste.isEmpty()) {
			drawEmpty(x, y);
		} else {
			drawCard(waste.peek(), x, y);
		}
		
		//Draw the board
		for (int i = 0; i < 7; i++) {
			x = PADDING + (CARD_WIDTH + PADDING) * i;
			Stack<Card> stack = board.get(i);
			if (stack.isEmpty()) {
				drawEmpty(x, PADDING * 2 + CARD_HEIGHT);
			} else {
				for (int j = 0; j < stack.size(); j++) {
					y = PADDING * (2 + j) + CARD_HEIGHT;
					Card card = stack.get(j);
					if (card.isRevealed()) {
						drawCard(card, x, y);
					} else {
						drawCardBack(x, y);
					}
				}
			}
		}
		
		//Draw the foundations
		y = PADDING;
		for (int i = 0; i < 4; i++) {
			x = PADDING + (CARD_WIDTH + PADDING) * (3 + i);			
			Stack<Card> stack = foundations.get(i);
			if (stack.isEmpty()) {
				drawEmpty(x, y);
			} else {
				drawCard(stack.peek(), x, y);
			}
		}

		//Draw the card indicator text
		if (selected != null) {
			drawText(selected.getName(), 10, 590);
		} else {
			drawText("null", 10, 590);
		}

		//Draw the alert text
		if (alertText != null) {
			drawText(alertText, 890, 590, Color.RED, TextAlignment.RIGHT);
		}
	}

    /**
     * Draw the specified Card to the game canvas
     * @param card the card to draw
     * @param x the x coordinate to draw at
     * @param y the y coordinate to draw at
     */
	private void drawCard(Card card, double x, double y) {
		gc.drawImage(imageCache.get(card.getName()), x, y, CARD_WIDTH, CARD_HEIGHT);
		if (card.isSelected()) {
			gc.setStroke(Color.LIGHTBLUE);
			gc.setLineWidth(3);
			gc.strokeRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, ROUNDING_FACTOR, ROUNDING_FACTOR);
		}
	}

    /**
     * Draw a cardback to the game canvas
     * @param x the x coordinate to draw at
     * @param y the y coordinate to draw at
     */
	private void drawCardBack(double x, double y) {
		gc.drawImage(imageCache.get("cardback"), x, y, CARD_WIDTH, CARD_HEIGHT);
	}

    /**
     * Draw an empty card space to the game canvas
     * @param x the x coordinate to draw at
     * @param y the y coordinate to draw at
     */
	private void drawEmpty(double x, double y) {
		gc.setStroke(Color.WHITE);
		gc.setLineWidth(1);
		gc.strokeRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, ROUNDING_FACTOR, ROUNDING_FACTOR);
	}
	
	/**
	 * Load the images from the resource folder into the image cache for later use
	 */
	private void loadImages() {
		for (Suit suit : Suit.values()) {
			for (Value value : Value.values()) {
				String filename = value.toString().toLowerCase() + "of" + suit.toString().toLowerCase() + "s";
				imageCache.put(filename, new Image(this.getClass().getResourceAsStream("/res/cards/" +  filename + ".png")));
			}
		}
		imageCache.put("cardback", new Image(this.getClass().getResourceAsStream("/res/cards/cardback.png")));
	}

    /**
     * Handle the mouse being clicked
     * @param me the MouseEvent passed from the GUISolitaire class
     */
	void handleMouseClicked(MouseEvent me) {
		double x = me.getX(), y = me.getY();

        //Reset the alert text
		alertText = null;

		//Handle hand interactivity
		if (handBounds.contains(x, y)) {
			handClicked();
			finish(me);
			return;
		}
		
		//Handle waste interactivity
		if (wasteBounds.contains(x, y)) {
			wasteClicked();
			finish(me);
			return;
		}
		
		//Handle board interactivity. Checks the bounds for each card on the board to see if any have been clicked. If
        //the mouse was clicked at an overlap of bounds it selects the topmost one.
		boolean boardClicked = false;
		int indexX = -1, indexY = -1;
		for (int i = 0; i < 7; i++) {
			List<BoundingBox> boundsList = boardBounds.get(i);
			for (int j = 0; j < boundsList.size(); j++) {
				if (boundsList.get(j).contains(x, y) && board.get(i).get(j).isRevealed()) {
					indexX = i;
					indexY = j;
					boardClicked = true;
				}
			}
			if (boardClicked) {
				boardClicked(indexX, indexY);
				finish(me);
				return;
			}
		}
		
		//Handle foundations interactivity
		for (int i = 0; i < 4; i++) {
			if (foundationsBounds.get(i).contains(x, y)) {
				foundationsClicked(i);
				finish(me);
				return;
			}
		}

		//If nothing was clicked
		deselect();
		finish(me);
	}

    /**
     * Deselect the currently selected card
     */
	private void deselect() {
		if (selected != null) {
			selected.toggleSelected();
			selected = null;
		}
	}

    /**
     * Set the selected variable to be equal to a card
     * @param card the card to select
     */
	private void select(Card card) {
		selected = card;
		selected.toggleSelected();
	}

    /**
     * Handle the hand being clicked
     */
	private void handClicked() {
		if (hand.isEmpty()) {
			resetHand();
		} else {
			turnHand();
		}
		deselect();
	}

    /**
     * Handle the waste being clicked
     */
	private void wasteClicked() {
		if (!waste.isEmpty()) {
			Card card = waste.peek();
			if (selected == card) {
				deselect();
			} else {
				deselect();
				select(card);
			}
		} else {
			deselect();
		}
	}

    /**
     * Handles the board being clicked
     * @param indexX the column on the board clicked
     * @param indexY the card in the column clicked
     */
	private void boardClicked(int indexX, int indexY) {
		Stack<Card> stack = board.get(indexX);
		Card card = stack.get(indexY);
		if (selected == card) {
			deselect();
		} else if (selected != null & indexY == stack.size() - 1) {
			if (isValidBoardMove(card, selected)) {
				moveCard(stack);
				generateBoardBounds();
				deselect();
			} else {
				alertText = "Invalid move!";
				deselect();
			}
		} else {
			deselect();
			select(card);
		}
	}

    /**
     * Handle the foundations being clicked
     * @param index
     */
	private void foundationsClicked(int index) {
		Card card = foundations.get(index).peek();
		if (selected == card) {
			deselect();
		} else {
			deselect();
			select(card);
		}
		card.toggleSelected();
	}

    /**
     * Draw the specified text to the game canvas in black and with left text alignment.
     * @param text the text to draw
     * @param x the x coordinate to draw at
     * @param y the y coordinate to draw at
     */
	private void drawText(String text, double x, double y) {
		drawText(text, x, y, Color.BLACK);
	}

    /**
     * Draw the specified text to the game canvas with the specified Paint and with left text alignment.
     * @param text the text to draw
     * @param x the x coordinate to draw at
     * @param y the y coordinate to draw at
     * @param paint the Paint to draw the text with
     */
	private void drawText(String text, double x, double y, Paint paint) {
		drawText(text, x, y, paint, TextAlignment.LEFT);
	}

    /**
     * Draw the specified text to the game canvas with the specified Paint and TextAlignment
     * @param text the text to draw
     * @param x the x coordinate to draw at
     * @param y the y coordinate to draw at
     * @param paint the Paint to draw the text with
     * @param textAlignment the TextAlignment to draw with
     */
	private void drawText(String text, double x, double y, Paint paint, TextAlignment textAlignment) {
		gc.setStroke(paint);
		gc.setTextAlign(textAlignment);
		gc.strokeText(text, x, y);
	}

    /**
     * Reveal any cards which are not yet revealed and on the bottom of their board stack
     */
	private void revealCards() {
		for (Stack<Card> stack : board) {
			Card card = stack.peek();
			if (!card.isRevealed()) {
				card.reveal();
			}
		}
	}

    /**
     * Perform final cleanup for the handleMouseClicked() function
     * @param me the MouseEvent passed by handleMouseClicked()
     */
	private void finish(MouseEvent me) {
		revealCards();
		drawGame();
		me.consume();
	}
}
