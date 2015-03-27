/*
 * Copyright (C) 2013 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.model;

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;

import rl.util.exceptions.ErrorHandler;
import rl.util.exceptions.PersistenceException;
import rl.util.persistence.PropertyManager;

/**
 * The <code>MapDataManager</code> maintains all the data which is needed for
 * map display and position marking. Besides a map image, at least two reference
 * points are needed to map image positions (in pixel) to latitude/longitude
 * values. The manager also maintains a lookup-table for previously used map
 * files which can be stored and loaded.
 * 
 * @author Ruediger Lunde
 * 
 */
public class MapDataManager implements MapData {

	private static final String MAP_PARAM_LOOKUP_FILE_NAME = "MapParamLookup.ser";

	private Image image;
	private MapParams params;
	private List<MapParams> mapParamLookup = new ArrayList<MapParams>();

	public void setMap(File mapFile) {
		image = null;
		params = null;
		if (mapFile != null) {
			params = lookupMapParams(mapFile);
			try {
				image = ImageIO.read(mapFile);
				if (params == null) {
					params = new MapParams();
					params.file = mapFile;
					mapParamLookup.add(params);
					Collections.sort(mapParamLookup);
				}
			} catch (IOException ex) {
				if (params != null) {
					clearCurrentMap();
				}
				Exception e = new PersistenceException(
						"Could not read map image from file " + mapFile + ".",
						ex);
				ErrorHandler.getInstance().handleError(e);
			}
			if (params != null && image == null) {
				// map file doesn't exist anymore, remove data.
				mapParamLookup.remove(params);
				params = null;
			}
		}
	}

	public void clearCurrentMap() {
		if (params != null) {
			mapParamLookup.remove(params);
			params = null;
			image = null;
		}
	}

	public File[] getAllMapFiles() {
		File[] result = new File[mapParamLookup.size()];
		for (int i = 0; i < mapParamLookup.size(); i++)
			result[i] = mapParamLookup.get(i).file;
		return result;
	}

	public boolean hasData() {
		return params != null && params.refPoints.size() >= 2;
	}

	public Image getImage() {
		return image;
	}

	public File getFile() {
		if (params != null)
			return params.file;
		else
			return null;
	}

	public void addRefPoint(GeoRefPoint refPoint) {
		if (params != null)
			params.refPoints.add(refPoint);
	}

	public void removeRefPoint(int index) {
		if (params != null)
			params.refPoints.remove(index);
	}

	public List<GeoRefPoint> getRefPoints() {
		if (params != null) {
			return Collections.unmodifiableList(params.refPoints);
		}
		return Collections.emptyList();
	}

	public double[] latLonToImagePos(final double lat, final double lon) {
		GeoRefPoint p1 = params.refPoints.get(0);
		GeoRefPoint p2 = params.refPoints.get(1);
		GeoRefPoint p3;
		if (params.refPoints.size() == 2)
			return convertWith2Samples(lat, lon, p1, p2);
		else if (params.refPoints.size() == 3) {
			p3 = params.refPoints.get(2);
			return convertWith3Samples(lat, lon, p1, p2, p3);
		} else { // find points closest to lat lon position
			List<GeoRefPoint> points = new ArrayList<GeoRefPoint>(
					params.refPoints);
			Collections.sort(points, new Comparator<GeoRefPoint>() {
				@Override
				public int compare(GeoRefPoint o1, GeoRefPoint o2) {
					double[] dist = new double[2];
					for (int i = 0; i < 2; i++) {
						GeoRefPoint o = (i == 0) ? o1 : o2;
						dist[i] = (o.lat - lat) * (o.lat - lat) + (o.lon - lon)
								* (o.lon - lon);
					}
					if (dist[0] < dist[1])
						return -1;
					else if (dist[0] > dist[1])
						return 1;
					else
						return 0;
				}
			});
			p1 = points.get(0);
			p2 = points.get(1);
			p3 = points.get(2);
			return convertWith3Samples(lat, lon, p1, p2, p3);
		}
	}

	public double[] convertWith2Samples(double lat, double lon, GeoRefPoint p1,
			GeoRefPoint p2) {
		double xImage = (lon - p1.lon) / (p2.lon - p1.lon)
				* (p2.xImage - p1.xImage) + p1.xImage;
		double yImage = (lat - p1.lat) / (p2.lat - p1.lat)
				* (p2.yImage - p1.yImage) + p1.yImage;
		return new double[] { xImage, yImage };
	}

	public double[] convertWith3Samples(double lat, double lon, GeoRefPoint p1,
			GeoRefPoint p2, GeoRefPoint p3) {
		double[] result = new double[2];
		for (int i = 0; i < 2; i++) {
			double[] v1 = new double[] { p1.lat, p1.lon,
					(i == 0) ? p1.xImage : p1.yImage };
			double[] v2 = new double[] { p2.lat, p2.lon,
					(i == 0) ? p2.xImage : p2.yImage };
			double[] v3 = new double[] { p3.lat, p3.lon,
					(i == 0) ? p3.xImage : p3.yImage };
			double[] a = new double[3];
			for (int j = 0; j < 3; j++)
				a[j] = v2[j] - v1[j];
			double[] b = new double[3];
			for (int j = 0; j < 3; j++)
				b[j] = v3[j] - v1[j];
			double[] n = new double[] { a[1] * b[2] - a[2] * b[1],
					a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0] };
			double d = 0;
			for (int j = 0; j < 3; j++)
				d += v1[j] * n[j];
			// n * x = d
			result[i] = (d - n[0] * lat - n[1] * lon) / n[2];
		}
		return result;
	}

	// public static void main(String[] args) {
	// MapDataManager m = new MapDataManager();
	// GeoRefPoint p1 = new GeoRefPoint(1, 0, 0, 0);
	// GeoRefPoint p2 = new GeoRefPoint(2, 0, 5, 0);
	// GeoRefPoint p3 = new GeoRefPoint(1, 0, 0, 5);
	// double[] result = m.convertWith3Samples(10, 0, p1, p2, p3);
	// System.out.println(result[0] + " " + result[1]);
	//
	// }

	public void saveMapParamLookup() {
		try {
			File file = PropertyManager.getInstance().getPropertyFile(
					MAP_PARAM_LOOKUP_FILE_NAME);
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(mapParamLookup);
			oos.close();
		} catch (Exception e) {
			ErrorHandler.getInstance().handleError(e);
		}
	}

	public void loadMapParamLookup() {
		File file = PropertyManager.getInstance().getPropertyFile(
				MAP_PARAM_LOOKUP_FILE_NAME);
		if (file.exists())
			try {
				FileInputStream fis = new FileInputStream(file);
				ObjectInputStream ois = new ObjectInputStream(fis);
				@SuppressWarnings("unchecked")
				List<MapParams> lookup = (List<MapParams>) ois.readObject();
				mapParamLookup = lookup;
				ois.close();
			} catch (Exception e) {
				ErrorHandler.getInstance().handleError(e);
			}
	}

	private MapParams lookupMapParams(File mapFile) {
		for (MapParams params : mapParamLookup)
			if (mapFile.equals(params.file))
				return params;
		return null;
	}

	public static class GeoRefPoint implements Serializable {
		private static final long serialVersionUID = 1L;
		private double xImage;
		private double yImage;
		private double lat;
		private double lon;

		public GeoRefPoint(double xImage, double yImage, double lat, double lon) {
			this.xImage = xImage;
			this.yImage = yImage;
			this.lat = lat;
			this.lon = lon;
		}

		public double getXImage() {
			return xImage;
		}

		public double getYImage() {
			return yImage;
		}

		public double getLat() {
			return lat;
		}

		public double getLon() {
			return lon;
		}
	}

	public static class MapParams implements Comparable<MapParams>,
			Serializable {
		private static final long serialVersionUID = 2L;
		File file; // never null!
		List<GeoRefPoint> refPoints = new ArrayList<GeoRefPoint>();

		@Override
		public int compareTo(MapParams params) {
			return file.getName().compareTo(params.file.getName());
		}

	}
}
