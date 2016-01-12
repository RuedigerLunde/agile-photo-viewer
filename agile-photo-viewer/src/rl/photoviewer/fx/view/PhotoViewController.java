// This code is strongly inspired by the PlutoExplorer class of James D.
// https://gist.github.com/james-d/ce5ec1fd44ce6c64e81a
// https://gist.github.com/james-d

package rl.photoviewer.fx.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class PhotoViewController {

	private static final int MAX_ZOOM_PIXELS = 4;

	private ImageView imageView;

	private Image image;

	private boolean scaleToFit = true;

	public void initialize(ImageView imageView, Pane container) {
		this.imageView = imageView;
		container.setMinWidth(0);
		container.setMinHeight(0);

		imageView.setPreserveRatio(true);
		imageView.fitWidthProperty().bind(container.widthProperty());
		imageView.fitHeightProperty().bind(container.heightProperty());

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
			}
		});

		imageView.setOnScroll(e -> {
			if (image != null) {
				double scale = Math.pow(1.01, e.getDeltaY());
				zoom(new Point2D(e.getX(), e.getY()), 1/scale);
			}
		});

		imageView.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2 && image != null) {
				if (!scaleToFit)
					reset();
				else
					zoom(new Point2D(e.getX(), e.getY()),
							imageView.getBoundsInLocal().getWidth() / imageView.getViewport().getWidth());
			}
		});
	}

	public void setImage(Image image) {
		this.image = image;
		imageView.setImage(image);
		reset();
	}

	// reset to the top left:
	private void reset() {
		if (image != null) {
			imageView.setViewport(new Rectangle2D(0, 0, image.getWidth(), image.getHeight()));
			scaleToFit = true;
		}
	}

	// shift the viewport of the imageView by the specified delta, clamping so
	// the viewport does not move off the actual image:
	private void shift(Point2D delta) {
		Rectangle2D viewport = imageView.getViewport();

		double width = imageView.getImage().getWidth();
		double height = imageView.getImage().getHeight();

		double maxX = width - viewport.getWidth();
		double maxY = height - viewport.getHeight();

		double minX = clamp(viewport.getMinX() - delta.getX(), 0, maxX);
		double minY = clamp(viewport.getMinY() - delta.getY(), 0, maxY);

		imageView.setViewport(new Rectangle2D(minX, minY, viewport.getWidth(), viewport.getHeight()));
		scaleToFit = false;
	}

	private void zoom(Point2D point, double scale) {

		Rectangle2D viewport = imageView.getViewport();

		// don't scale so we see one image pixel in more than
		// MAX_ZOOM_PIXELS image view pixels in both directions (both
		// should be equal - RLu)
		double scaleMin = Math.min(imageView.getBoundsInLocal().getWidth() / viewport.getWidth(),
				imageView.getBoundsInLocal().getHeight() / viewport.getHeight()) / MAX_ZOOM_PIXELS;

		// don't scale so that we're bigger than image dimensions: (both
		// should be equal - RLu)
		double scaleMax = Math.max(image.getWidth() / viewport.getWidth(), image.getHeight() / viewport.getHeight());

		scale = clamp(scale, scaleMin, scaleMax);

		Point2D mouse = imageViewToImage(point);

		double newWidth = viewport.getWidth() * scale;
		double newHeight = viewport.getHeight() * scale;

		// To keep the visual point under the mouse from moving, we need
		// (x - newViewportMinX) / (x - currentViewportMinX) = scale
		// where x is the mouse X coordinate in the image

		// solving this for newViewportMinX gives

		// newViewportMinX = x - (x - currentViewportMinX) * scale

		// we then clamp this value so the image never scrolls out
		// of the imageview:

		double newMinX = clamp(mouse.getX() - (mouse.getX() - viewport.getMinX()) * scale, 0,
				image.getWidth() - newWidth);
		double newMinY = clamp(mouse.getY() - (mouse.getY() - viewport.getMinY()) * scale, 0,
				image.getHeight() - newHeight);

		imageView.setViewport(new Rectangle2D(newMinX, newMinY, newWidth, newHeight));
		scaleToFit = (scale == scaleMax);
	}

	private double clamp(double value, double min, double max) {

		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

	// convert mouse coordinates in the imageView to coordinates in the actual
	// image:
	private Point2D imageViewToImage(Point2D imageViewCoordinates) {
		double xProportion = imageViewCoordinates.getX() / imageView.getBoundsInLocal().getWidth();
		double yProportion = imageViewCoordinates.getY() / imageView.getBoundsInLocal().getHeight();

		Rectangle2D viewport = imageView.getViewport();
		return new Point2D(viewport.getMinX() + xProportion * viewport.getWidth(),
				viewport.getMinY() + yProportion * viewport.getHeight());
	}
}
