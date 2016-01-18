/*
 * Copyright (C) 2013-2016 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.model;

import java.io.Serializable;

/**
 * Relates a point defined by image coordinates to a point defined by
 * geographical lat-lon coordinates. Given two or more of these reference
 * points, arbitrary geographical coordinates can be translated to image
 * coordinates.
 */
public class GeoRefPoint implements Serializable {
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