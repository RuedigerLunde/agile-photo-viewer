/*
 * Copyright (C) 2013-2016 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.fx.view;

import java.util.Locale;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 * Central class for creating and running the Agile Photo Viewer FX application.
 * This version of the photo viewer shares the model with the Swing version but
 * uses JavaFX for graphics. The architecture of the viewer strictly follows the
 * model - view - view-model pattern.
 * 
 * @author Ruediger Lunde
 * 
 */
public class AgilePhotoViewerApp extends Application {
	private static Stage currStage;
	private static AgilePhotoViewerCtrl controller;
	private static EventHandler<WindowEvent> windowCloseHandler =
			event -> { if (currStage != null) controller.storeSession(); };

	@Override
	public void start(Stage primaryStage) {
		try {
			Locale.setDefault(Locale.ENGLISH);
			FXMLLoader loader = new FXMLLoader();
			Pane root = loader.load(getClass().getResource("AgilePhotoViewer.fxml").openStream());
			currStage = primaryStage;
			controller = loader.getController();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("AgilePhotoViewer.css").toExternalForm());
			primaryStage.setTitle("Agile Photo Viewer FX");
			primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("AgilePhotoViewerIcon.jpg")));
			primaryStage.setScene(scene);
			primaryStage.setOnCloseRequest(windowCloseHandler);
			primaryStage.show();
			controller.restoreSession();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Provides the currently used state. The controller uses it for showing
	 * dialogs.
	 */
	public static Stage getCurrStage() {
		return currStage;
	}

	/**
	 * Transfers the scene to a new stage. So it is possible to toggle between
	 * decorated and undecorated stages.
	 */
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

	/** Launches the photo viewer application */
	public static void main(String[] args) {
		launch(args);
	}
}
