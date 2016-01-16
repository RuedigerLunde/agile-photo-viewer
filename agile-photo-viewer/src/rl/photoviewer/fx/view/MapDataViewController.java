package rl.photoviewer.fx.view;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Lighting;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import rl.photoviewer.model.IndexedGeoPoint;
import rl.photoviewer.model.MapData;
import rl.photoviewer.model.MapDataManager.GeoRefPoint;
import rl.photoviewer.model.PVModel;
import rl.photoviewer.model.PhotoMetadata;

public class MapDataViewController {
	private ImageViewController viewController;
	private PVModel model;

	private Circle currPhotoMarker;
	private List<Shape> refPointMarkers = new ArrayList<Shape>();
	private List<Shape> photoMarkers = new ArrayList<Shape>();

	public void initialize(ImageViewController viewController, PVModel model) {
		this.viewController = viewController;
		this.model = model;
	}

	public void update(Object arg) {
		Pane container = viewController.getContainer();
		ViewParams viewParams = viewController.viewParamsProperty().get();
		MapData mapData = model.getMapData();
		
		while (model.getVisiblePhotoCount() < photoMarkers.size())
			container.getChildren().remove(photoMarkers.remove(photoMarkers.size() - 1));
		while (model.getVisiblePhotoCount() > photoMarkers.size()) {
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
		
		while (mapData.getRefPoints().size() < refPointMarkers.size())
			container.getChildren().remove(refPointMarkers.remove(refPointMarkers.size() - 1));
		while (mapData.getRefPoints().size() > refPointMarkers.size()) {
			Shape refMarker = createRefPointMarker();
			refPointMarkers.add(refMarker);
			container.getChildren().add(refMarker);
		}

		int j = 0;
		for (GeoRefPoint refPoint : mapData.getRefPoints()) {
			//double[] pos = mapData.latLonToImagePos(grp.getLat(), grp.getLon());
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
					viewController.shift(new Point2D(viewParams.viewToImage(deltaX), viewParams.viewToImage(deltaY)));
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
		//result.setStrokeWidth(1)
		result.setManaged(false);
		result.setEffect(new DropShadow(6, 3, 3, Color.BLACK));//new Lighting());
		return result;
	}
	
	protected Shape createRefPointMarker() {
		Circle result = new Circle();
		result.setRadius(10);
		result.setFill(Color.TRANSPARENT);
		result.setStroke(Color.YELLOW);
		result.setStrokeWidth(5);
		result.setManaged(false);
		result.setEffect(new Lighting());
		return result;
	}
}
