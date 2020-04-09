/*
 * Copyright (C) 2013-2016 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */

package rl.photoviewer.swing;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import rl.photoviewer.model.PVModel;
import rl.photoviewer.swing.controller.PVController;
import rl.photoviewer.swing.view.Commands;
import rl.photoviewer.swing.view.PVView;
import rl.util.exceptions.ErrorHandler;
import rl.util.persistence.PropertyManager;

/**
 * Central class for creating and running the Agile Photo Viewer application.
 * The architecture of the viewer strictly follows the model-view-controller
 * pattern.
 * 
 * @author Ruediger Lunde
 * 
 */
public class AgilePhotoViewerApp {

	PVView view;
	PVController controller;
	PVModel model;

	/** Creates model, view, and controller and connects the three components. */
	public AgilePhotoViewerApp() {
		model = new PVModel();
		view = new PVView(model);
		controller = new PVController(view, model);
		model.addPropertyChangeListener(view);
	}

	public AgilePhotoViewerApp(File photo) {
		this();
		model.selectPhoto(photo);
	}

	/** Restores the last session and starts the application. */
	public void start() {
		try {
			String home = System.getProperty("user.home");
			File propDir = new File(home, ".agilephotoviewer");
			if (!propDir.exists())
				propDir.mkdir();
			PropertyManager.setApplicationDataDirectory(propDir);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ActionEvent e = new ActionEvent(this, 0, Commands.RESTORE_SESSION_CMD);
		controller.actionPerformed(e);
		view.getFrame().setVisible(true);
	}

	// works only with metal L&F...
	public static void updateDefaultFontSize(float factor) {
		// float screenRes = Toolkit.getDefaultToolkit().getScreenResolution();
		// float factor = (float) (screenRes / 72.0 * 0.8);
		UIDefaults defaults = UIManager.getDefaults();
		List<Object> keys = new ArrayList<>();
		List<Object> values = new ArrayList<>();

		for (Entry<Object, Object> entry : defaults.entrySet()) {
			Font font = UIManager.getFont(entry.getKey());
			if (font != null) { // && entry.getValue() instanceof FontUIResource) {
				//System.out.println(entry.getKey() + ":    " + entry.getValue());
				float size = Math.round(font.getSize2D() * factor);
				keys.add(entry.getKey());
				values.add(new FontUIResource(font.deriveFont(size)));
			}
		}
		for (int i = 0; i < keys.size(); i++)
			UIManager.put(keys.get(i), values.get(i));
	}

	/** Creates and starts the application. */
	public static void main(String[] args) {
		try {
			Locale.setDefault(Locale.US);
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager
					.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			//UIManager
			//		.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			ErrorHandler.getInstance().handleWarning(e);
		}
		// updateDefaultFontSize();
		new AgilePhotoViewerApp().start();
	}
}
