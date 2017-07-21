/*
 * Copyright (C) 2013-2016 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.fx.view;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import rl.photoviewer.model.KeywordExpression;
import rl.photoviewer.model.MapData;
import rl.photoviewer.model.PVModel;
import rl.photoviewer.model.PhotoMetadata;
import rl.util.exceptions.ErrorHandler;
import rl.util.exceptions.PersistenceException;
import rl.util.persistence.PropertyManager;

/**
 * Controller which is responsible for handling all kinds of user events. It
 * mediates between view and model.
 * 
 * @author Ruediger Lunde
 *
 */
public class AgilePhotoViewerCtrl implements Initializable, Observer {

	static final int INFO_TAB_INDEX = 0;
	static final int VISIBILITY_TAB_INDEX = 1;
	static final int MAP_TAB_INDEX = 2;

	static final String SELECT_BTN_ID = "selectBtn";
	static final String FIRST_BTN_ID = "firstBtn";
	static final String PREV_BTN_ID = "prevBtn";
	static final String NEXT_BTN_ID = "nextBtn";
	static final String AND_BTN_ID = "andBtn";
	static final String DELETE_BTN_ID = "deleteBtn";

	@FXML
	private AnchorPane rootPane;

	@FXML
	private AnchorPane leftPane;

	@FXML
	private FlowPane controlPane;

	@FXML
	private ToggleButton slideShowBtn;

	@FXML
	private ComboBox<Sec> slideShowCombo;

	@FXML
	private ToggleButton sortByDateBtn;

	@FXML
	private ToggleButton undecorateBtn;

	@FXML
	private TextArea captionPane;

	@FXML
	private TabPane tabPane;

	@FXML
	private TextArea infoPane;

	@FXML
	private ComboBox<String> ratingCombo;

	@FXML
	private ListView<String> keywordLst;

	@FXML
	private ToggleButton notBtn;

	@FXML
	private TextArea keywordExpressionTxt;

	@FXML
	private Label statusLabel;

	@FXML
	private StackPane mapPane;

	@FXML
	private ImageView mapView;

	@FXML
	private StackPane rightPane;

	@FXML
	private ImageView photoView;

	private ControlPaneMenu controlPaneMenu;
	private MapViewMenu mapMenu;
	private ContextMenu photoViewMenu;

	private ImageViewCtrl photoViewCtrl = new ImageViewCtrl();
	private ImageViewCtrl mapViewCtrl = new ImageViewCtrl();
	private MapDataViewCtrl mapDataViewCtrl = new MapDataViewCtrl();

	private double defaultFontSize = 12; // set when calling initialize...
	protected File exportPath;
	private Timeline slideShowTimer;

	private PVModel model;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		SplitPane.setResizableWithParent(leftPane, Boolean.FALSE);
		defaultFontSize = statusLabel.getFont().getSize();
		slideShowCombo.getItems().addAll(new Sec(2), new Sec(4), new Sec(6), new Sec(8), new Sec(12), new Sec(20));
		slideShowCombo.setValue(new Sec(4));

		captionPane.setOnMouseClicked(ev -> model.selectNextPhoto());

		ratingCombo.getItems().addAll("No Rating Filter", ">= *", ">= **", ">= ***", ">= ****", ">= *****");
		ratingCombo.setValue("No Rating Filter");
		ratingCombo.setOnAction(ev -> {
			String val = ratingCombo.getValue();
			if (val.equals("No Rating Filter"))
				model.setVisibility(0, model.getVisibilityExpression());
			else
				model.setVisibility(val.length() - 3, model.getVisibilityExpression());
		});

		keywordLst.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
			if (n != null)
				onKeywordSelected(n);
		});

		photoViewCtrl.initialize(photoView, rightPane);
		photoViewCtrl.setLimitersEnabled(true);
		photoViewCtrl.setMaxScale(4);
		mapViewCtrl.initialize(mapView, mapPane);
		mapViewCtrl.setInitScale(1);

		rootPane.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);
		controlPane.setOnScroll(e -> {
			if (e.getDeltaY() > 0)
				model.selectNextPhoto();
			else
				model.selectPrevPhoto();
			// e.consume();
		});

		model = new PVModel();
		model.addObserver(this);
		mapDataViewCtrl.initialize(mapViewCtrl, model);
		mapViewCtrl.viewParamsProperty().addListener(e -> mapDataViewCtrl.update(null));

		rightPane.setOnContextMenuRequested(this::onPhotoContextMenuRequest);

		controlPaneMenu = new ControlPaneMenu(this, model);
		controlPane.setOnContextMenuRequested(controlPaneMenu::show);

		mapMenu = new MapViewMenu(mapDataViewCtrl, model);
		mapPane.setOnContextMenuRequested(mapMenu::show);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg == PVModel.SELECTED_PHOTO_CHANGED) {
			Image image;
			try {
				PhotoMetadata data = model.getSelectedPhotoData();
				if (data != null) {
					image = new Image(model.getSelectedPhoto().toURI().toURL().toExternalForm());
					captionPane.setText(data.getCaption());
				} else {
					image = null;
					captionPane.setText("");
				}
				photoViewCtrl.setImage(image);
				updateInfoPane();
			} catch (MalformedURLException e) {
				e.printStackTrace(); // should never happen...
			}
		} else if (arg == PVModel.SELECTED_MAP_CHANGED) {
			Image image = null;
			try {
				MapData mapData = model.getMapData();
				if (mapData.getFile() != null) {
					image = new Image(mapData.getFile().toURI().toURL().toExternalForm());
				}
				mapViewCtrl.setImage(image);
			} catch (MalformedURLException e) {
				e.printStackTrace(); // should never happen...
			}
		} else if (arg == PVModel.METADATA_CHANGED) {
			ObservableList<String> items = FXCollections.observableArrayList(model.getAllKeywords());
			keywordLst.getSelectionModel().clearSelection();
			keywordLst.setItems(items);
			ratingCombo.getSelectionModel().select(model.getRatingFilter());
		}
		mapDataViewCtrl.update(arg);
		keywordExpressionTxt.setText(model.getVisibilityExpression().toString());
		statusLabel.setText(model.getVisiblePhotoCount() + " Photo(s) visible.");
	}

	public void onKeyPressed(KeyEvent keyEvent) {
		if (keyEvent.getCode() == KeyCode.F) {
			AgilePhotoViewerApp.getCurrStage().setFullScreen(!AgilePhotoViewerApp.getCurrStage().isFullScreen());
		} else if (keyEvent.getCode() == KeyCode.PLUS) {
			setCaptionFontSize(getCaptionFontSize() + 2);
		} else if (keyEvent.getCode() == KeyCode.MINUS) {
			setCaptionFontSize(getCaptionFontSize() - 2);
		} else if (keyEvent.getCode() == KeyCode.PAGE_DOWN || keyEvent.getCode() == KeyCode.N) {
			model.selectNextPhoto();
		} else if (keyEvent.getCode() == KeyCode.PAGE_UP || keyEvent.getCode() == KeyCode.P) {
			model.selectPrevPhoto();
		}
		// keyEvent.consume();
	}

	@FXML
	protected void onButtonAction(ActionEvent event) {
		Node source = (Node) event.getSource();
		if (source.getId().equals(SELECT_BTN_ID))
			onSelectAction(event);
		else if (source.getId().equals(FIRST_BTN_ID))
			model.selectFirstPhoto();
		else if (source.getId().equals(PREV_BTN_ID))
			model.selectPrevPhoto();
		else if (source.getId().equals(NEXT_BTN_ID))
			model.selectNextPhoto();
		else if (source == slideShowBtn) {
			if (slideShowBtn.isSelected()) {
				slideShowTimer = new Timeline(new KeyFrame(Duration.millis(1000 * slideShowCombo.getValue().seconds),
						ae -> model.selectNextPhoto()));
				slideShowTimer.setCycleCount(Timeline.INDEFINITE);
				slideShowTimer.play();
			} else {
				slideShowTimer.stop();
			}
		} else if (source == slideShowCombo) {
			slideShowBtn.setSelected(false);
			if (slideShowTimer != null)
				slideShowTimer.stop();
		} else if (source == sortByDateBtn)
			model.setSortByDate(sortByDateBtn.isSelected());
		else if (source == undecorateBtn)
			AgilePhotoViewerApp.changeStage(undecorateBtn.isSelected());
		else if (source.getId().equals(AND_BTN_ID)) {
			model.getVisibilityExpression().addClause();
			notBtn.setSelected(false);
			keywordLst.getSelectionModel().clearSelection();
			model.setVisibility(model.getRatingFilter(), model.getVisibilityExpression());
		} else if (source.getId().equals(DELETE_BTN_ID)) {
			model.getVisibilityExpression().deleteLastClause();
			notBtn.setSelected(false);
			keywordLst.getSelectionModel().clearSelection();
			model.setVisibility(model.getRatingFilter(), model.getVisibilityExpression());
		}
	}

	private void onKeywordSelected(String newKeyword) {
		KeywordExpression expression = model.getVisibilityExpression();
		expression.addLiteral(newKeyword, notBtn.isSelected());
		notBtn.setSelected(false);
		model.setVisibility(model.getRatingFilter(), expression);
	}

	@FXML
	protected void onPhotoContextMenuRequest(ContextMenuEvent event) {
		if (photoViewMenu == null) {
			photoViewMenu = new ContextMenu();
			MenuItem mi = new MenuItem("Use as Map");
			photoViewMenu.getItems().add(mi);
			mi.setOnAction(e -> {
				model.setMap(model.getSelectedPhoto());
				tabPane.getSelectionModel().select(MAP_TAB_INDEX);
			});
		}
		photoViewMenu.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());

		// event.consume();
	}

	private void onSelectAction(ActionEvent event) {
		FileChooser selectChooser = new FileChooser();
		File curr = model.getSelectedPhoto();
		if (curr != null) {
			selectChooser.setInitialDirectory(curr.getParentFile());
			selectChooser.setInitialFileName(curr.getName());
		}
		File next = selectChooser.showOpenDialog(AgilePhotoViewerApp.getCurrStage());
		if (next != null)
			model.selectPhoto(next);
	}

	private void updateInfoPane() {
		PhotoMetadata data = model.getSelectedPhotoData();
		if (data != null)
			infoPane.setText(data.toString());
	}

	public double getCaptionFontSize() {
		return captionPane.getFont().getSize();
	}

	public void setCaptionFontSize(double size) {
		captionPane.setFont(new Font(size));
		double fontSize2 = Math.max(defaultFontSize, size / 2);
		infoPane.setFont(new Font(fontSize2));
		keywordLst.setStyle("-fx-font-size:" + fontSize2 + ";");
		keywordExpressionTxt.setFont(new Font(fontSize2));
		statusLabel.setFont(new Font(fontSize2));
		mapDataViewCtrl.setMaxMarkerSize(size * 1.5);
	}

	public void setStatus(String message) {
		tabPane.getSelectionModel().select(VISIBILITY_TAB_INDEX);
		statusLabel.setText(message);
	}

	/**
	 * Restores view settings according to the settings of the last session.
	 */
	public void restoreSession() {
		try {
			String home = System.getProperty("user.home");
			File propDir = new File(home, ".agilephotoviewer");
			if (!propDir.exists())
				propDir.mkdir();
			PropertyManager.setApplicationDataDirectory(propDir);
			PropertyManager pm = PropertyManager.getInstance();

			slideShowCombo.setValue(new Sec(pm.getIntValue("gui.slideshowsec", 5)));
			sortByDateBtn.setSelected(pm.getBooleanValue("gui.sortbydate", true));
			model.setSortByDate(sortByDateBtn.isSelected());
			setCaptionFontSize(pm.getDoubleValue("gui.fontsize", defaultFontSize * 2));
			tabPane.getSelectionModel().select(pm.getIntValue("gui.selectedtab", 0));
			String exp = pm.getStringValue("gui.outputfile", null);
			if (exp != null)
				exportPath = new File(exp);

			model.loadMapParamLookup();
			String map = pm.getStringValue("model.currmapfile", "");
			if (!map.isEmpty() && new File(map).exists())
				model.setMap(new File(map));
			String fileName = pm.getStringValue("model.currfile", null);
			if (fileName != null && model.getCurrDirectory() == null) {
				File f = new File(fileName);
				if (f.exists())
					model.selectPhoto(f);
			}

			// mapImagePanel.setShowAllPhotoPositions(pm.getBooleanValue(
			// "gui.showallphotopositions", true));
			// infoPanel.setShowCaptionInStatus(pm.getBooleanValue(
			// "gui.showcaptioninstatus", true));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Saves session settings. */
	public void storeSession() {
		PropertyManager pm = PropertyManager.getInstance();
		// pm.setValue("gui.window.width", frame.getSize().width);
		// pm.setValue("gui.window.height", frame.getSize().height);
		// pm.setValue("gui.window.dividerlocation",
		// splitPane.getDividerLocation());

		pm.setValue("gui.slideshowsec", slideShowCombo.getValue().getSeconds());
		pm.setValue("gui.sortbydate", sortByDateBtn.isSelected());
		// pm.setValue("gui.showallphotopositions",
		// mapImagePanel.isShowAllPhotoPositions());
		pm.setValue("gui.fontsize", captionPane.getFont().getSize());
		// pm.setValue("gui.showcaptioninstatus",
		// infoPanel.isShowCaptionInStatus());
		pm.setValue("gui.selectedtab", tabPane.getSelectionModel().getSelectedIndex());
		if (exportPath != null)
			pm.setValue("gui.outputfile", exportPath.getAbsolutePath());
		if (model.getSelectedPhoto() != null)
			pm.setValue("model.currfile", model.getSelectedPhoto());
		File file = model.getMapData().getFile();
		pm.setValue("model.currmapfile", file != null ? file.getAbsolutePath() : "");
		model.saveMapParamLookup();
		try {
			pm.saveSessionProperties();
		} catch (PersistenceException ex) {
			ErrorHandler.getInstance().handleError(ex);
		}
	}

	public static class Sec {
		int seconds;

		private Sec(int sec) {
			seconds = sec;
		}

		public int getSeconds() {
			return seconds;
		}

		public String toString() {
			return seconds + " sec";
		}
	}
}
