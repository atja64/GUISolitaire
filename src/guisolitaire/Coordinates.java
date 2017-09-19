package guisolitaire;

/**
 *
 * @author Ashley Allen
 */
public class Coordinates {
	private double x, y;
	
	Coordinates(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public Coordinates add(Coordinates coords) {
		return new Coordinates(x + coords.getX(), y + coords.getY());
	}
	
	public Coordinates addX(double x) {
		return new Coordinates(this.x + x, y);
	}
	
	public Coordinates addY(double y) {
		return new Coordinates(x, this.y + y);
	}
}
