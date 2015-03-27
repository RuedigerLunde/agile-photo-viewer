/*
 * Copyright (C) 2013 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.List;

import rl.util.exceptions.ErrorHandler;
import rl.util.exceptions.PersistenceException;

/**
 * Provides methods to copy and rename files.
 * @author Ruediger Lunde
 * 
 */
public class PhotoExporter {

	File destDir;
	/**
	 * Part of the file name template in front of the number. Value null means
	 * renaming is disabled.
	 */
	String namePrefix;
	/** Part of the file name template after the dot. E.g. 'jpg'. */
	String nameSuffix;
	/** Something like '000'. */
	String numFormatString;
	/** Number which is incremented when exporting a photo. */
	int currIndex;

	/**
	 * Expects a directory and a file name template of the form first prefix
	 * then optionally number then optionally suffix. E.g.
	 * 'Scotland2012-005.jpg', 'Scotland2012-.jpg', 'Scotland2012-'.
	 * 
	 * @param destFileNameTemplate
	 *            possibly null (copy without renaming).
	 */
	public void setDestination(File destDir, String destFileNameTemplate) {
		this.destDir = destDir;
		if (destFileNameTemplate != null) {
			int dotPos = destFileNameTemplate.lastIndexOf('.');
			if (dotPos == -1) {
				dotPos = destFileNameTemplate.length();
				nameSuffix = null;
			} else
				nameSuffix = destFileNameTemplate.substring(dotPos);
			int numPos = dotPos;
			numFormatString = "";
			while (Character.isDigit(destFileNameTemplate.charAt(numPos - 1))) {
				numPos--;
				numFormatString += "0";
			}
			namePrefix = destFileNameTemplate.substring(0, numPos);
			if (numPos < dotPos)
				currIndex = Integer.parseInt(destFileNameTemplate.substring(
						numPos, dotPos)) + 1;
			else
				currIndex = 1;
		} else {
			namePrefix = null;
		}
	}

	/**
	 * Copies the specified files to the previously specified destination
	 * directory. If a file name pattern was provided, the files are renamed
	 * accordingly.
	 * 
	 * @return Number of copied files.
	 */
	public int copyFiles(List<File> files) {
		int result = 0;
		if (namePrefix != null)
			while (Math.log10(files.size()) + 1e-5 > numFormatString.length())
				numFormatString += "0";
		for (File file : files) {
			Path source = Paths.get(file.getAbsolutePath());
			String name = getDestFileName(file.getName());
			Path dest = Paths.get(new File(destDir, name).getAbsolutePath());
			try {
				Files.copy(source, dest);
				result++;
			} catch (IOException ex) {
				Exception e = new PersistenceException("Could not copy file "
						+ file.getName() + ".", ex);
				ErrorHandler.getInstance().handleWarning(e);
			}
		}
		return result;
	}

	private String getDestFileName(String sourceFileName) {
		if (namePrefix == null)
			return sourceFileName;
		else {
			String result = namePrefix;
			result += new DecimalFormat(numFormatString).format(currIndex++);
			if (nameSuffix != null)
				result += nameSuffix;
			else {
				int dotPos = sourceFileName.lastIndexOf('.');
				if (dotPos != -1)
					result += sourceFileName.substring(dotPos);
			}
			return result;
		}
	}

	// public static void main(String[] args) {
	// PhotoExporter pe = new PhotoExporter();
	// pe.setDestination(new File("/ab/c"), "d");
	// System.out.println(pe.getDestFileName("x.z").equals("d1.z"));
	// System.out.println(pe.getDestFileName("xy.z").equals("d2.z"));
	// pe.setDestination(new File("/ab/c/"), "d-009.e");
	// System.out.println(pe.getDestFileName("x.z").equals("d-010.e"));
	// System.out.println(pe.getDestFileName("xy.z").equals("d-011.e"));
	// }
}
