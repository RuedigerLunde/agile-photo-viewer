package rl.photoviewer.fx.view;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class AgilePhotoViewerApp extends Application {
	private static Stage currStage;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			currStage = primaryStage;
			// primaryStage.initStyle(StageStyle.UNDECORATED);
			Pane root = FXMLLoader.load(getClass().getResource("AgilePhotoViewer.fxml"));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("AgilePhotoViewer.css").toExternalForm());
			//scene.getStylesheets().add(getClass().getResource("DarkTheme.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void changeStage(boolean undecorated) {
		Stage newStage = new Stage(undecorated ? StageStyle.UNDECORATED :StageStyle.DECORATED);
		newStage.setX(currStage.getX());
		newStage.setY(currStage.getY());
		newStage.setWidth(currStage.getWidth());
		newStage.setHeight(currStage.getHeight());
		System.out.println(newStage.getX());
		Scene scene = currStage.getScene();
		currStage.hide();
		currStage.setScene(null);
		newStage.setScene(scene);
		currStage = newStage;
		currStage.show();
	}
	
	
	public static void main(String[] args) {
		launch(args);
	}
}
