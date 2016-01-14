package rl.photoviewer.fx.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class ImageViewController {

	private Pane container;

	private ImageView imageView;

	private Image image;

	private ObjectProperty<ViewParams> viewParams = new SimpleObjectProperty<ViewParams>();

	private boolean enableLimiters = false;
	private double maxScale = Double.MAX_VALUE;
	
	private boolean isScaleToFitActive = true;
	private boolean zoomingOut;

	public void initialize(ImageView imageView, Pane container) {
		this.imageView = imageView;
		this.container = container;
		viewParams.set(new ViewParams());
		container.setMinWidth(0);
		container.setMinHeight(0);

		imageView.setPreserveRatio(true);
		imageView.fitWidthProperty().bind(container.widthProperty());
		imageView.fitHeightProperty().bind(container.heightProperty());

		container.widthProperty().addListener(e -> update(viewParams.get()));
		container.heightProperty().addListener(e -> update(viewParams.get()));

		ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<>();

		imageView.setOnMousePressed(e -> {
			if (image != null) {
				Point2D mousePress = viewParams.get().viewToImage(new Point2D(e.getX(), e.getY()));
				mouseDown.set(mousePress);
			}
		});

		imageView.setOnMouseDragged(e -> {
			if (image != null) {
				Point2D dragPoint = viewParams.get().viewToImage(new Point2D(e.getX(), e.getY()));
				shift(dragPoint.subtract(mouseDown.get()));
				mouseDown.set(viewParams.get().viewToImage(new Point2D(e.getX(), e.getY())));
				e.consume();
			}
		});

		imageView.setOnScroll(e -> {
			if (image != null) {
				double newScale = viewParams.get().getScale() * Math.pow(1.01, e.getDeltaY() / 2);
				zoom(new Point2D(e.getX(), e.getY()), newScale);
			}
			e.consume();
		});

		imageView.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2 && image != null) {
				if (!isScaleToFitActive) {
					isScaleToFitActive = true;
					update(viewParams.get());
				} else {
					zoom(new Point2D(e.getX(), e.getY()), 1.0);
				}
				e.consume();
			}
		});
	}

	public void setImage(Image image) {
		this.image = image;
		imageView.setImage(image);
		isScaleToFitActive = true;
		update(viewParams.get());
	}

	public void setLimitersEnabled(boolean state) {
		enableLimiters = state;
		update(viewParams.get());
	}
	
	public void setMaxScale(double scale) {
		maxScale = scale;
		update(viewParams.get());
	}

	private void update(ViewParams nextParams) {
		ViewParams vp = nextParams;
		if (image != null && container.getWidth() > 0) {
			double scaleFit = computeScaleToFit();
			if (isScaleToFitActive) {
				vp.setScale(scaleFit);
//				vp.setImgX(0);
//				vp.setImgY(0);
			} else if (enableLimiters && vp.getScale() <= scaleFit) {
					vp.setScale(scaleFit);
					isScaleToFitActive = true;
			}
			if (enableLimiters || isScaleToFitActive) {
				if (image.getWidth() / image.getHeight() > container.getWidth() / container.getHeight()) {
					vp.clampImgX(0, image.getWidth() - vp.viewToImage(container.getWidth()));
					double tol = (container.getHeight() - image.getHeight() * scaleFit) / vp.getScale() / 2;
					vp.clampImgY(-tol, image.getHeight() - vp.viewToImage(container.getHeight()) + tol);
				} else {
					vp.clampImgY(0, image.getHeight() - vp.viewToImage(container.getHeight()));
					double tol = (container.getWidth() - image.getWidth() * scaleFit) / vp.getScale() / 2;
					vp.clampImgX(-tol, image.getWidth() - vp.viewToImage(container.getWidth()) + tol);
				}
			}
//					if (image.getWidth() - vp.getImgX() < vp.viewToImage(container.getWidth()))
//						vp.setImgX(Math.max(0, image.getWidth() - vp.viewToImage(container.getWidth())));
//				} else {
//				vp.setImgY(Math.max(vp.getImgY(), 0));
//				if (image.getHeight() - vp.getImgY() < vp.viewToImage(container.getHeight()))
//					vp.setImgY(Math.max(0, image.getHeight() - vp.viewToImage(container.getHeight())));
//				}
//				
//			if (isScaleToFitActive || zoomingOut) {
//				if (image.getWidth() < vp.viewToImage(container.getWidth())) {
//					vp.setImgX((image.getWidth() - vp.viewToImage(container.getWidth())) / 2 );
//				} else if (image.getHeight() < vp.viewToImage(container.getHeight())) {
//					vp.setImgY((image.getHeight() - vp.viewToImage(container.getHeight())) / 2 );
//				}
//			}
			viewParams.set(vp);
			imageView.setViewport(new Rectangle2D(vp.getImgX(), vp.getImgY(), vp.viewToImage(container.getWidth()),
					vp.viewToImage(container.getHeight())));
			zoomingOut = false;
		}
	}

	// shift the viewport of the imageView by the specified delta, clamping so
	// the viewport does not move off the actual image:
	private void shift(Point2D delta) {
		ViewParams vp = viewParams.get().clone();
		vp.setImgX(vp.getImgX() - delta.getX());
		vp.setImgY(vp.getImgY() - delta.getY());
		isScaleToFitActive = false;
		update(vp);
	}

	private void zoom(Point2D point, double newScale) {
		Point2D mouse = viewParams.get().viewToImage(point);
		ViewParams vp = viewParams.get().clone();
		if (enableLimiters)
			// already necessary here to prevent max zoomed images from moving
			newScale = Math.max(newScale, computeScaleToFit());
		newScale = Math.min(newScale, maxScale);
		// (mouse.x - imgX) * scale = (mouse.x - newImgX) * newScale;
		vp.setImgX((vp.getImgX() - mouse.getX()) * vp.getScale() / newScale + mouse.getX());
		vp.setImgY((vp.getImgY() - mouse.getY()) * vp.getScale() / newScale + mouse.getY());
		
		vp.setScale(newScale);
		isScaleToFitActive = false;
		zoomingOut = vp.getScale() < viewParams.get().getScale();
		update(vp);
	}

	private double computeScaleToFit() {
		if (image == null)
			return 0.0;
		// else if (enableLimiters)
		// return Math.max(container.getWidth() / image.getWidth(),
		// container.getHeight() / image.getHeight());
		// else
		return Math.min(container.getWidth() / image.getWidth(), container.getHeight() / image.getHeight());
	}
}
