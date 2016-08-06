/*
 * Copyright (C) 2013-2016 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.fx.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 * Controller which is responsible for handling user events for an image pane.
 * It provides functions like zooming and panning.
 * 
 * @author Ruediger Lunde
 *
 */
public class ImageViewCtrl {

	private Pane container;

	private ImageView imageView;

	private Image image;

	private ObjectProperty<ViewParams> viewParams = new SimpleObjectProperty<ViewParams>();

	private Point2D lastMousePosition;
	/** Indicator for pan operations. */
	private boolean mouseDragged;
	/**
	 * If true, at least one dimension of the view (X or Y axis) remains fully
	 * covered by the image. The effect of pane and zoom operations is
	 * restricted accordingly.
	 */
	private boolean enableLimiters = false;
	/**
	 * The scale to be used when showing a new image. -1 denotes scale-to-fit.
	 */
	private double initScale = -1;
	/**
	 * Limits scale (if you don't want to see only one image pixel filling the
	 * whole view...).
	 */
	private double maxScale = Double.MAX_VALUE;

	private boolean isScaleToFitActive = true;
	/**
	 * If true, the next update with existing view (view screen size greater 0)
	 * will set scale to the specified initial scale and show the image centered
	 * in the view.
	 */
	private boolean isWaitingForInitScale;

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

		container.setOnMousePressed(e -> {
			if (image != null) {
				lastMousePosition = new Point2D(e.getX(), e.getY()); // (mousePress);
				mouseDragged = false;
			}
		});

		container.setOnMouseDragged(e -> {
			if (image != null) {
				Point2D mouse = new Point2D(e.getX(), e.getY());
				double deltaX = viewParams.get().viewToImage(e.getX() - lastMousePosition.getX());
				double deltaY = viewParams.get().viewToImage(e.getY() - lastMousePosition.getY());
				shift(new Point2D(deltaX, deltaY));
				lastMousePosition = mouse;
				mouseDragged = true;
				e.consume();
			}
		});

		container.setOnScroll(e -> {
			if (image != null) {
				double newScale = viewParams.get().getScale() * Math.pow(1.01, e.getDeltaY() / 2);
				zoom(viewParams.get().viewToImage(new Point2D(e.getX(), e.getY())), newScale);
			}
			e.consume();
		});

		container.setOnMouseClicked(e -> onMouseClicked(e));
	}

	public void onMouseClicked(MouseEvent event) {
		if (event.getButton() == MouseButton.MIDDLE && image != null) {
			if (!isScaleToFitActive) {
				isScaleToFitActive = true;
				update(viewParams.get());
			} else {
				zoom(viewParams.get().viewToImage(new Point2D(event.getX(), event.getY())), 1.0);
			}
			// e.consume();
		}
	}

	public Pane getContainer() {
		return container;
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
		imageView.setImage(image);
		isScaleToFitActive = true;
		if (initScale >= 0 && image != null)
			isWaitingForInitScale = true;
		update(viewParams.get());
	}

	public ObjectProperty<ViewParams> viewParamsProperty() {
		return viewParams;
	}

	/** Indicates whether the last click was a pan operation. */
	public boolean isMouseDragged() {
		return mouseDragged;
	}

	public void setLimitersEnabled(boolean state) {
		enableLimiters = state;
		update(viewParams.get());
	}

	public void setInitScale(double scale) {
		initScale = scale;
		update(viewParams.get());
	}

	public void setMaxScale(double scale) {
		maxScale = scale;
		update(viewParams.get());
	}

	private void update(ViewParams nextParams) {
		if (nextParams == viewParams.get())
			nextParams = nextParams.clone();
		if (image != null && container.getWidth() > 0) {
			ViewParams vp = nextParams;
			double scaleFit = computeScaleToFit();
			if (isScaleToFitActive) {
				vp.setScale(scaleFit);
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
			viewParams.set(vp);
			imageView.setViewport(new Rectangle2D(vp.getImgX(), vp.getImgY(), vp.viewToImage(container.getWidth()),
					vp.viewToImage(container.getHeight())));
			if (isWaitingForInitScale) {
				isWaitingForInitScale = false;
				zoom(new Point2D(image.getWidth() / 2, image.getHeight() / 2), initScale);
			}
		}
	}

	// shift the viewport of the imageView by the specified delta (in image
	// coordinates).
	public void shift(Point2D delta) {
		ViewParams vp = viewParams.get().clone();
		vp.setImgX(vp.getImgX() - delta.getX());
		vp.setImgY(vp.getImgY() - delta.getY());
		isScaleToFitActive = false;
		update(vp);
	}

	/**
	 * Performs zooming. Image information at refPoint does not move on the
	 * screen.
	 * 
	 * @param refPoint
	 *            Mouse position in image coordinates
	 * @param newScale
	 *            Scale to be used next (might be modified during update to fit
	 *            to layout constraints)
	 */
	public void zoom(Point2D refPoint, double newScale) {
		ViewParams vp = viewParams.get().clone();
		if (enableLimiters)
			// already necessary here to prevent max zoomed images from moving
			newScale = Math.max(newScale, computeScaleToFit());
		newScale = Math.min(newScale, maxScale);
		// (refPoint.x - imgX) * scale = (refPoint.x - newImgX) * newScale;
		vp.setImgX((vp.getImgX() - refPoint.getX()) * vp.getScale() / newScale + refPoint.getX());
		vp.setImgY((vp.getImgY() - refPoint.getY()) * vp.getScale() / newScale + refPoint.getY());

		vp.setScale(newScale);
		isScaleToFitActive = false;
		update(vp);
	}

	private double computeScaleToFit() {
		return Math.min(container.getWidth() / image.getWidth(), container.getHeight() / image.getHeight());
	}
}
