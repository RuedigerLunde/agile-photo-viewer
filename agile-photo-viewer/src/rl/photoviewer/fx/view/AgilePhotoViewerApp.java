package rl.photoviewer.fx.view;

import java.util.Locale;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class AgilePhotoViewerApp extends Application {
	private static Stage currStage;
	private static AgilePhotoViewerController controller;
	private static EventHandler<WindowEvent> windowCloseHandler = new EventHandler<WindowEvent>() {
		@Override
		public void handle(WindowEvent event) {
			if (currStage != null)
				controller.storeSession();
		}
	};
	
	@Override
	public void start(Stage primaryStage) {
		try {
			Locale.setDefault(Locale.ENGLISH);
			FXMLLoader loader = new FXMLLoader();
			Pane root = loader.load(getClass().getResource("AgilePhotoViewer.fxml").openStream());
			currStage = primaryStage;
			controller = (AgilePhotoViewerController) loader.getController();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("AgilePhotoViewer.css").toExternalForm());
			// scene.getStylesheets().add(getClass().getResource("DarkTheme.css").toExternalForm());
			primaryStage.setTitle("Agile Photo Viewer FX");
			primaryStage.setScene(scene);
			primaryStage.setOnCloseRequest(windowCloseHandler);
			primaryStage.show();
			controller.restoreSession();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Stage getCurrStage() {
		return currStage;
	}

	public static void changeStage(boolean undecorated) {
		Stage newStage = new Stage(undecorated ? StageStyle.UNDECORATED : StageStyle.DECORATED);
		Stage oldStage = currStage;
		currStage = null; // session should not be stored now...
		newStage.setX(oldStage.getX());
		newStage.setY(oldStage.getY());
		newStage.setWidth(oldStage.getWidth());
		newStage.setHeight(oldStage.getHeight());
		newStage.setTitle(oldStage.getTitle());
		Scene scene = oldStage.getScene();
		oldStage.hide();
		oldStage.setScene(null);
		newStage.setScene(scene);
		newStage.setOnCloseRequest(windowCloseHandler);
		currStage = newStage;
		currStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
