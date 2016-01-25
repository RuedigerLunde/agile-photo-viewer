/*
 * Copyright (C) 2013-2016 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.fx.view;

import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Lighting;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 * Factory which creates Markers for the map pane.
 * 
 * @author Ruediger Lunde
 *
 */
public class MarkerFactory {
	private double maxMarkerSize = 40;

	public void setMaxMarkerSize(double size) {
		maxMarkerSize = size;
	}
	
	public double getMaxMarkerSize() {
		return maxMarkerSize;
	}

	public Shape createCurrPhotoMarker() {
		Circle result = new Circle();
		double radius = maxMarkerSize / 2;
		result.setRadius(radius);
		result.setFill(Color.TRANSPARENT);
		result.setStroke(Color.RED);
		result.setStrokeWidth(radius / 4);
		result.setManaged(false);
		result.setEffect(new Lighting());
		return result;
	}

	public Shape createPhotoMarker() {
		double size = maxMarkerSize / 6;
		Rectangle result = new Rectangle(-size / 2, -size / 2, size, size);
		result.setFill(Color.WHITE);
		result.setManaged(false);
		result.setEffect(new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(20,
				20, 20), size * 0.9, 0, size / 3, size / 3));
		return result;
	}

	public Shape createRefPointMarker() {
		Circle result = new Circle();
		double radius = maxMarkerSize / 4;
		result.setRadius(radius);
		result.setFill(Color.TRANSPARENT);
		result.setStroke(Color.GREEN);
		result.setStrokeWidth(radius / 2);
		result.setManaged(false);
		result.setEffect(new Lighting());
		return result;
	}
}
