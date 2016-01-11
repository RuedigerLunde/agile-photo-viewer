package rl.photoviewer.fx.view;
	
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
	
	@Override
	public void start(Stage primaryStage) {
		try {
			currStage = primaryStage;
			FXMLLoader loader = new FXMLLoader();
			Pane root = loader.load(getClass().getResource("AgilePhotoViewer.fxml").openStream());
			final AgilePhotoViewerController controller = (AgilePhotoViewerController) loader.getController();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("AgilePhotoViewer.css").toExternalForm());
			//scene.getStylesheets().add(getClass().getResource("DarkTheme.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					if (currStage != null)
						controller.storeSession();
				}});
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Stage getCurrStage() {
		return currStage;
	}
	
	public static void changeStage(boolean undecorated) {
		Stage newStage = new Stage(undecorated ? StageStyle.UNDECORATED :StageStyle.DECORATED);
		Stage oldStage = currStage;
		currStage = null; // session should not be stored now...
		newStage.setX(oldStage.getX());
		newStage.setY(oldStage.getY());
		newStage.setWidth(oldStage.getWidth());
		newStage.setHeight(oldStage.getHeight());
		Scene scene = oldStage.getScene();
		oldStage.hide();
		oldStage.setScene(null);
		newStage.setScene(scene);
		currStage = newStage;
		currStage.show();
	}
	
	
	public static void main(String[] args) {
		launch(args);
	}
}
