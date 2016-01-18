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
	public Shape createCurrPhotoMarker() {
		Circle result = new Circle();
		result.setRadius(20);
		result.setFill(Color.TRANSPARENT);
		result.setStroke(Color.RED);
		result.setStrokeWidth(5);
		result.setManaged(false);
		result.setEffect(new Lighting());
		return result;
	}

	public Shape createPhotoMarker() {
		Rectangle result = new Rectangle(-3, -3, 6, 6);
		result.setFill(Color.WHITE);
		result.setManaged(false);
		result.setEffect(new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(20, 20, 20), 5, 0, 2, 2));
		return result;
	}

	public Shape createRefPointMarker() {
		Circle result = new Circle();
		result.setRadius(10);
		result.setFill(Color.TRANSPARENT);
		result.setStroke(Color.GREEN);
		result.setStrokeWidth(5);
		result.setManaged(false);
		result.setEffect(new Lighting());
		return result;
	}
}
