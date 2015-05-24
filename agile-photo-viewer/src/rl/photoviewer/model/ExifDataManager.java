/*
 * Copyright (C) 2013 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import rl.util.exceptions.ErrorHandler;
import rl.util.exceptions.PersistenceException;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

/**
 * Tool for collecting EXIF metadata for all photos in a directory. When
 * selecting a new photo, the tool checks whether the directory has changed and
 * updates the maintained metadata if necessary. The tool also provides means to
 * navigate to the previous or next photo in the directory using different
 * sorting criteria (file name or EXIF date).
 * 
 * @author Ruediger Lunde
 * 
 */
public class ExifDataManager {

	private File currDirectory;
	private List<PhotoMetadata> photoDataList = Collections.emptyList();
	/** Metadata of the currently selected photo. */
	private PhotoMetadata selectedPhotoData;
	private int minRating;
	private KeywordExpression keywordExpression = new KeywordExpression();
	private HashSet<PhotoMetadata> visiblePhotoData = new HashSet<PhotoMetadata>();
	private List<String> allKeywords = Collections.emptyList();
	private List<Integer> keywordCounts = Collections.emptyList();

	/** Flag to enable detailed metadata display on console for selected photos. */
	private boolean debug = false;

	public void enableDebugMode(boolean b) {
		debug = b;
	}

	public void setCurrDirectory(File dir, boolean sortByDate) {
		selectedPhotoData = null;
		boolean dirChanged = !dir.equals(currDirectory);
		if (dirChanged) {
			List<PhotoMetadata> newPhotoDataList = new ArrayList<PhotoMetadata>();
			List<String> newKeywordList = new ArrayList<String>();
			List<Integer> newKeywordCounts = new ArrayList<Integer>();
			collectMetadata(dir, newPhotoDataList, newKeywordList,
					newKeywordCounts);
			Collections.sort(newKeywordList);
			updateStoredMetadata(dir, newPhotoDataList, newKeywordList,
					newKeywordCounts);
		}
		changeOrder(sortByDate);
		if (dirChanged)
			setVisibility(0, new KeywordExpression());
	}

	public File getCurrDirectory() {
		return currDirectory;
	}

	public void setVisibility(int minRating, KeywordExpression expression) {
		this.minRating = minRating;
		keywordExpression = expression;
		visiblePhotoData.clear();
		for (PhotoMetadata data : photoDataList)
			if (data.getRating() >= minRating && keywordExpression.checkKeywords(data.getKeywords()))
				visiblePhotoData.add(data);
	}

	public int getRatingFilter() {
		return minRating;
	}
	
	public KeywordExpression getVisibilityExpression() {
		return keywordExpression;
	}

	public synchronized void changeOrder(boolean sortByDate) {
		Comparator<PhotoMetadata> comp = sortByDate ? new PhotoMetadata.SortByDateComparator()
				: new PhotoMetadata.SortByFileNameComparator();
		Collections.sort(photoDataList, comp);
		for (int i = 0; i < photoDataList.size(); i++) {
			photoDataList.get(i).setIndex(i);
		}
	}

	public List<String> getAllKeywords() {
		return allKeywords;
	}

	public List<Integer> getKeywordCounts() {
		return keywordCounts;
	}

	/**
	 * Returns the number of files with photo format in the current directory
	 * which pass the keyword filter.
	 */
	public int getVisiblePhotoCount() {
		return visiblePhotoData.size();
	}

	public synchronized List<File> getVisiblePhotos() {
		List<File> result = new ArrayList<File>();
		for (int i = 0; i < photoDataList.size(); i++)
			if (visiblePhotoData.contains(photoDataList.get(i)))
				result.add(getFile(i));
		return result;
	}

	public Set<? extends IndexedGeoPoint> getVisiblePhotoPositions() {
		return visiblePhotoData;
	}

	public File selectPhoto(String fileName) {
		int index = getIndexInMetadataList(fileName);
		File result = null;
		if (index != -1) {
			selectedPhotoData = photoDataList.get(index);
			result = getFile(index);
			if (debug)
				printMetadataForDebugging(result);
		}
		return result;
	}

	public File selectFirstPhoto() {
		selectedPhotoData = null;
		return selectNextPhoto();
	}

	public synchronized File selectNextPhoto() {
		File result = null;
		if (getVisiblePhotoCount() > 0) {
			int currIndex = getSelectedPhotoIndex();
			int newIndex = currIndex;
			do {
				newIndex = (newIndex + 1) % photoDataList.size();
			} while (newIndex != currIndex
					&& !visiblePhotoData.contains(photoDataList.get(newIndex)));
			selectedPhotoData = photoDataList.get(newIndex);
			result = getFile(newIndex);
		} else {
			selectedPhotoData = null;
		}
		return result;
	}

	public synchronized File selectPreviousPhoto() {
		File result = null;
		if (getVisiblePhotoCount() > 0) {
			int currIndex = getSelectedPhotoIndex();
			int newIndex = currIndex;
			do {
				newIndex = (newIndex - 1 + photoDataList.size())
						% photoDataList.size();
			} while (newIndex != currIndex
					&& !visiblePhotoData.contains(photoDataList.get(newIndex)));
			selectedPhotoData = photoDataList.get(newIndex);
			result = getFile(newIndex);
		} else {
			selectedPhotoData = null;
		}
		return result;
	}

	/** Might be null! */
	public PhotoMetadata getSelectedPhotoData() {
		return selectedPhotoData;
	}

	/** Might be -1 */
	private int getSelectedPhotoIndex() {
		return selectedPhotoData != null ? selectedPhotoData.getIndex() : -1;
	}

	public synchronized void deleteSelectedPhoto() {
		int currIndex = getSelectedPhotoIndex();
		if (currIndex != -1) {
			File file = getFile(currIndex);
			if (file != null && file.exists()) {
				if (file.delete()) {
					photoDataList.remove(currIndex);
					visiblePhotoData.remove(selectedPhotoData);
					for (int i = currIndex; i < photoDataList.size(); i++)
						photoDataList.get(i).setIndex(i);
					for (String k : selectedPhotoData.getKeywords()) {
						int idx = allKeywords.indexOf(k);
						keywordCounts.set(idx, keywordCounts.get(idx) - 1);
					}
					selectedPhotoData = null;
					if (getVisiblePhotoCount() > 0) {
						if (currIndex > 0)
							selectedPhotoData = photoDataList
									.get(currIndex - 1);
						selectNextPhoto();
					}
				}
			}
		}
	}

	private File getFile(int index) {
		if (index != -1)
			return new File(currDirectory, photoDataList.get(index)
					.getFileName());
		else
			return null;
	}

	private void collectMetadata(File dir, List<PhotoMetadata> newMetadata,
			List<String> newKeywords, List<Integer> newKeywordCount) {
		File[] files = dir.listFiles();
		Hashtable<String, Integer> keywordHash = new Hashtable<String, Integer>();
		for (File f : files) {
			try {
				Metadata metadata = ImageMetadataReader.readMetadata(f);
				PhotoMetadata data = new PhotoMetadata(f, metadata);
				newMetadata.add(data);
				for (String keyword : data.getKeywords()) {
					Integer i = keywordHash.get(keyword);
					i = (i == null) ? 1 : i + 1;
					keywordHash.put(keyword, i);
				}
			} catch (Exception ex) { // ImageProcessingException, IOException
				if (isImageFormatSupported(f)) {
					Exception e = new PersistenceException(
							"Could not read metadata of file " + f.getName()
									+ ".", ex);
					ErrorHandler.getInstance().handleWarning(e);
					newMetadata.add(new PhotoMetadata(f));
				}
			}
		}
		newKeywords.addAll(keywordHash.keySet());
		Collections.sort(newKeywords);
		for (String keyword : newKeywords)
			newKeywordCount.add(keywordHash.get(keyword));
	}

	private boolean isImageFormatSupported(File file) {
		String name = file.getName();
		for (String ext : ImageIO.getReaderFormatNames())
			if (name.toLowerCase().endsWith("." + ext.toLowerCase()))
				return true;
		return false;
	}

	private synchronized void updateStoredMetadata(File dir,
			List<PhotoMetadata> newPhotoDataList, List<String> newKeywordList,
			List<Integer> newKeywordCount) {
		currDirectory = dir;
		photoDataList = newPhotoDataList;
		allKeywords = newKeywordList;
		keywordCounts = newKeywordCount;
	}

	private int getIndexInMetadataList(String fileName) {
		for (int i = 0; i < photoDataList.size(); i++)
			if (photoDataList.get(i).getFileName().equals(fileName))
				return i;
		return -1;
	}

	private void printMetadataForDebugging(File file) {
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(file);
			for (Directory dir : metadata.getDirectories()) {
				for (Tag tag : dir.getTags()) {
					System.out.println(tag);
				}
			}
		} catch (Exception ex) {
			Exception e = new PersistenceException(
					"Could not read metadata of file " + file.getName() + ".",
					ex);
			ErrorHandler.getInstance().handleWarning(e);
		}
	}
}
