package rl.photoviewer.fx.view;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
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
import javafx.scene.layout.HBox;
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

public class AgilePhotoViewerController implements Initializable, Observer {

	@FXML
	private AnchorPane rootPane;

	@FXML
	private AnchorPane leftPane;

	@FXML
	private FlowPane ctrlPane;

	@FXML
	private Button selectBtn;

	@FXML
	private Button firstBtn;

	@FXML
	private Button prevBtn;

	@FXML
	private Button nextBtn;

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
	private Button andBtn;

	@FXML
	private Button deleteBtn;

	@FXML
	private TextArea keywordExpressionTxt;

	@FXML
	private Label statusLabel;

	@FXML
	private StackPane mapPane;

	@FXML
	private ImageView mapView;

	@FXML
	private HBox rightPane;

	@FXML
	private ImageView photoView;

	private ContextMenu photoContextMenu;

	private ImageViewController photoViewController = new ImageViewController();
	private ImageViewController mapViewController = new ImageViewController();
	private MapDataViewController mapDataViewController = new MapDataViewController();

	private Timeline slideShowTimer;

	private PVModel model;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		SplitPane.setResizableWithParent(leftPane, Boolean.FALSE);
		slideShowCombo.getItems().addAll(new Sec(2), new Sec(4), new Sec(6), new Sec(8));
		slideShowCombo.setValue(new Sec(4));

		ratingCombo.getItems().addAll("No Rating Filter", ">= *", ">= **", ">= ***", ">= ****", ">= *****");
		ratingCombo.setValue("No Rating Filter");
		ratingCombo.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent ev) {
				String val = ratingCombo.getValue();
				if (val == "No Rating Filter")
					model.setVisibility(0, model.getVisibilityExpression());
				else
					model.setVisibility(val.length() - 3, model.getVisibilityExpression());
			}
		});

		keywordLst.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (newValue != null)
					onKeywordSelected(newValue);
			}
		});

		photoViewController.initialize(photoView, rightPane);
		photoViewController.setLimitersEnabled(true);
		photoViewController.setMaxScale(4);
		mapViewController.initialize(mapView, mapPane);
		mapViewController.setInitScale(1);

		rootPane.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			public void handle(final KeyEvent keyEvent) {
				onKeyPressed(keyEvent);
			}
		});
		ctrlPane.setOnScroll(e -> {
			if (e.getDeltaY() > 0)
				model.selectNextPhoto();
			else
				model.selectPrevPhoto();
			// e.consume();
		});
		
		rightPane.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
			@Override
			public void handle(ContextMenuEvent event) {
				onPhotoContextMenuRequest(event);
			}
		});
		
		model = new PVModel();
		model.addObserver(this);
		mapDataViewController.initialize(mapViewController, model);
		mapViewController.viewParamsProperty().addListener(e -> mapDataViewController.update(null));
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
				photoViewController.setImage(image);
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
				mapViewController.setImage(image);
			} catch (MalformedURLException e) {
				e.printStackTrace(); // should never happen...
			}
		} else if (arg == PVModel.METADATA_CHANGED) {
			ObservableList<String> items = FXCollections.observableArrayList(model.getAllKeywords());
			keywordLst.getSelectionModel().clearSelection();
			keywordLst.setItems(items);
		}
		mapDataViewController.update(arg);
		keywordExpressionTxt.setText(model.getVisibilityExpression().toString());
		statusLabel.setText(model.getVisiblePhotoCount() + " Photo(s) visible.");
	}

	public void onKeyPressed(KeyEvent keyEvent) {
		if (keyEvent.getCode() == KeyCode.PLUS) {
			captionPane.setFont(new Font(captionPane.getFont().getSize() + 1));
		} else if (keyEvent.getCode() == KeyCode.MINUS) {
			captionPane.setFont(new Font(captionPane.getFont().getSize() - 1));
		} else if (keyEvent.getCode() == KeyCode.PAGE_DOWN || keyEvent.getCode() == KeyCode.N) {
			model.selectNextPhoto();
		} else if (keyEvent.getCode() == KeyCode.PAGE_UP || keyEvent.getCode() == KeyCode.P) {
			model.selectPrevPhoto();
		}
		// keyEvent.consume();
	}

	@FXML
	protected void onButtonAction(ActionEvent event) {
		Object source = event.getSource();
		if (source == selectBtn)
			openFileChooser();
		else if (source == firstBtn)
			model.selectFirstPhoto();
		else if (source == prevBtn)
			model.selectPrevPhoto();
		else if (source == nextBtn)
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
		else if (source == andBtn) {
			model.getVisibilityExpression().addClause();
			notBtn.setSelected(false);
			keywordLst.getSelectionModel().clearSelection();
			model.setVisibility(model.getRatingFilter(), model.getVisibilityExpression());
		} else if (source == deleteBtn) {
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
		if (photoContextMenu == null) {
			photoContextMenu = new ContextMenu();
			MenuItem mi = new MenuItem("Use as Map");
			photoContextMenu.getItems().add(mi);
			mi.setOnAction(e -> {
				model.setMap(model.getSelectedPhoto());
				tabPane.getSelectionModel().selectLast(); // select map tab
			});
		}
		photoContextMenu.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());

		//event.consume();
	}

	private void openFileChooser() {
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
			captionPane.setFont(new Font(pm.getDoubleValue("gui.fontsize", 12)));
			tabPane.getSelectionModel().select(pm.getIntValue("gui.selectedtab", 0));

			model.loadMapParamLookup();
			String fileName = pm.getStringValue("model.currfile", null);
			String map = pm.getStringValue("model.currmapfile", "");
			if (!map.isEmpty() && new File(map).exists())
				model.setMap(new File(map));
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
		// File file = outputFileChooser.getSelectedFile();
		// if (file != null)
		// pm.setValue("gui.outputfile", file.getAbsolutePath());
		// else if (model.getCurrDirectory() != null)
		// pm.setValue("gui.outputfile", model.getCurrDirectory());
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
