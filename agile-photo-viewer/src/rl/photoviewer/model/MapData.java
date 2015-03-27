/*
 * Copyright (C) 2013 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.model;

import java.awt.Image;
import java.io.File;
import java.util.List;

import rl.photoviewer.model.MapDataManager.GeoRefPoint;

/**
 * Abstraction of the <code>MapDataManager</code> which focuses on read access
 * methods. View components are allowed to directly communicate with the manager
 * by this interface.
 * 
 * @author Ruediger Lunde
 * 
 */
public interface MapData {

	public boolean hasData();

	public Image getImage();

	public File getFile();

	public void addRefPoint(GeoRefPoint refPoint); // remove from this
													// interface?

	public void removeRefPoint(int index); // remove from this interface?

	public List<GeoRefPoint> getRefPoints();

	public double[] latLonToImagePos(double lat, double lon);

	public File[] getAllMapFiles();
}
