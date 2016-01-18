/*
 * Copyright (C) 2013-2016 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.fx.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
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
 * It strongly cooperates with an <code>ImageViewController</code> and adds functionality
 * for showing and editing markers representing geo-located images within the map image.
 * 
 * @author Ruediger Lunde
 *
 */
public class MapDataViewController {
	private final static int SELECTION_RADIUS = 20;
	private final static int TOLERANCE = 8;
	private ImageViewController imageViewController;
	private PVModel model;

	private Shape currPhotoMarker;
	private List<Shape> refPointMarkers = new ArrayList<Shape>();
	private List<Shape> photoMarkers = new ArrayList<Shape>();
	private MapContextMenu mapMenu = new MapContextMenu();
	private MarkerFactory markerFactory = new MarkerFactory();

	public void initialize(ImageViewController viewController, PVModel model) {
		this.imageViewController = viewController;
		this.model = model;

		viewController.getContainer().setOnContextMenuRequested(e -> {
			mapMenu.prepare(e);
			mapMenu.show();
		});

		viewController.getContainer().setOnMouseClicked(e -> onMouseClicked(e));
	}
	
	public void setMarkerFactory(MarkerFactory factory) {
		markerFactory = factory;
	}

	public void update(Object arg) {
		Pane container = imageViewController.getContainer();
		ViewParams viewParams = imageViewController.viewParamsProperty().get();
		MapData mapData = model.getMapData();

		int photoCount = mapData.hasData() ? model.getVisiblePhotoCount() : 0;
		while (photoCount < photoMarkers.size())
			container.getChildren().remove(photoMarkers.remove(photoMarkers.size() - 1));

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
			if (arg == PVModel.SELECTED_PHOTO_CHANGED) {
				double dist = SELECTION_RADIUS * 2;
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
	
	public void onMouseClicked(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && model.getMapData().hasData()
				&& !imageViewController.isMouseDragged()) {
			ViewParams vp = imageViewController.viewParamsProperty().get();
			Point2D posImg = vp.viewToImage(new Point2D(event.getX(), event.getY()));
			double radius = vp.viewToImage(SELECTION_RADIUS);
			double tolerance = vp.viewToImage(TOLERANCE);
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

	private class MapContextMenu {
		ContextMenu menu;
		ContextMenuEvent trigger;
		GeoRefPoint refPoint;
		PhotoMetadata photoData;
		File map1;
		File map2;
		MenuItem refPointItem;
		MenuItem openMap1Item;
		MenuItem openMap2Item;
		MenuItem closeMapItem;

		MapContextMenu() {
			refPointItem = new MenuItem("Refpoint");
			refPointItem.setOnAction(e -> onMenuItemAction(e));
			openMap1Item = new MenuItem("Open");
			openMap1Item.setOnAction(e -> model.setMap(map1));
			openMap2Item = new MenuItem("Open");
			openMap2Item.setOnAction(e -> model.setMap(map2));
			closeMapItem = new MenuItem("Close Map");
			closeMapItem.setOnAction(e -> {
				model.setMap(null);
			});
			
			menu = new ContextMenu();
			menu.getItems().addAll(refPointItem, openMap1Item, openMap2Item, closeMapItem);
		}

		public void prepare(ContextMenuEvent trigger) {
			menu.hide();
			this.trigger = trigger;
			MapData mapData = model.getMapData();
			ViewParams viewParams = imageViewController.viewParamsProperty().get();
			Point2D posImg = viewParams.viewToImage(new Point2D(trigger.getX(), trigger.getY()));
			double radius = viewParams.viewToImage(SELECTION_RADIUS);
			refPoint = mapData.findRefPointAt(posImg.getX(), posImg.getY(), radius);
			photoData = model.getSelectedPhotoData();
			if (photoData != null && Double.isNaN(photoData.getLat()))
				photoData = null;
			if (refPoint != null) {
				refPointItem.setText("Remove Reference Point");
				refPointItem.setId("RemoveRefPointItem");
			} else {
				refPointItem.setText("Add Reference Point");
				refPointItem.setId("AddRefPointItem");
			}
			
			File[] mapFiles = model.getMapData().getAllMapFiles();
			int idx = mapData.getFile() == null ? -1 : 0;
			while (++idx < mapFiles.length && !mapFiles[idx].exists());
			map1 = (idx < mapFiles.length) ? mapFiles[idx] : null;
			while (++idx < mapFiles.length && !mapFiles[idx].exists());
			map2 = (idx < mapFiles.length) ? mapFiles[idx] : null;
			openMap1Item.setText("Open " + (map1 != null ? map1.getName() : ""));
			openMap2Item.setText("Open " + (map2 != null ? map2.getName() : ""));
			
			refPointItem.setDisable(imageViewController.getImage() == null || refPoint == null && photoData == null);
			openMap1Item.setDisable(map1 == null);
			openMap2Item.setDisable(map2 == null);
			closeMapItem.setDisable(imageViewController.getImage() == null);
		}

		public void show() {
			menu.show((Node) trigger.getSource(), trigger.getScreenX(), trigger.getScreenY());
			trigger.consume();
		}

		public void onMenuItemAction(ActionEvent event) {
			MenuItem source = (MenuItem) event.getSource();
			if (source.getId().equals("RemoveRefPointItem")) {
				model.removeMapRefPoint(refPoint);
			} else if (source.getId().equals("AddRefPointItem")) {
				ViewParams vp = imageViewController.viewParamsProperty().get();
				Point2D mouseImg = vp.viewToImage(new Point2D(trigger.getX(), trigger.getY()));
				model.addMapRefPoint(
						new GeoRefPoint(mouseImg.getX(), mouseImg.getY(), photoData.getLat(), photoData.getLon()));
			}
		}
	}
}
