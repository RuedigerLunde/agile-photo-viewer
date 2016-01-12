package rl.photoviewer.fx.view;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.layout.HBox;
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
	private SplitPane splitPane;

	@FXML
	private AnchorPane leftPane;

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
	private HBox mapPane;

	@FXML
	private ImageView mapView;
	
	@FXML
	private HBox rightPane;

	@FXML
	private ImageView photoView;

	private ContextMenu contextMenu;
	
	private ContextMenu mapMenu;
	
	private Timeline slideShowTimer;

	private PVModel model;

	final EventHandler<KeyEvent> keyEventHandler = new EventHandler<KeyEvent>() {
		public void handle(final KeyEvent keyEvent) {
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
	};

	private PhotoViewController photoViewController = new PhotoViewController();
	private MapViewController mapViewController = new MapViewController();

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
		mapViewController.initialize(mapView, mapPane);

		splitPane.addEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler);
		model = new PVModel();
		model.addObserver(this);
		restoreSession();
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
	public void onContextMenuRequest(ContextMenuEvent event) {
		if (contextMenu == null) {
			contextMenu = new ContextMenu();
			MenuItem mi = new MenuItem("Show in Map");
			contextMenu.getItems().add(mi);
			mi.setOnAction(e -> {
				model.setMap(model.getSelectedPhoto());
			});
		}
		contextMenu.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());
			
		event.consume();
	}
	
	@FXML
	public void onMapMenuRequest(ContextMenuEvent event) {
		if (mapMenu == null) {
			mapMenu = new ContextMenu();
			MenuItem mi = new MenuItem("Clear Map");
			mapMenu.getItems().add(mi);
			mi.setOnAction(e -> {
				model.clearCurrentMap();
			});
		}
		mapMenu.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());
			
		event.consume();
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
				tabPane.getSelectionModel().selectLast(); // select map tab
			} catch (MalformedURLException e) {
				e.printStackTrace(); // should never happen...
			}
		}
		keywordLst.getItems().addAll(model.getAllKeywords());
		keywordExpressionTxt.setText(model.getVisibilityExpression().toString());
		statusLabel.setText(model.getVisiblePhotoCount() + " Photo(s) visible.");
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
		StringBuffer text = new StringBuffer();
		PhotoMetadata data = model.getSelectedPhotoData();
		if (data != null) {
			text.append("File:\n  " + data.getFileName());
			if (data.getCaption() != null)
				text.append("\nCaption:\n  " + data.getCaption());
			if (data.getRating() != 0)
				text.append("\nRating:\n  " + "******".substring(0, data.getRating()));
			if (data.getDate() != null)
				text.append("\nDate:\n  " + data.getDate());
			if (data.getModel() != null)
				text.append("\nModel:\n  " + data.getModel());
			if (!Double.isNaN(data.getLat())) {
				DecimalFormat df = new DecimalFormat("###.####");
				text.append("\nLat:\n  " + df.format(data.getLat()) + "\nLon:\n  " + df.format(data.getLon()));
			}
			if (!data.getKeywords().isEmpty()) {
				text.append("\nKeywords:");
				for (String key : data.getKeywords())
					text.append("\n  " + key);
			}
		}
		infoPane.setText(text.toString());
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

			model.loadMapParamLookup();
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
		// pm.setValue("gui.selectedtab", tabbedPane.getSelectedIndex());
		// File file = outputFileChooser.getSelectedFile();
		// if (file != null)
		// pm.setValue("gui.outputfile", file.getAbsolutePath());
		// else if (model.getCurrDirectory() != null)
		// pm.setValue("gui.outputfile", model.getCurrDirectory());
		if (model.getSelectedPhoto() != null)
			pm.setValue("model.currfile", model.getSelectedPhoto());
		// File file = model.getMapData().getFile();
		// pm.setValue("model.currmapfile", file != null ?
		// file.getAbsolutePath()
		// : "");
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
