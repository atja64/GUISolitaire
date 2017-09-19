package guisolitaire;

/**
 *
 * @author Ashley Allen
 */
public class Bounds {
	private final Coordinates topLeft, botRight;
	
	Bounds(double x1, double y1, double x2, double y2) {
		this.topLeft = new Coordinates(x1, y1);
		this.botRight = new Coordinates(x2, y2);
	}
	
	public boolean isInBounds(Coordinates coords) {
		double x = coords.getX(), y = coords.getY();
		if (x >= topLeft.getX() && y >= topLeft.getY() && x < botRight.getX() && y < botRight.getY()) {
			return true;
		}
		return false;
	}
	
	public Coordinates getTopLeft() {
		return topLeft;
	}
}
