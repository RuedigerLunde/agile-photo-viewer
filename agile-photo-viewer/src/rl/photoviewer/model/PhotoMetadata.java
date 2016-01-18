/*
 * Copyright (C) 2013-2016 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.model;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import rl.util.exceptions.ErrorHandler;

import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.iptc.IptcDirectory;
import com.drew.metadata.xmp.XmpDirectory;

/**
 * Container for selected Exif metadata. Currently, for each photo the file
 * name, caption, date, camera model, latitude and longitude values, as well as
 * all keywords are stored.
 * 
 * @author Ruediger Lunde
 * 
 */
public class PhotoMetadata implements IndexedGeoPoint {
	private int index;
	private String fileName;
	private String caption;
	private int rating;
	private Date date;
	private String exposureTime;
	private double aperture = Double.NaN;
	private String focalLength;
	private String iso;
	private String lensModel;
	private String model;
	private int orientation;
	private double lat = Double.NaN;
	private double lon = Double.NaN;
	private List<String> keywords;

	public PhotoMetadata(File file) {
		fileName = file.getName();
		keywords = Collections.emptyList();
	}

	public PhotoMetadata(File file, Metadata metadata) {
		this(file);
		ExifSubIFDDirectory dir1 = metadata.getDirectory(ExifSubIFDDirectory.class);
		if (dir1 != null) {
			date = dir1.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
			exposureTime = dir1.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME);
			try {
				aperture = dir1.getDouble(ExifSubIFDDirectory.TAG_APERTURE);
			} catch (MetadataException e) {
				// no aperture data available - ignore...
			}
			focalLength = dir1.getString(ExifSubIFDDirectory.TAG_35MM_FILM_EQUIV_FOCAL_LENGTH);
			iso = dir1.getString(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT);
			lensModel = dir1.getString(ExifSubIFDDirectory.TAG_LENS_MODEL);
		}
		ExifIFD0Directory dir2 = metadata.getDirectory(ExifIFD0Directory.class);
		if (dir2 != null) {
			model = dir2.getString(ExifIFD0Directory.TAG_MODEL);
			// [Exif IFD0] Orientation - Right side, top (Rotate 90 CW) -> 6
			try {
				orientation = dir2.getInt(ExifIFD0Directory.TAG_ORIENTATION);
			} catch (MetadataException e) {
				// wrong orientation value - forget it.
			}
		}
		GpsDirectory dir3 = metadata.getDirectory(GpsDirectory.class);
		if (dir3 != null) {
			GeoLocation loc = dir3.getGeoLocation();
			if (loc != null) {
				lat = loc.getLatitude();
				lon = loc.getLongitude();
			}
		}
		IptcDirectory dir4 = metadata.getDirectory(IptcDirectory.class);
		if (dir4 != null) {
			caption = dir4.getString(IptcDirectory.TAG_CAPTION);
			if (caption != null) {
				// fix UTF8 bug in library of Drew Noakes.
				String oldEn = System.getProperty("file.encoding");
				String newEn = dir4.getString(IptcDirectory.TAG_CODED_CHARACTER_SET);
				if (oldEn != null && newEn != null && newEn.equals('\u001b' + "%G")) {
					try {
						byte[] cap = caption.getBytes(oldEn);
						caption = new String(cap, "UTF8");
					} catch (UnsupportedEncodingException e) {
						ErrorHandler.getInstance().handleWarning(e);
					}
				}
			}
			if (dir4.getKeywords() != null)
				keywords = dir4.getKeywords();
		}
		XmpDirectory dir5 = metadata.getDirectory(XmpDirectory.class);
		if (dir5 != null) {
			try {
				rating = dir5.getInt(XmpDirectory.TAG_RATING);
			} catch (Exception e) {
				// nothing to do...
			}
		}
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getFileName() {
		return fileName;
	}

	public String getCaption() {
		return caption;
	}

	public int getRating() {
		return rating;
	}

	public Date getDate() {
		return date;
	}

	public String getExposureTime() {
		return exposureTime;
	}

	public double getAperture() {
		return aperture;
	}

	public String getFocalLength() {
		return focalLength;
	}

	public String getIso() {
		return iso;
	}

	public String getModel() {
		return model;
	}

	public String getLensModel() {
		return lensModel;
	}

	/** Latitude. Value NaN indicates no latitude value available. */
	public double getLat() {
		return lat;
	}

	/** Longitude. Value NaN indicates no longitude value available. */
	public double getLon() {
		return lon;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public int getOrientation() {
		return orientation;
	}

	/** Returns 0 if data is null. */
	public static int getOrientation(PhotoMetadata data) {
		int result = 0;
		if (data != null)
			result = data.getOrientation();
		return result;
	}

	public String toString() {
		StringBuffer text = new StringBuffer();
		text.append("File:\n  " + getFileName());
		if (getCaption() != null)
			text.append("\nCaption:\n  " + getCaption());
		if (getRating() != 0)
			text.append("\nRating:\n  " + "******".substring(0, getRating()));
		if (getDate() != null)
			text.append("\nDate:\n  " + getDate());
		if (getExposureTime() != null && !Double.isNaN(getAperture())) {
			DecimalFormat df = new DecimalFormat("##.#");
			text.append("\nExposure:\n  " + getExposureTime() + " at f / " + df.format(getAperture()));
		}
		if (getFocalLength() != null)
			text.append("\nFocal Length:\n  " + getFocalLength());
		if (getIso() != null)
			text.append("\nIso:\n  " + getIso());
		if (getModel() != null)
			text.append("\nModel:\n  " + getModel());
		if (getLensModel() != null)
			text.append("\nLens Model:\n  " + getLensModel());
		if (!Double.isNaN(getLat())) {
			DecimalFormat df = new DecimalFormat("###.####");
			text.append("\nLat:\n  " + df.format(getLat()) + "\nLon:\n  " + df.format(getLon()));
		}
		if (!getKeywords().isEmpty()) {
			text.append("\nKeywords:");
			for (String key : getKeywords())
				text.append("\n  " + key);
		}
		return text.toString();
	}

	/** Compares the file names. */
	public static class SortByFileNameComparator implements Comparator<PhotoMetadata> {
		@Override
		public int compare(PhotoMetadata obj1, PhotoMetadata obj2) {
			return obj1.getFileName().compareTo(obj2.getFileName());
		}
	}

	/** Compares photo dates (Exif) if available. */
	public static class SortByDateComparator implements Comparator<PhotoMetadata> {
		@Override
		public int compare(PhotoMetadata obj1, PhotoMetadata obj2) {
			if (obj1.date != null && obj2.getDate() != null)
				return obj1.date.compareTo(obj2.getDate());
			else if (obj1.date != null)
				return -1;
			else if (obj2.getDate() != null)
				return 1;
			else
				return obj1.getFileName().compareTo(obj2.getFileName());
		}

	}
}
