/*
 * Copyright (C) 2013-2016 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.swing.view;

import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import rl.util.exceptions.ErrorHandler;
import rl.util.exceptions.PersistenceException;

/**
 * Shows an about dialog with version information, feature list, and some advises
 * for users which are new to the software.
 * 
 * @author Ruediger Lunde
 */
public class AboutDialog {

	/**
	 * Tries to load the about information from a HTML file and shows it in a
	 * dialog.
	 */
	public static void showAboutDialog(JFrame parent) {
		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		java.net.URL helpURL = AboutDialog.class.getResource("About.html");
		if (helpURL != null) {
			try {
				editorPane.setPage(helpURL);
			} catch (IOException e) {
				Exception ex = new PersistenceException("Attempted to read a bad URL: " + helpURL);
				ErrorHandler.getInstance().handleError(ex);
			}
		} else {
			Exception ex = new PersistenceException("Couldn't find file: About.html");
			ErrorHandler.getInstance().handleError(ex);
		}
		JScrollPane scroller = new JScrollPane(editorPane);
		scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setPreferredSize(new Dimension(500, 400));
		JOptionPane.showMessageDialog(parent, scroller);
	}
}
