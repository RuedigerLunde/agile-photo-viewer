/*
 * Copyright (C) 2013-2016 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.fx.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;
import rl.photoviewer.model.GeoRefPoint;
import rl.photoviewer.model.IndexedGeoPoint;
import rl.photoviewer.model.MapData;
import rl.photoviewer.model.PVModel;
import rl.photoviewer.model.PhotoMetadata;

/**
 * Controller which is responsible for handling user events for a map image pane.
 * It strongly cooperates with an {@link ImageViewCtrl} and adds functionality
 * for showing and editing markers representing geo-located images within the map image.
 * 
 * @author Ruediger Lunde
 *
 */
public class MapDataViewCtrl {
	private final static double TOLERANCE = 0.4;
	private ImageViewCtrl imageViewController;
	private PVModel model;

	private Shape currPhotoMarker;
	private List<Shape> refPointMarkers = new ArrayList<>();
	private List<Shape> photoMarkers = new ArrayList<>();
	private MarkerFactory markerFactory = new MarkerFactory();

	public void initialize(ImageViewCtrl viewController, PVModel model) {
		this.imageViewController = viewController;
		this.model = model;

		viewController.getContainer().setOnMouseClicked(this::onMouseClicked);
	}
	
	public ImageViewCtrl getImageViewController() {
		return imageViewController;
	}
	
	public void setMarkerFactory(MarkerFactory factory) {
		markerFactory = factory;
	}

	public double getMaxMarkerSize() {
		return markerFactory.getMaxMarkerSize();
	}
	
	
	/**
	 * Changes size of all markers and also the radius for photo selection (half of the size).
	 * @param size Default is 40
	 */
	public void setMaxMarkerSize(double size) {
		markerFactory.setMaxMarkerSize(size);
		
		Pane container = imageViewController.getContainer();
		while (!photoMarkers.isEmpty())
			container.getChildren().remove(photoMarkers.remove(photoMarkers.size() - 1));
		while (!refPointMarkers.isEmpty())
			container.getChildren().remove(refPointMarkers.remove(refPointMarkers.size() - 1));
		container.getChildren().remove(currPhotoMarker);
		currPhotoMarker = null;
		
		update(null);
	}
	
	public void update(Object arg) {
        if (arg == PVModel.CURR_PHOTO_PROP)
            adjustMapPosition();

		Pane container = imageViewController.getContainer();
		ViewParams viewParams = imageViewController.viewParamsProperty().get();
		MapData mapData = model.getMapData();

		int photoCount = mapData.hasData() ? model.getVisiblePhotoCount() : 0;
		while (photoCount < photoMarkers.size())
			container.getChildren().remove(photoMarkers.remove(photoMarkers.size() - 1));

		while (mapData.getRefPoints().size() < refPointMarkers.size())
			container.getChildren().remove(refPointMarkers.remove(refPointMarkers.size() - 1));
		while (mapData.getRefPoints().size() > refPointMarkers.size()) {
			Shape refMarker = markerFactory.createRefPointMarker();
			refPointMarkers.add(refMarker);
			container.getChildren().add(refMarker);
		}

		int j = 0;
		for (GeoRefPoint refPoint : mapData.getRefPoints()) {
			Point2D posRefMarker = viewParams.imageToView(new Point2D(refPoint.getXImage(), refPoint.getYImage()));
			Shape refMarker = refPointMarkers.get(j++);
			refMarker.setLayoutX(posRefMarker.getX());
			refMarker.setLayoutY(posRefMarker.getY());
		}

		if (photoCount > 0) {
			while (photoCount > photoMarkers.size()) {
				Shape photoMarker = markerFactory.createPhotoMarker();
				photoMarkers.add(photoMarker);
				container.getChildren().add(photoMarker);
			}

			int i = 0;
			for (IndexedGeoPoint geoPoint : model.getVisiblePhotoPositions()) {
				double[] posPhoto = mapData.latLonToImagePos(geoPoint.getLat(), geoPoint.getLon());
				Point2D posPhotoMarker = viewParams.imageToView(new Point2D(posPhoto[0], posPhoto[1]));
				Shape photoMarker = photoMarkers.get(i++);
				photoMarker.setLayoutX(posPhotoMarker.getX());
				photoMarker.setLayoutY(posPhotoMarker.getY());
			}
		}
		
		PhotoMetadata currData = model.getSelectedPhotoData();
		if (mapData.hasData() && currData != null && !Double.isNaN(currData.getLat())) {
			if (currPhotoMarker == null) {
				currPhotoMarker = markerFactory.createCurrPhotoMarker();
				container.getChildren().add(currPhotoMarker);
			}
			double[] posCurrPhoto = mapData.latLonToImagePos(currData.getLat(), currData.getLon());
			Point2D posCurrPhotoMarker = viewParams.imageToView(new Point2D(posCurrPhoto[0], posCurrPhoto[1]));
			currPhotoMarker.setLayoutX(posCurrPhotoMarker.getX());
			currPhotoMarker.setLayoutY(posCurrPhotoMarker.getY());
			if (arg == PVModel.CURR_PHOTO_PROP) {
				double dist = getMaxMarkerSize() * 1.5;
				double deltaX = posCurrPhotoMarker.getX() >= dist ? 0 : dist - posCurrPhotoMarker.getX();
				if (posCurrPhotoMarker.getX() > container.getWidth() - dist)
					deltaX = container.getWidth() - dist - posCurrPhotoMarker.getX();
				double deltaY = posCurrPhotoMarker.getY() >= dist ? 0 : dist - posCurrPhotoMarker.getY();
				if (posCurrPhotoMarker.getY() > container.getHeight() - dist)
					deltaY = container.getHeight() - dist - posCurrPhotoMarker.getY();
				if (deltaX != 0 || deltaY != 0)
					imageViewController
							.shift(new Point2D(viewParams.viewToImage(deltaX), viewParams.viewToImage(deltaY)));
			}
		} else if (currPhotoMarker != null) {
			container.getChildren().remove(currPhotoMarker);
			currPhotoMarker = null;
		}
	}

    /**
     * Shifts the map so that it covers as much as possible of the view area.
     */
	private void adjustMapPosition() {
        Pane container = imageViewController.getContainer();
        Image image = imageViewController.getImage();
        ViewParams viewParams = imageViewController.viewParamsProperty().get();
        if (image != null) {
            double deltaX = 0;
            double deltaY = 0;
            if (viewParams.getImgX() > 0 &&
                    image.getWidth() - viewParams.getImgX() < container.getWidth() / viewParams.getScale())
                deltaX = viewParams.getImgX() + container.getWidth() / viewParams.getScale() - image.getWidth();
            if (viewParams.getImgX() < deltaX)
                deltaX = viewParams.getImgX() - deltaX;
            if (viewParams.getImgY() > 0 &&
                    image.getHeight() - viewParams.getImgY() < container.getHeight() / viewParams.getScale())
                deltaY =   viewParams.getImgY() + container.getHeight() / viewParams.getScale() - image.getHeight();
            if (viewParams.getImgY() < deltaY)
                deltaY = viewParams.getImgY() - deltaY;
            if (deltaX != 0 || deltaY != 0)
                imageViewController.shift(new Point2D(deltaX, deltaY));
        }
    }

	
	public void onMouseClicked(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && model.getMapData().hasData()
				&& !imageViewController.isMouseDragged()) {
			// select photo next to mouse position
			ViewParams vp = imageViewController.viewParamsProperty().get();
			Point2D posImg = vp.viewToImage(new Point2D(event.getX(), event.getY()));
			double radius = vp.viewToImage(getMaxMarkerSize() / 2);
			double tolerance = radius * TOLERANCE;
			Set<? extends IndexedGeoPoint> geoPoints = model.getVisiblePhotoPositions();
			IndexedGeoPoint pt = model.getMapData().findPhotoPositionAt(geoPoints, posImg.getX(), posImg.getY(),
					radius, tolerance);
			if (pt != null)
				model.selectPhotoByMetadata(pt);
			// e.consume();
		} else {
			imageViewController.onMouseClicked(event);
		}
	}	
}
