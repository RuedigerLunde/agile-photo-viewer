package rl.photoviewer.model;

/**
 * Interface for latitude longitude pairs.
 * 
 * @author Ruediger Lunde
 */
public interface IndexedGeoPoint {
	int getIndex();
	double getLat();
	double getLon();
}
