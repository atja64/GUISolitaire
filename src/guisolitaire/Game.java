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
 *
 * @author Ashley Allen
 */
class Game {
	private final double CARD_WIDTH = 100, CARD_HEIGHT = 145.2,
		ROUNDING_FACTOR = 10, PADDING = 25;	
	
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

	private String alertText = null;
	private Card selected = null;
	
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
	
	private boolean isValidBoardMove(Card parent, Card child) {
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
	
	private void drawGame() {
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

		if (selected != null) {
			drawText(selected.getName(), 10, 590);
		} else {
			drawText("null", 10, 590);
		}

		if (alertText != null) {
			drawText(alertText, 890, 590, Color.RED, TextAlignment.RIGHT);
		}
	}
	
	private void drawCard(Card card, double x, double y) {
		gc.drawImage(imageCache.get(card.getName()), x, y, CARD_WIDTH, CARD_HEIGHT);
		if (card.isSelected()) {
			gc.setStroke(Color.LIGHTBLUE);
			gc.setLineWidth(3);
			gc.strokeRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, ROUNDING_FACTOR, ROUNDING_FACTOR);
		}
	}
	
	private void drawCardBack(double x, double y) {
		gc.drawImage(imageCache.get("cardback"), x, y, CARD_WIDTH, CARD_HEIGHT);
	}
	
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
	
	void handleMouseClicked(MouseEvent me) {
		double x = me.getX(), y = me.getY();

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
		
		//Handle board interactivity
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

	private void deselect() {
		if (selected != null) {
			selected.toggleSelected();
			selected = null;
		}
	}

	private void select(Card card) {
		selected = card;
		selected.toggleSelected();
	}

	private void handClicked() {
		if (hand.isEmpty()) {
			resetHand();
		} else {
			turnHand();
		}
		deselect();
	}
	
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

	private void drawText(String text, double x, double y) {
		drawText(text, x, y, Color.BLACK);
	}

	private void drawText(String text, double x, double y, Paint paint) {
		drawText(text, x, y, paint, TextAlignment.LEFT);
	}

	private void drawText(String text, double x, double y, Paint paint, TextAlignment textAlignment) {
		gc.setStroke(paint);
		gc.setTextAlign(textAlignment);
		gc.strokeText(text, x, y);
	}

	private void revealCards() {
		for (Stack<Card> stack : board) {
			Card card = stack.peek();
			if (!card.isRevealed()) {
				card.reveal();
			}
		}
	}
	
	private void finish(MouseEvent me) {
		revealCards();
		drawGame();
		me.consume();
	}
}
