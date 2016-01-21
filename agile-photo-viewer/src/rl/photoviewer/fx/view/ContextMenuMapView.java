/*
 * Copyright (C) 2013-2016 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.fx.view;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import rl.photoviewer.model.GeoRefPoint;
import rl.photoviewer.model.MapData;
import rl.photoviewer.model.PVModel;
import rl.photoviewer.model.PhotoMetadata;

/**
 * This class provides a context menu for the Map View.
 * 
 * @author Ruediger Lunde
 *
 */
public class ContextMenuMapView {
	PVModel model;
	ImageViewController imageViewController;

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

	public ContextMenuMapView(ImageViewController imageViewController, PVModel model) {
		this.model = model;
		this.imageViewController = imageViewController;
		
		refPointItem = new MenuItem("Refpoint");
		refPointItem.setOnAction(e -> onRefPointAction(e));
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

	public void show(ContextMenuEvent event) {
		trigger = event;
		prepare();
		menu.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());
		event.consume();
	}
	
	private void prepare() {
		menu.hide();
		MapData mapData = model.getMapData();
		ViewParams viewParams = imageViewController.viewParamsProperty().get();
		Point2D posImg = viewParams.viewToImage(new Point2D(trigger.getX(), trigger.getY()));
		double radius = viewParams.viewToImage(MapDataViewController.SELECTION_RADIUS);
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

	private void onRefPointAction(ActionEvent event) {
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
