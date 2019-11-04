/*
 * Copyright (C) 2013-2016 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.model;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Abstraction of the <code>MapDataManager</code> which focuses on read access
 * methods. View components are allowed to directly communicate with the manager
 * by this interface.
 * 
 * @author Ruediger Lunde
 * 
 */
public interface MapData {

	boolean hasData();

	File getFile();

	List<GeoRefPoint> getRefPoints();

	GeoRefPoint findRefPointAt(double xImg, double yImg, double radius);

	IndexedGeoPoint findPhotoPositionAt(Set<? extends IndexedGeoPoint> photoPositions, double xImg, double yImg,
			double radius, double tolerance);

	double[] latLonToImagePos(double lat, double lon);

	File[] getAllMapFiles();
}
