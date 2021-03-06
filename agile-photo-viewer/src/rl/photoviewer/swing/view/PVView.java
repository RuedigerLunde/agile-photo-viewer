/*
 * Copyright (C) 2013-2016 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.swing.view;

import rl.photoviewer.model.PVModel;
import rl.photoviewer.model.PhotoMetadata;
import rl.photoviewer.swing.AgilePhotoViewerApp;
import rl.photoviewer.swing.controller.ControllerProxy;
import rl.photoviewer.swing.controller.PVController;
import rl.util.exceptions.ErrorHandler;
import rl.util.exceptions.PersistenceException;
import rl.util.persistence.PropertyManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * Defines the main frame of the Agile Photo Viewer application. It consists of
 * a photo panel (showing a scaled image), a control panel (for navigation,
 * slide show control etc.), an EXIF data view panel and a keyword panel.
 * 
 * @author Ruediger Lunde
 * 
 */
public class PVView implements PropertyChangeListener {

	protected JFrame frame;
	private JSplitPane splitPane;
	private JTabbedPane tabbedPane;
	private ControlPanel ctrlPanel;
	private InfoPanel infoPanel;
	private VisibilityPanel visibilityPanel;
	private StatusArea statusArea;
	private ImagePanel photoPanel;
	private MapImagePanel mapImagePanel;
	private JFileChooser inputFileChooser;
	private JFileChooser outputFileChooser;

	private ControllerProxy controller;
	private PVModel model;

	/** Creates the application objects including frame layout. */
	public PVView(PVModel model) {
		this.model = model;

		frame = new JFrame("Agile Photo Viewer");
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				ActionEvent e = new ActionEvent(this, 0, Commands.EXIT_CMD);
				controller.actionPerformed(e);
			}
		});
		controller = new ControllerProxy();

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		frame.add(splitPane);

		photoPanel = new ImagePanel();
		photoPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
		photoPanel.addMouseListener(controller);

		splitPane.setRightComponent(photoPanel);
		splitPane.setDividerSize(5);
		splitPane.setForeground(Color.BLACK);

		inputFileChooser = new JFileChooser();
		inputFileChooser
				.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		outputFileChooser = new JFileChooser();
		outputFileChooser
				.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		JPanel leftPanel = new JPanel(new GridBagLayout());
		GridBagConstraints constr = new GridBagConstraints();
		constr.fill = GridBagConstraints.BOTH;
		constr.weightx = 1;
		constr.weighty = 1;
		constr.gridwidth = 1;
		constr.gridheight = 1;
		constr.gridx = 0;
		
		leftPanel.setMinimumSize(new Dimension(0, 0));
		leftPanel.setBackground(Color.DARK_GRAY);
		splitPane.setLeftComponent(leftPanel);
		ctrlPanel = new ControlPanel(controller);
		ctrlPanel.setMinimumSize(new Dimension(10, 150));
		
		constr.gridy = 0;
		leftPanel.add(ctrlPanel, constr);

		statusArea = new StatusArea();
		statusArea.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
		statusArea.addMouseListener(ctrlPanel.popupListener);
		constr.gridy = 1;
		leftPanel.add(statusArea, constr);
		
		tabbedPane = new JTabbedPane();
		tabbedPane.setBackground(Color.DARK_GRAY);
		infoPanel = new InfoPanel(controller);
		infoPanel.addKeyListener(controller);
		infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		infoPanel.setMinimumSize(new Dimension(0, 0));
		tabbedPane.addTab("Info", infoPanel);
		visibilityPanel = new VisibilityPanel(model, controller);
		tabbedPane.addTab("Visibility", visibilityPanel);
		mapImagePanel = new MapImagePanel(model, controller);
		mapImagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
		tabbedPane.addTab("Map", mapImagePanel);
		
		constr.weighty = 10;
		constr.gridy = 2;
		leftPanel.add(tabbedPane, constr);
		
		ErrorHandler.setInstance(statusArea.createStatusErrorHandler());
	}

	/**
	 * Restores view settings according to the settings of the last session.
	 */
	public void restoreSession() {
		try {
			PropertyManager pm = PropertyManager.getInstance();
			frame.setSize(pm.getIntValue("gui.window.width", 800),
					pm.getIntValue("gui.window.height", 600));
			splitPane.setDividerLocation(pm.getIntValue(
					"gui.window.dividerlocation", 140));
			ctrlPanel.setSlideShowSec(pm.getIntValue("gui.slideshowsec", 5));
			ctrlPanel.setSortByDateSelected(pm.getBooleanValue(
					"gui.sortbydate", false));
			mapImagePanel.setShowAllPhotoPositions(pm.getBooleanValue(
					"gui.showallphotopositions", true));
			infoPanel.setShowCaptionInStatus(pm.getBooleanValue(
					"gui.showcaptioninstatus", true));
			float size = (float) pm.getDoubleValue("gui.fontsize", 12);
			
			if (size > 24) {
				// hack - hope there will be better solutions for hd displays in future...
				float factor = size / 24f;
				AgilePhotoViewerApp.updateDefaultFontSize(factor);
				SwingUtilities.updateComponentTreeUI(frame.getContentPane());
				SwingUtilities.updateComponentTreeUI(inputFileChooser);
				SwingUtilities.updateComponentTreeUI(outputFileChooser);
				SwingUtilities.updateComponentTreeUI(ctrlPanel.popup);
				Dimension d = ctrlPanel.firstButton.getPreferredSize();
				ctrlPanel.slideShowSpinner.setPreferredSize(d);
			}
			
			Font font = statusArea.getFont().deriveFont(size);
			setInfoFont(font);
			tabbedPane.setSelectedIndex(pm.getIntValue("gui.selectedtab", 0));
			String outputFileName = pm.getStringValue("gui.outputfile", null);
			if (outputFileName != null)
				outputFileChooser.setSelectedFile(new File(outputFileName));
			model.loadMapParamLookup();
			String fileName = pm.getStringValue("model.currfile", null);
			if (fileName != null && model.getCurrDirectory() == null) {
				File f = new File(fileName);
				if (f.exists())
					model.selectPhoto(f);
			}
			fileName = pm.getStringValue("model.currmapfile", "");
			if (!fileName.isEmpty()) {
				File f = new File(fileName);
				if (f.exists())
					model.setMap(f);
			}
		} catch (Exception ex) {
			ErrorHandler.getInstance().handleError(ex);
		}
	}

	/** Saves session settings. */
	public void storeSession() {
		frame.setVisible(false);
		PropertyManager pm = PropertyManager.getInstance();
		pm.setValue("gui.window.width", frame.getSize().width);
		pm.setValue("gui.window.height", frame.getSize().height);
		pm.setValue("gui.window.dividerlocation",
				splitPane.getDividerLocation());
		pm.setValue("gui.slideshowsec", ctrlPanel.getSlideShowSec());
		pm.setValue("gui.sortbydate", ctrlPanel.isSortByDateSelected());
		pm.setValue("gui.showallphotopositions",
				mapImagePanel.isShowAllPhotoPositions());
		pm.setValue("gui.fontsize", statusArea.getFont().getSize());
		pm.setValue("gui.showcaptioninstatus",
				infoPanel.isShowCaptionInStatus());
		pm.setValue("gui.selectedtab", tabbedPane.getSelectedIndex());
		File file = outputFileChooser.getSelectedFile();
		if (file != null)
			pm.setValue("gui.outputfile", file.getAbsolutePath());
		else if (model.getCurrDirectory() != null)
			pm.setValue("gui.outputfile", model.getCurrDirectory());
		if (model.getSelectedPhoto() != null)
			pm.setValue("model.currfile", model.getSelectedPhoto());
		file = model.getMapData().getFile();
		pm.setValue("model.currmapfile", file != null ? file.getAbsolutePath()
				: "");
		model.saveMapParamLookup();
		try {
			pm.saveSessionProperties();
		} catch (PersistenceException ex) {
			ErrorHandler.getInstance().handleError(ex);
		}
	}

	public void setController(PVController controller) {
		this.controller.setController(controller);
	}

	public JFrame getFrame() {
		return frame;
	}

	public ControlPanel getCtrlPanel() {
		return ctrlPanel;
	}

	public StatusArea getStatusPanel() {
		return statusArea;
	}

	public InfoPanel getInfoPanel() {
		return infoPanel;
	}

	public MapImagePanel getMapImagePanel() {
		return mapImagePanel;
	}

	public ImagePanel getPhotoPanel() {
		return photoPanel;
	}

	public VisibilityPanel getVisibilityPanel() {
		return visibilityPanel;
	}

	public void setInfoFont(Font font) {
		getStatusPanel().setFont(font);
		getInfoPanel().setFont(font);
		getVisibilityPanel().setFont(font);
		getMapImagePanel().setFont(font);
	}
	
	
	public File showInputFileChooser(File file) {
		File result = null;
		if (file != null)
			inputFileChooser.setSelectedFile(file);
		if (JFileChooser.APPROVE_OPTION == inputFileChooser
				.showOpenDialog(frame))
			result = inputFileChooser.getSelectedFile();
		return result;
	}

	public File showOutputFileChooser(int fileCount) {
		File result = null;
		if (JFileChooser.APPROVE_OPTION == outputFileChooser.showDialog(frame,
				"Export " + fileCount + " Photos"))
			result = outputFileChooser.getSelectedFile();
		return result;
	}

	public void selectMapTab() {
		tabbedPane.setSelectedComponent(mapImagePanel);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		PhotoMetadata data = model.getSelectedPhotoData();
		infoPanel.update(data);
		mapImagePanel.update(data, model.getVisiblePhotoPositions());
		String txt = "Keyword Expression:\n"
				+ model.getVisibilityExpression().toString() + "\n"
				+ model.getVisiblePhotoCount() + " photo(s) visible.";
		visibilityPanel.setText(txt);
		photoPanel.setImage(model.getSelectedPhoto(), PhotoMetadata.getOrientation(data));
	}
}
