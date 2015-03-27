/*
 * Copyright (C) 2013 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.model;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Set;

import javax.imageio.ImageIO;

import rl.util.exceptions.ErrorHandler;
import rl.util.exceptions.PersistenceException;

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

	private Image currImage;
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
	public boolean selectPhoto(File file, boolean sortByDate) {
		boolean result = false;
		File dir = file;
		String fileName = null;
		if (!dir.isDirectory()) {
			dir = file.getParentFile();
			fileName = file.getName();
		}
		if (!dir.equals(getCurrDirectory())) {
			setMap(null);
			result = true;
		}
		exifDataManager.setCurrDirectory(dir, sortByDate);
		File f = null;
		if (fileName != null)
			f = exifDataManager.selectPhoto(fileName);
		else if (exifDataManager.getVisiblePhotoCount() > 0)
			f = exifDataManager.selectFirstPhoto();
		loadImage(f);
		setChanged();
		notifyObservers(METADATA_CHANGED);
		return result;
	}

	public void setVisibility(VisibilityExpression expression) {
		exifDataManager.setVisibility(expression);
		setChanged();
		notifyObservers();
	}

	public VisibilityExpression getVisibilityExpression() {
		return exifDataManager.getVisibilityExpression();
	}

	/** Selects the first photo in the current directory. */
	public void selectFirstPhoto() {
		File file = exifDataManager.selectFirstPhoto();
		loadImage(file);
	}

	public void selectPhoto(IndexedGeoPoint pos) {
		if (pos instanceof PhotoMetadata) {
			PhotoMetadata data = (PhotoMetadata) pos;
			File f = exifDataManager.selectPhoto(data.getFileName());
			loadImage(f);
			setChanged();
			notifyObservers();
		}
	}

	/**
	 * Searches for the previous photo in the current directory and selects it.
	 */
	public void selectPrevPhoto() {
		File file = exifDataManager.selectPreviousPhoto();
		loadImage(file);
	}

	/**
	 * Searches for the next photo in the current directory and selects it.
	 */
	public void selectNextPhoto() {
		File file = exifDataManager.selectNextPhoto();
		loadImage(file);
	}

	/** Changes the order of the photos. Options: Order by name or by date. */
	public void changeOrder(boolean sortByDate) {
		exifDataManager.changeOrder(sortByDate);
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

	public File getSelectedPhotoFile() {
		PhotoMetadata data = getSelectedPhotoData();
		if (data != null)
			return new File(getCurrDirectory(), data.getFileName());
		else
			return null;
	}

	public Image getSelectedPhotoImage() {
		return currImage;
	}

	public PhotoMetadata getSelectedPhotoData() {
		return exifDataManager.getSelectedPhotoData();
	}

	public void deleteSelectedPhoto() {
		exifDataManager.deleteSelectedPhoto();
		loadImage(getSelectedPhotoFile());
		setChanged();
		notifyObservers();
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
		notifyObservers();
	}

	public void clearCurrentMap() {
		mapDataManager.clearCurrentMap();
		setChanged();
		notifyObservers();
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

	public int exportPhotos(List<File> photos, File destDir,
			String destFileNameTemplate) {
		PhotoExporter exporter = new PhotoExporter();
		exporter.setDestination(destDir, destFileNameTemplate);
		int copied = exporter.copyFiles(photos);
		return copied;
	}

	/** Accepts null! */
	private void loadImage(File file) {
		Image image = null;
		if (file != null) {
			try {
				image = ImageIO.read(file);
			} catch (IOException ex) {
				Exception e = new PersistenceException(
						"Could not read image from file " + file + ".", ex);
				ErrorHandler.getInstance().handleError(e);
			}
		}
		Image save = currImage;
		currImage = image;
		setChanged();
		notifyObservers();
		if (save != null)
			save.flush();
	}
}
