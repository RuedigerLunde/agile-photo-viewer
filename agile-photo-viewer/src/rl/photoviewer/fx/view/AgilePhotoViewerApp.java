package rl.photoviewer.fx.view;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class AgilePhotoViewerApp extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
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
	
	public static void main(String[] args) {
		launch(args);
	}
}
