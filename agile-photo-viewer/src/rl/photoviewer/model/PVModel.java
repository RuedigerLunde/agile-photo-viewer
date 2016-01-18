/*
 * Copyright (C) 2013-2016 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.model;

import java.io.File;
import java.util.List;
import java.util.Observable;
import java.util.Set;

/**
 * Facade which provides access to the complete state of the photo viewer. The
 * state is defined by a selected photo, a map image with reference points, and
 * an ordered collection of metadata for all photos in the directory of the
 * selected photo.
 * 
 * @author Ruediger Lunde
 * 
 */
public class PVModel extends Observable {

	public final static String METADATA_CHANGED = "MetadataChanged";
	public final static String SELECTED_PHOTO_CHANGED = "SelectedPhotoChanged";
	public final static String SELECTED_MAP_CHANGED = "SelectedMapChanged";
	public final static String MAP_DATA_CHANGED = "MapDataChanged";
	
	private ExifDataManager exifDataManager;
	private MapDataManager mapDataManager;

	public PVModel() {
		exifDataManager = new ExifDataManager();
		mapDataManager = new MapDataManager();
	}

	/**
	 * Updates the current directory and corresponding metadata if necessary and
	 * selects the photo to be shown in the viewer.
	 * 
	 * @param file
	 *            File in photo format or directory with photos.
	 * @return Value true if current directory has changed.
	 */
	public boolean selectPhoto(File file) {
		boolean result = false;
		File dir = file;
		String fileName = null;
		if (!dir.isDirectory()) {
			dir = file.getParentFile();
			fileName = file.getName();
		}
		if (getCurrDirectory() != null && !dir.equals(getCurrDirectory())) {
			setMap(null);
			result = true;
		}
		exifDataManager.setCurrDirectory(dir);
		if (fileName != null)
			exifDataManager.selectPhoto(fileName);
		else if (exifDataManager.getVisiblePhotoCount() > 0)
			exifDataManager.selectFirstPhoto();
		setChanged();
		notifyObservers(METADATA_CHANGED);
		setChanged();
		notifyObservers(SELECTED_PHOTO_CHANGED);
		return result;
	}

	public void setVisibility(int minRating, KeywordExpression expression) {
		exifDataManager.setVisibility(minRating, expression);
		setChanged();
		notifyObservers();
	}

	public int getRatingFilter() {
		return exifDataManager.getRatingFilter();
	}
	
	public KeywordExpression getVisibilityExpression() {
		return exifDataManager.getVisibilityExpression();
	}

	/** Selects the first photo in the current directory. */
	public void selectFirstPhoto() {
		exifDataManager.selectFirstPhoto();
		setChanged();
		notifyObservers(SELECTED_PHOTO_CHANGED);
	}

	public void selectPhotoByMetadata(IndexedGeoPoint pos) {
		if (pos instanceof PhotoMetadata) {
			PhotoMetadata data = (PhotoMetadata) pos;
			exifDataManager.selectPhoto(data.getFileName());
			setChanged();
			notifyObservers(SELECTED_PHOTO_CHANGED);
		}
	}

	/**
	 * Searches for the previous photo in the current directory and selects it.
	 */
	public void selectPrevPhoto() {
		exifDataManager.selectPreviousPhoto();
		setChanged();
		notifyObservers(SELECTED_PHOTO_CHANGED);
	}

	/**
	 * Searches for the next photo in the current directory and selects it.
	 */
	public void selectNextPhoto() {
		exifDataManager.selectNextPhoto();
		setChanged();
		notifyObservers(SELECTED_PHOTO_CHANGED);
	}

	/** Changes the order of the photos. Options: Order by name or by date. */
	public void setSortByDate(boolean state) {
		exifDataManager.setSortByDate(state);
	}

	/**
	 * Returns the files of all photos in the current directory which fulfill
	 * the visibility expression.
	 */
	public List<File> getVisiblePhotos() {
		return exifDataManager.getVisiblePhotos();
	}

	public Set<? extends IndexedGeoPoint> getVisiblePhotoPositions() {
		return exifDataManager.getVisiblePhotoPositions();
	}

	public File getSelectedPhoto() {
		PhotoMetadata data = getSelectedPhotoData();
		if (data != null)
			return new File(getCurrDirectory(), data.getFileName());
		else
			return null;
	}

	public PhotoMetadata getSelectedPhotoData() {
		return exifDataManager.getSelectedPhotoData();
	}

	public void deleteSelectedPhoto() {
		exifDataManager.deleteSelectedPhoto();
		setChanged();
		notifyObservers(SELECTED_PHOTO_CHANGED);
	}

	public File getCurrDirectory() {
		return exifDataManager.getCurrDirectory();
	}

	/**
	 * Returns all keywords which were found in EXIF tags of photos in the
	 * current directory.
	 */
	public List<String> getAllKeywords() {
		return exifDataManager.getAllKeywords();
	}

	/**
	 * Returns a list which contains the number of occurrences for each keyword.
	 */
	public List<Integer> getKeywordCounts() {
		return exifDataManager.getKeywordCounts();
	}

	/**
	 * Returns the number of files with photo format in the current directory
	 * which evaluate the visibility expression to true.
	 */
	public int getVisiblePhotoCount() {
		return exifDataManager.getVisiblePhotoCount();
	}

	public void setMap(File file) {
		mapDataManager.setMap(file);
		setChanged();
		notifyObservers(SELECTED_MAP_CHANGED);
	}

	public void clearCurrentMap() {
		mapDataManager.clearCurrentMap();
		setChanged();
		notifyObservers(SELECTED_MAP_CHANGED);
	}

	public MapData getMapData() {
		return mapDataManager;
	}

	public void loadMapParamLookup() {
		mapDataManager.loadMapParamLookup();
	}

	public void saveMapParamLookup() {
		mapDataManager.saveMapParamLookup();
	}
	
	public void addMapRefPoint(GeoRefPoint refPoint) {
		mapDataManager.addRefPoint(refPoint);
		setChanged();
		notifyObservers(MAP_DATA_CHANGED);
	}

	public void removeMapRefPoint(GeoRefPoint refPoint) {
		mapDataManager.removeRefPoint(refPoint);
		setChanged();
		notifyObservers(MAP_DATA_CHANGED);
	}

	

	public int exportPhotos(List<File> photos, File destDir,
			String destFileNameTemplate) {
		PhotoExporter exporter = new PhotoExporter();
		exporter.setDestination(destDir, destFileNameTemplate);
		int copied = exporter.copyFiles(photos);
		return copied;
	}
}
