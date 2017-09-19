package guisolitaire;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 *
 * @author Ashley Allen
 */
public class GUISolitaire extends Application {

	@Override
	public void start(Stage primaryStage) {
		Canvas canvas = new Canvas(900, 600);
		
		Game game = new Game(canvas.getGraphicsContext2D());
		
		canvas.setOnMouseClicked(game::handleMouseClicked);
		
		BorderPane root = new BorderPane(canvas);
		
		Scene scene = new Scene(root, Color.DARKGREEN);
		
		primaryStage.setScene(scene);
		primaryStage.setTitle("GUISolitaire");
		primaryStage.show();
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
