/*
 * Copyright (C) 2013-2016 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import rl.photoviewer.model.PVModel;
import rl.util.exceptions.ErrorHandler;
import rl.util.exceptions.PersistenceException;

/**
 * This class provides a context menu for the Control Pane.
 * 
 * @author Ruediger Lunde
 *
 */
public class ContextMenuControlPane {

	AgilePhotoViewerController mainController;
	PVModel model;

	private ContextMenu menu;
	private MenuItem aboutItem;
	private CheckMenuItem fullScreenItem;
	private MenuItem increaseFontSizeItem;
	private MenuItem decreaseFontSizeItem;
	private MenuItem exportItem;
	MenuItem exitItem;

	public ContextMenuControlPane(AgilePhotoViewerController mainController,
			PVModel model) {
		this.model = model;
		this.mainController = mainController;

		menu = new ContextMenu();
		aboutItem = new MenuItem("About");
		aboutItem.setOnAction(e -> onAboutAction(e));

		fullScreenItem = new CheckMenuItem("Full Sceen Mode");
		fullScreenItem.setOnAction(e -> AgilePhotoViewerApp.getCurrStage()
				.setFullScreen(fullScreenItem.isSelected()));
		fullScreenItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));

		increaseFontSizeItem = new MenuItem("Increase Font Size");
		increaseFontSizeItem.setOnAction(e -> mainController
				.setCaptionFontSize(mainController.getCaptionFontSize() + 2));
		increaseFontSizeItem.setAccelerator(new KeyCodeCombination(KeyCode.PLUS, KeyCombination.CONTROL_DOWN));

		decreaseFontSizeItem = new MenuItem("Decrease Font Size");
		decreaseFontSizeItem.setOnAction(e -> mainController
				.setCaptionFontSize(mainController.getCaptionFontSize() - 2));
		decreaseFontSizeItem.setAccelerator(new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN));
		
		exportItem = new MenuItem("Export Visible Photos");
		exportItem.setOnAction(e -> onExportAction(e));
		
		exitItem = new MenuItem("Exit");
		exitItem.setOnAction(e -> {
			mainController.storeSession();
			Platform.exit();
		});
		menu.getItems().addAll(aboutItem, fullScreenItem, increaseFontSizeItem, decreaseFontSizeItem, exportItem, exitItem);
	}

	public void show(ContextMenuEvent event) {
		prepare();
		menu.show((Node) event.getSource(), event.getScreenX(),
				event.getScreenY());
		// event.consume();
	}

	private void prepare() {
		menu.hide();
		fullScreenItem.setSelected(AgilePhotoViewerApp.getCurrStage()
				.isFullScreen());
	}

	public void onAboutAction(ActionEvent event) {
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
	}

	private void onExportAction(ActionEvent event) {
		FileChooser exportChooser = new FileChooser();
		if (mainController.exportPath != null) {
			exportChooser.setInitialDirectory(mainController.exportPath
					.getParentFile());
			exportChooser.setInitialFileName("default");
		}
		File file = exportChooser.showSaveDialog(AgilePhotoViewerApp
				.getCurrStage());
		if (file != null) {

			mainController.setStatus("Exporting "
					+ model.getVisiblePhotoCount() + " photo(s) ...");
			String name = file.getName().equals("default") ? null : file
					.getName();
			mainController.exportPath = file;
			int copied = model.exportPhotos(model.getVisiblePhotos(),
					mainController.exportPath.getParentFile(), name);
			String txt = copied < model.getVisiblePhotoCount() ? " out of "
					+ model.getVisiblePhotoCount() : "";
			mainController.setStatus(copied + txt + " photo(s) exported.");
		}
	}
}
