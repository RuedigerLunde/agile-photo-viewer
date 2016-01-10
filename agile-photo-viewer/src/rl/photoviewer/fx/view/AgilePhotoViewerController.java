package rl.photoviewer.fx.view;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
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
	private AnchorPane rightPane;

	@FXML
	private ScrollPane imageScrollPane;

	@FXML
	private ImageView imageView;

	private Image currImage;

	private PVModel model;

	final DoubleProperty zoomProperty = new SimpleDoubleProperty(1);
	final DoubleProperty transXProperty = new SimpleDoubleProperty(0);
	final DoubleProperty transYProperty = new SimpleDoubleProperty(0);

	final EventHandler<KeyEvent> keyEventHandler = new EventHandler<KeyEvent>() {
		public void handle(final KeyEvent keyEvent) {
			System.out.println("> " + keyEvent.getText());
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

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		SplitPane.setResizableWithParent(leftPane, Boolean.FALSE);
		rightPane.setMinWidth(0);
		rightPane.boundsInParentProperty().addListener(new ChangeListener<Bounds>() {
			@Override
			public void changed(ObservableValue<? extends Bounds> arg0, Bounds b1, Bounds b2) {
				if (Math.abs(b1.getHeight() - b2.getHeight()) > 5 && Math.abs(b1.getHeight() - b2.getHeight()) > 5)
					scaleToFit();
			}
		});
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

		zoomProperty.addListener(new InvalidationListener() {
			@Override
			public void invalidated(javafx.beans.Observable obs) {
				// imageView.scaleXProperty().set(zoomProperty.get());
				// imageView.scaleYProperty().set(zoomProperty.get());
				imageView.setFitWidth(zoomProperty.get() * currImage.getWidth());
				imageView.setFitHeight(zoomProperty.get() * currImage.getHeight());
			}
		});

		imageScrollPane.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent event) {
				double hvalue = imageScrollPane.hvalueProperty().get();
				double vvalue = imageScrollPane.vvalueProperty().get();
				if (event.getDeltaY() > 0) {
					zoomProperty.set(zoomProperty.get() * 1.1);
				} else if (event.getDeltaY() < 0) {
					zoomProperty.set(zoomProperty.get() / 1.1);
				}
				System.out.println(
						0.5 * imageScrollPane.hvalueProperty().get() + 0.5 * event.getX() / rightPane.getWidth());
				imageScrollPane.hvalueProperty().set(0.7 * hvalue + 0.3 * event.getX() / rightPane.getWidth());
				imageScrollPane.vvalueProperty().set(0.7 * vvalue + 0.3 * event.getY() / rightPane.getHeight());
//				System.out.println(event.getX() + " " + event.getY() + " : " + rightPane.getWidth() + " : "
//						+ imageScrollPane.hvalueProperty().get() + " Min " + imageScrollPane.getHmin() + " Max "
//						+ imageScrollPane.getHmax());
				event.consume();
			}
		});

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

	private void scaleToFit() {
		double zoomX = (rightPane.getWidth()) / currImage.getWidth();
		double zoomY = (rightPane.getHeight()) / currImage.getHeight();
		if (zoomX < zoomY) {
			zoomProperty.set(zoomX);
			imageView.translateYProperty().set((rightPane.getHeight() - zoomX * currImage.getHeight()) / 2.0);
			imageView.translateXProperty().set(0);
		} else {
			zoomProperty.set(zoomY);
			imageView.translateXProperty().set((rightPane.getWidth() - zoomY * currImage.getWidth()) / 2.0);
			imageView.translateYProperty().set(0);
		}
		// double zoom = Math.min(zoomX, zoomY);
		// zoomProperty.set(zoom);
		// imageView.translateXProperty().set(Math.max(((zoomX / zoom) - 1) *
		// rightPane.getBoundsInParent().getMaxX() / 2, 0));
		// imageView.translateYProperty().set(Math.max(((zoomY / zoom) - 1) *
		// rightPane.getBoundsInParent().getMaxY() / 2, 0));
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
			try {
				PhotoMetadata data = model.getSelectedPhotoData();
				if (data != null) {
					currImage = new Image(model.getSelectedPhoto().toURI().toURL().toExternalForm());
					statusPane.setText(data.getCaption());
				} else {
					currImage = null;
					statusPane.setText("");
				}
				imageView.setImage(currImage);
				scaleToFit();
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
