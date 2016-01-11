package rl.photoviewer.fx.view;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import rl.photoviewer.model.KeywordExpression;
import rl.photoviewer.model.PVModel;
import rl.photoviewer.model.PhotoMetadata;
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
	private TextArea statusPane;

	@FXML
	private TextArea infoPane;

	@FXML
	private ComboBox<String> ratingCombo;

	@FXML
	private HBox rightPane;

	@FXML
	private ImageView imageView;

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
		try {
			String home = System.getProperty("user.home");
			File propDir = new File(home, ".agilephotoviewer");
			if (!propDir.exists())
				propDir.mkdir();
			PropertyManager.setApplicationDataDirectory(propDir);
			PropertyManager pm = PropertyManager.getInstance();
			model = new PVModel();
			model.loadMapParamLookup();
			String fileName = pm.getStringValue("model.currfile", null);
			if (fileName != null && model.getCurrDirectory() == null) {
				File f = new File(fileName);
				if (f.exists())
					model.selectPhoto(f); // todo
			}
			model.addObserver(this);
			update(model, PVModel.SELECTED_PHOTO_CHANGED);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	protected void onButtonAction(ActionEvent event) {
		if (event.getSource() == selectBtn)
			openFileChooser();
		else if (event.getSource() == firstBtn)
			model.selectFirstPhoto();
		else if (event.getSource() == prevBtn)
			model.selectPrevPhoto();
		else if (event.getSource() == nextBtn)
			model.selectNextPhoto();
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
}
