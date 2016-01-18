/*
 * Copyright (C) 2013-2016 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.model;

/**
 * Interface for ordered latitude longitude pairs.
 * 
 * @author Ruediger Lunde
 */
public interface IndexedGeoPoint {
	int getIndex();
	double getLat();
	double getLon();
}
