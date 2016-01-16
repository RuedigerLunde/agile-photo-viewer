/*
 * Copyright (C) 2016 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.fx.view;

import javafx.geometry.Point2D;

/**
 * Provides parameters and simple computations to scale and translate images for
 * viewing.
 * 
 * @author Ruediger Lunde
 */
public class ViewParams implements Cloneable {
	/* Image x coordinate shown in the top left corner of the image viewer. */
	private double imgX;
	/* Image y coordinate shown in the top left corner of the image viewer. */
	private double imgY;
	/*
	 * Value 1/2 means one viewer pixel represents two image pixels in each
	 * direction (-> 4 pixels).
	 */
	private double scale;

	public ViewParams(double imgX, double imgY, double scale) {
		super();
		this.imgX = imgX;
		this.imgY = imgY;
		this.scale = scale;
	}
	
	public ViewParams() {
	}

	public double getImgX() {
		return imgX;
	}

	public double getImgY() {
		return imgY;
	}

	public double getScale() {
		return scale;
	}
	
	public void setImgX(double imgX) {
		this.imgX = imgX;
	}

	public void setImgY(double imgY) {
		this.imgY = imgY;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}

	public void clampImgX(double min, double max) {
		if (imgX < min)
			imgX = min;
		else if (imgX > max)
			imgX = max;
	}
	
	public void clampImgY(double min, double max) {
		if (imgY < min)
			imgY = min;
		else if (imgY > max)
			imgY = max;
	}
	
	public void clampScale(double min, double max) {
		if (scale < min)
			scale = min;
		else if (scale > max)
			scale = max;
	}
	
	public Point2D imageToView(Point2D coordsImage) {
		double x = (coordsImage.getX() - imgX) * scale;
		double y = (coordsImage.getY() - imgY) * scale;
		return new Point2D(x, y);
	}

	public double imageToView(double distImage) {
		return distImage * scale;
	}

	public Point2D viewToImage(Point2D coordsView) {
		double x = imgX + coordsView.getX() / scale;
		double y = imgY + coordsView.getY() / scale;
		return new Point2D(x, y);
	}

	public double viewToImage(double distView) {
		return distView / scale;
	}

	@Override
	public ViewParams clone() {
		ViewParams result = null;
		try {
			result = (ViewParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace(); // should never happen...
		}
		return result;
	}

}
