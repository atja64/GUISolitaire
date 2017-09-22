package guisolitaire;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 *
 * @author Ashley Allen
 */
public class Game {
	private final Stack<Card> pack = new Stack<>();
	private final Stack<Card> waste = new Stack<>();
	private final List<Stack<Card>> board = new ArrayList<>(), podium = new ArrayList<>();
	private final Map<String, Image> imageCache = new HashMap<>();
	private final Map<String, Bounds> bounds = new HashMap<>();
	private final Random random = new Random();
	private final GraphicsContext gc;
	private final double CARD_WIDTH = 100, CARD_HEIGHT = 145.2, ROUNDING_FACTOR = 10;
	
	private Card selected;
	
	Game(GraphicsContext gc) {
		this.gc = gc;
		initVars();
		fillBounds();
		loadImages();
		fillPack();
		shufflePack();
		layBoard();
		drawGame();
	}
	
	/**
	 * Initialises the 2D ArrayLists to avoid NullPointer exceptions
	 */
	private void initVars() {
		for (int i = 0; i < 7; i++) {
			board.add(new Stack<>());
		}
		for (int i = 0; i < 4; i++) {
			podium.add(new Stack<>());
		}
	}
	
	private void fillBounds() {
		double y1 = 25;
		double y2 = 50 + CARD_HEIGHT;
		double[] x = new double[7];
		for (int i = 0; i < 7; i++) {
			x[i] = 25 + i * (CARD_WIDTH + 25);
		}
		bounds.put("hand", new Bounds(x[0], y1, x[0] + CARD_WIDTH, y1 + CARD_HEIGHT));
		bounds.put("waste", new Bounds(x[1], y1, x[1] + CARD_WIDTH, y1 + CARD_HEIGHT));
		for (int i = 0; i < 7; i++) {
			bounds.put("board" + i, new Bounds(x[i], y2, x[i] + CARD_WIDTH, y2 + CARD_HEIGHT + 15 * 25));
		}
		for (int i = 0; i < 4; i++) {
			bounds.put("podium" + i, new Bounds(x[3 + i], y1, x[3 + i] + CARD_WIDTH, y1 + CARD_HEIGHT));
		}
	}
	
	/**
	 * Fill the pack Stack with each card in a 52 card deck
	 */
	private void fillPack() {
		pack.clear();
		for (Suit suit : Suit.values()) {
			for (Value value : Value.values()) {
				pack.push(new Card(suit, value));
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
		Card temp = pack.get(i1);
		pack.set(i1, pack.get(i2));
		pack.set(i2, temp);
	}
	
	/**
	 * Lay out the board from the pack of cards. Essentially places
	 * sequentially increasing stacks of cards on each space of the board
	 */
	private void layBoard() {
		for (int i = 0; i < 7; i++) {
			Stack<Card> stack = board.get(i);
			for (int j = 0; j < i; j++) {
				stack.push(pack.pop());
			}
			Card lastCard = pack.pop();
			lastCard.reveal();
			stack.push(lastCard);
			board.add(stack);
		}
	}
	
	/**
	 * Turns the hand by taking the top card of the pack and placing it in the
	 * waste face up.
	 */
	private void turnHand() {
		waste.push(pack.pop());
	}
	
	/**
	 * Place all the cards in the waste back into the pack.
	 */
	private void resetHand() {
		int size = waste.size();
		for (int i = 0; i < size; i++) {
			pack.push(waste.pop());
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
	
	/**
	 * Draw the game board including the hand, waste, board and podium.
	 */
	private void drawGame() {
		gc.clearRect(0, 0, 900, 600);

		//Draw the hand
		Coordinates coords = bounds.get("hand").getTopLeft();
		if (pack.isEmpty()) {
			drawEmpty(coords);
		} else {
			drawCardBack(coords);
		}
		
		//Draw the waste
		coords = bounds.get("waste").getTopLeft();
		if (waste.isEmpty()) {
			drawEmpty(coords);
		} else {
			drawCard(waste.peek(), coords);
		}
		
		//Draw the main board
		for (int i = 0; i < 7; i++) {
			coords = bounds.get("board" + i).getTopLeft();
			Stack<Card> stack = board.get(i);
			if (stack.isEmpty()) {
				drawEmpty(coords);
			} else {
				for (int j = 0; j < stack.size(); j++) {
					Card card = stack.get(j);
					if (card.isRevealed()) {
						drawCard(card, coords);
					} else {
						drawCardBack(coords);
					}
					coords = coords.addY(25);
				}
			}
		}
		
		//Draw the podium
		for (int i = 0; i < 4; i++) {
			coords = bounds.get("podium" + i).getTopLeft();
			Stack<Card> stack = podium.get(i);
			if (stack.isEmpty()) {
				drawEmpty(coords);
			} else {
				drawCard(stack.peek(), coords);
			}
		}
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
	 * Draw an empty space at the specified coordinates
	 * @param x the x coordinate of the upper left corner to draw from
	 * @param y the y coordinate of the upper left corner to draw from
	 */
	private void drawEmpty(Coordinates coords) {
		gc.setStroke(Color.WHITE);
		gc.setLineWidth(1);
		gc.strokeRoundRect(coords.getX(), coords.getY(), CARD_WIDTH, CARD_HEIGHT, ROUNDING_FACTOR, ROUNDING_FACTOR);
	}
	
	/**
	 * Draw a card at the specified coordinates
	 * @param card the card to be drawn
	 * @param x the x coordinate of the upper left corner to draw from
	 * @param y the y coordinate of the upper left corner to draw from
	 */
	private void drawCard(Card card, Coordinates coords) {
		String filename = card.getValue().toString().toLowerCase() + "of" + card.getSuit().toString().toLowerCase() + "s";
		gc.drawImage(imageCache.get(filename), coords.getX(), coords.getY(), CARD_WIDTH, CARD_HEIGHT);
		if (card.isSelected()) {
			gc.setStroke(Color.LIGHTBLUE);
			gc.setLineWidth(3.5);
			gc.strokeRoundRect(coords.getX(), coords.getY(), CARD_WIDTH, CARD_HEIGHT, ROUNDING_FACTOR, ROUNDING_FACTOR);
		}
	}
	
	/**
	 * Draw a card back at the specified coordinates
	 * @param x the x coordinate of the upper left corner to draw from
	 * @param y the y coordinate of the upper left corner to draw from
	 */
	private void drawCardBack(Coordinates coords) {
		gc.drawImage(imageCache.get("cardback"), coords.getX(), coords.getY(), CARD_WIDTH, CARD_HEIGHT);	
	}
	
	/**
	 * Handle the MouseEvent generated by the OnMouseClicked from the Canvas element
	 * @param me the MouseEvent to be handled
	 */
	public void handleMouseClicked(MouseEvent me) {
		Coordinates coords = new Coordinates(me.getX(), me.getY());
		
		//If the hand is clicked turn the hand or reset it accordingly.
		if (bounds.get("hand").isInBounds(coords)) {
			if (pack.isEmpty()) {
				resetHand();
			} else {
				turnHand();
			}
		}
		
		if (bounds.get("waste").isInBounds(coords)) {
			if (!waste.isEmpty()) {
				if (waste.peek() == selected) {
					selected = null;
				} else {
					selected = waste.peek();
				}
				waste.peek().toggleSelected();
			}
		}
		
		//Draw the game and consume the MouseEvent
		drawGame();
		me.consume();
	}
}
