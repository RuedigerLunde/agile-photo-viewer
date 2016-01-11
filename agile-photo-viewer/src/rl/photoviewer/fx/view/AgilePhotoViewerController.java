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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import rl.photoviewer.model.KeywordExpression;
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
	private TextArea statusPane;

	@FXML
	private TextArea infoPane;

	@FXML
	private ComboBox<String> ratingCombo;

	@FXML
	private HBox rightPane;

	@FXML
	private ImageView imageView;

	private Timeline slideShowTimer;

	private PVModel model;

	final EventHandler<KeyEvent> keyEventHandler = new EventHandler<KeyEvent>() {
		public void handle(final KeyEvent keyEvent) {
			if (keyEvent.getCode() == KeyCode.PLUS) {
				statusPane.setFont(new Font(statusPane.getFont().getSize() + 1));
			} else if (keyEvent.getCode() == KeyCode.MINUS) {
				statusPane.setFont(new Font(statusPane.getFont().getSize() - 1));
			} else if (keyEvent.getCode() == KeyCode.PAGE_DOWN || keyEvent.getCode() == KeyCode.N) {
				model.selectNextPhoto();
			} else if (keyEvent.getCode() == KeyCode.PAGE_UP || keyEvent.getCode() == KeyCode.P) {
				model.selectPrevPhoto();
			}
			// keyEvent.consume();
		}
	};

	private PhotoViewController photoViewController = new PhotoViewController();

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
					model.setVisibility(0, new KeywordExpression());
				else
					model.setVisibility(val.length() - 3, new KeywordExpression());
			}
		});

		photoViewController.initialize(imageView, rightPane);

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
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg == PVModel.SELECTED_PHOTO_CHANGED) {
			Image image;
			try {
				PhotoMetadata data = model.getSelectedPhotoData();
				if (data != null) {
					image = new Image(model.getSelectedPhoto().toURI().toURL().toExternalForm());
					statusPane.setText(data.getCaption());
				} else {
					image = null;
					statusPane.setText("");
				}
				photoViewController.setImage(image);
				updateInfoPane();
			} catch (MalformedURLException e) {
				e.printStackTrace(); // should never happen...
			}
		}
	}

	private void openFileChooser() {
		FileChooser chooser = new FileChooser();
		File curr = model.getSelectedPhoto();
		if (curr != null) {
			chooser.setInitialDirectory(curr.getParentFile());
			chooser.setInitialFileName(curr.getName());
		}
		File next = chooser.showOpenDialog(null);
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
			statusPane.setFont(new Font(pm.getDoubleValue("gui.fontsize", 12)));
			
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
//		pm.setValue("gui.window.width", frame.getSize().width);
//		pm.setValue("gui.window.height", frame.getSize().height);
//		pm.setValue("gui.window.dividerlocation",
//				splitPane.getDividerLocation());
		
		pm.setValue("gui.slideshowsec", slideShowCombo.getValue().getSeconds());
		pm.setValue("gui.sortbydate", sortByDateBtn.isSelected());
//		pm.setValue("gui.showallphotopositions",
//				mapImagePanel.isShowAllPhotoPositions());
		pm.setValue("gui.fontsize", statusPane.getFont().getSize());
//		pm.setValue("gui.showcaptioninstatus",
//				infoPanel.isShowCaptionInStatus());
//		pm.setValue("gui.selectedtab", tabbedPane.getSelectedIndex());
//		File file = outputFileChooser.getSelectedFile();
//		if (file != null)
//			pm.setValue("gui.outputfile", file.getAbsolutePath());
//		else if (model.getCurrDirectory() != null)
//			pm.setValue("gui.outputfile", model.getCurrDirectory());
		if (model.getSelectedPhoto() != null)
			pm.setValue("model.currfile", model.getSelectedPhoto());
//		File file = model.getMapData().getFile();
//		pm.setValue("model.currmapfile", file != null ? file.getAbsolutePath()
//				: "");
		model.saveMapParamLookup();
		try {
			pm.saveSessionProperties();
		} catch (PersistenceException ex) {
			ErrorHandler.getInstance().handleError(ex);
		}
	}

}
