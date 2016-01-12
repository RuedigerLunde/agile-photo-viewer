package rl.photoviewer.fx.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class MapViewController {

	private ImageView imageView;

	private Image image;

	private Pane container;

	private double imgX;
	private double imgY;
	private double scale;

	private boolean enableLimiters = false;
	private boolean enableScaleToFit = true;

	public void initialize(ImageView imageView, Pane container) {
		this.imageView = imageView;
		this.container = container;
		container.setMinWidth(0);
		container.setMinHeight(0);

		imageView.setPreserveRatio(true);
		imageView.fitWidthProperty().bind(container.widthProperty());
		imageView.fitHeightProperty().bind(container.heightProperty());

		container.widthProperty().addListener(e -> update());
		container.heightProperty().addListener(e -> update());

		ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<>();

		imageView.setOnMousePressed(e -> {
			if (image != null) {
				Point2D mousePress = imageViewToImage(new Point2D(e.getX(), e.getY()));
				mouseDown.set(mousePress);
			}
		});

		imageView.setOnMouseDragged(e -> {
			if (image != null) {
				Point2D dragPoint = imageViewToImage(new Point2D(e.getX(), e.getY()));
				shift(dragPoint.subtract(mouseDown.get()));
				mouseDown.set(imageViewToImage(new Point2D(e.getX(), e.getY())));
				e.consume();
			}
		});

		imageView.setOnScroll(e -> {
			if (image != null) {
				double newScale = scale * Math.pow(1.01, e.getDeltaY());
				zoom(new Point2D(e.getX(), e.getY()), newScale);
			}
		});

		imageView.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2 && image != null) {
				if (!enableScaleToFit) {
					enableScaleToFit = true;
					update();
				} else {
					zoom(new Point2D(e.getX(), e.getY()), 1.0);
				}
			}
		});
	}

	public void setImage(Image image) {
		this.image = image;
		imageView.setImage(image);
		imgX = 0.0;
		imgY = 0.0;
		enableScaleToFit = true;
		update();
	}

	public void setLimitersEnabled(boolean state) {
		enableLimiters = state;
		update();
	}
	
	// reset to the top left:
	private void update() {
		if (image != null && container.getWidth() > 0) {
			double scaleFit = computeScaleToFit();
			if (enableScaleToFit) {
				scale = scaleFit;
				imgX = 0;
				imgY = 0;
			}
			if (enableLimiters) {
				if (scale < scaleFit) {
					scale = scaleFit;
					enableScaleToFit = true;
				}
				imgX = Math.max(imgX, 0);
				imgY = Math.max(imgY, 0);
				if (image.getWidth() - imgX < container.getWidth() / scale)
					imgX = Math.max(0, image.getWidth() - container.getWidth() / scale);
				if (image.getHeight() - imgY < container.getHeight() / scale)
					imgY = Math.max(0, image.getHeight() - container.getHeight() / scale);
			}
			imageView.setViewport(
					new Rectangle2D(imgX, imgY, container.getWidth() / scale, container.getHeight() / scale));
			// System.out.println(
			// container.getWidth() + " " + container.getHeight() + " / " + imgX
			// + " " + imgY + " " + scale);
		}
	}

	// shift the viewport of the imageView by the specified delta, clamping so
	// the viewport does not move off the actual image:
	private void shift(Point2D delta) {
		imgX -= delta.getX();
		imgY -= delta.getY();
		enableScaleToFit = false;
		update();
	}

	private void zoom(Point2D point, double newScale) {
		Point2D mouse = imageViewToImage(point);
		if (enableLimiters)
			newScale = Math.max(newScale, computeScaleToFit());
		// (mouse.x - imgX) * scale = (mouse.x - newImgX) * newScale;
		imgX = (imgX - mouse.getX()) * scale / newScale + mouse.getX();
		imgY = (imgY - mouse.getY()) * scale / newScale + mouse.getY();
		scale = newScale;
		enableScaleToFit = false;
		update();
	}

	private double computeScaleToFit() {
		if (image == null)
			return 0.0;
		else if (enableLimiters)
			return Math.max(container.getWidth() / image.getWidth(), container.getHeight() / image.getHeight());
		else
			return Math.min(container.getWidth() / image.getWidth(), container.getHeight() / image.getHeight());
	}

	// convert mouse coordinates in the imageView to coordinates in the actual
	// image:
	private Point2D imageViewToImage(Point2D imageViewCoordinates) {
		double x = imgX + imageViewCoordinates.getX() / scale;
		double y = imgY + imageViewCoordinates.getY() / scale;
		return new Point2D(x, y);
	}
}
