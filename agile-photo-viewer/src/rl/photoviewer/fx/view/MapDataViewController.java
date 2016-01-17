package rl.photoviewer.fx.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Lighting;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import rl.photoviewer.model.GeoRefPoint;
import rl.photoviewer.model.IndexedGeoPoint;
import rl.photoviewer.model.MapData;
import rl.photoviewer.model.PVModel;
import rl.photoviewer.model.PhotoMetadata;

public class MapDataViewController {
	private ImageViewController imageViewController;
	private PVModel model;

	private Circle currPhotoMarker;
	private List<Shape> refPointMarkers = new ArrayList<Shape>();
	private List<Shape> photoMarkers = new ArrayList<Shape>();
	private MapContextMenu mapMenu = new MapContextMenu();

	public void initialize(ImageViewController viewController, PVModel model) {
		this.imageViewController = viewController;
		this.model = model;

		viewController.getContainer().setOnContextMenuRequested(e -> {
			mapMenu.prepare(e);
			mapMenu.show();
		});

		viewController.getContainer().setOnMouseClicked(e -> {
			if (e.getClickCount() == 1 && model.getMapData().hasData()) {
				ViewParams viewParams = imageViewController.viewParamsProperty().get();
				Point2D posImg = viewParams.viewToImage(new Point2D(e.getX(), e.getY()));
				double radius = viewParams.viewToImage(20);
				double tolerance = viewParams.viewToImage(5);
				Set<? extends IndexedGeoPoint> geoPoints = model.getVisiblePhotoPositions();
				IndexedGeoPoint pt = model.getMapData().findPhotoPositionAt(geoPoints, posImg.getX(), posImg.getY(),
						radius, tolerance);
				if (pt != null)
					model.selectPhotoByMetadata(pt);
				e.consume();
			}
		});
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
				Shape photoMarker = createPhotoMarker();
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
			Shape refMarker = createRefPointMarker();
			refPointMarkers.add(refMarker);
			container.getChildren().add(refMarker);
		}

		int j = 0;
		for (GeoRefPoint refPoint : mapData.getRefPoints()) {
			// double[] pos = mapData.latLonToImagePos(grp.getLat(),
			// grp.getLon());
			Point2D posRefMarker = viewParams.imageToView(new Point2D(refPoint.getXImage(), refPoint.getYImage()));
			Shape refMarker = refPointMarkers.get(j++);
			refMarker.setLayoutX(posRefMarker.getX());
			refMarker.setLayoutY(posRefMarker.getY());
		}

		PhotoMetadata currData = model.getSelectedPhotoData();
		if (mapData.hasData() && currData != null && !Double.isNaN(currData.getLat())) {
			if (currPhotoMarker == null) {
				currPhotoMarker = createCurrPhotoMarker();
				container.getChildren().add(currPhotoMarker);
			}
			double[] posCurrPhoto = mapData.latLonToImagePos(currData.getLat(), currData.getLon());
			Point2D posCurrPhotoMarker = viewParams.imageToView(new Point2D(posCurrPhoto[0], posCurrPhoto[1]));
			currPhotoMarker.setLayoutX(posCurrPhotoMarker.getX());
			currPhotoMarker.setLayoutY(posCurrPhotoMarker.getY());
			if (arg == PVModel.SELECTED_PHOTO_CHANGED) {
				double dist = currPhotoMarker.getRadius() * 2;
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

	protected Circle createCurrPhotoMarker() {
		Circle result = new Circle();
		result.setRadius(20);
		result.setFill(Color.TRANSPARENT);
		result.setStroke(Color.RED);
		result.setStrokeWidth(5);
		result.setManaged(false);
		result.setEffect(new Lighting());
		return result;
	}

	protected Shape createPhotoMarker() {
		Rectangle result = new Rectangle(-3, -3, 6, 6);
		result.setFill(Color.WHITE);
		result.setManaged(false);
		result.setEffect(new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(20, 20, 20), 5, 0, 2, 2));
		return result;
	}

	protected Shape createRefPointMarker() {
		Circle result = new Circle();
		result.setRadius(10);
		result.setFill(Color.TRANSPARENT);
		result.setStroke(Color.GREEN);
		result.setStrokeWidth(5);
		result.setManaged(false);
		result.setEffect(new Lighting());
		return result;
	}

	private class MapContextMenu {
		ContextMenu menu;
		ContextMenuEvent trigger;
		GeoRefPoint refPoint;
		PhotoMetadata photoData;
		MenuItem refPointItem;
		MenuItem clearMapItem;

		MapContextMenu() {
			menu = new ContextMenu();
			refPointItem = new MenuItem("Refpoint");
			menu.getItems().add(refPointItem);
			refPointItem.setOnAction(e -> onMenuItemAction(e));
			clearMapItem = new MenuItem("Clear Map");
			menu.getItems().add(clearMapItem);
			clearMapItem.setOnAction(e -> {
				model.clearCurrentMap();
			});
		}

		public void prepare(ContextMenuEvent trigger) {
			menu.hide();
			this.trigger = trigger;
			ViewParams viewParams = imageViewController.viewParamsProperty().get();
			Point2D posImg = viewParams.viewToImage(new Point2D(trigger.getX(), trigger.getY()));
			double radius = viewParams.viewToImage(20);
			refPoint = model.getMapData().findRefPointAt(posImg.getX(), posImg.getY(), radius);
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
			refPointItem.setDisable(imageViewController.getImage() == null || refPoint == null && photoData == null);
			clearMapItem.setDisable(imageViewController.getImage() == null);
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
