package rl.photoviewer.fx.view;

import java.io.File;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import rl.photoviewer.model.PVModel;
import rl.util.exceptions.ErrorHandler;
import rl.util.exceptions.PersistenceException;

public class ContextMenuControlPane {

	AgilePhotoViewerController mainController;
	PVModel model;

	private ContextMenu menu;
	private MenuItem aboutItem;
	private CheckMenuItem fullScreenItem;
	private MenuItem exportItem;
	MenuItem exitItem;

	public ContextMenuControlPane(AgilePhotoViewerController mainController, PVModel model) {
		this.model = model;
		this.mainController = mainController;
		
		menu = new ContextMenu();
		aboutItem = new MenuItem("About");
		aboutItem.setOnAction(e -> {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("About");
			alert.setHeaderText("Agile Photo Viewer FX");

			final WebView browser = new WebView();
			final WebEngine webEngine = browser.getEngine();
			ScrollPane scrollPane = new ScrollPane();
			scrollPane.setContent(browser);
			alert.getDialogPane().setContent(scrollPane);

			java.net.URL helpURL = getClass().getResource("About.html");
			if (helpURL != null) {
				webEngine.load(helpURL.toExternalForm());
			} else {
				Exception ex = new PersistenceException(
						"Couldn't find file: About.html");
				ErrorHandler.getInstance().handleError(ex);
			}

			alert.show();
		});

		fullScreenItem = new CheckMenuItem("Full Sceen Mode");
		fullScreenItem.setOnAction(e -> AgilePhotoViewerApp.getCurrStage()
				.setFullScreen(fullScreenItem.isSelected()));

		exportItem = new MenuItem("Export Visible Photos");
		exportItem.setOnAction(e -> {
			FileChooser exportChooser = new FileChooser();
			if (mainController.exportPath != null) {
				exportChooser.setInitialDirectory(mainController.exportPath.getParentFile());
				exportChooser.setInitialFileName("default");
			}
			File file = exportChooser.showSaveDialog(AgilePhotoViewerApp
					.getCurrStage());
			if (file != null) {
				
				mainController.setStatus("Exporting " + model.getVisiblePhotoCount()
						+ " photo(s) ...");
				String name = file.getName().equals("default") ? null : file
						.getName();
				mainController.exportPath = file;
				int copied = model.exportPhotos(model.getVisiblePhotos(),
						mainController.exportPath.getParentFile(), name);
				String txt = copied < model.getVisiblePhotoCount() ? " out of "
						+ model.getVisiblePhotoCount() : "";
						mainController.setStatus(copied + txt + " photo(s) exported.");
			}
		});

		exitItem = new MenuItem("Exit");
		exitItem.setOnAction(e -> {
			mainController.storeSession();
			Platform.exit();
		});
		menu.getItems().addAll(aboutItem, fullScreenItem, exportItem, exitItem);
	}

	public void prepare() {
		menu.hide();
		fullScreenItem.setSelected(AgilePhotoViewerApp.getCurrStage().isFullScreen());
	}

	public void show(ContextMenuEvent event) {
		prepare();
		menu.show((Node) event.getSource(), event.getScreenX(),
				event.getScreenY());
		// event.consume();
	}

	public void onMenuItemAction(ActionEvent event) {
	}
}
