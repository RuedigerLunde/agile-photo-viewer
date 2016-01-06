/*
 * Copyright (C) 2013 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.view.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;

import rl.photoviewer.controller.swing.Controller;

/**
 * Panel with Buttons to control navigation, slide show, full-screen mode etc.
 * 
 * @author Ruediger Lunde
 * 
 */
public class ControlPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	JButton selButton;
	JButton firstButton;
	JToggleButton slideShowButton;
	JSpinner slideShowSpinner;
	JToggleButton undecorateButton;
	JToggleButton sortByDateButton;
	JPopupMenu popup;
	JMenuItem helpMenuItem;
	JMenuItem exportMenuItem;
	JMenuItem deleteMenuItem;
	ActionListener listener;
	MouseListener popupListener;

	public ControlPanel(Controller controller) {
		setBackground(Color.DARK_GRAY);
		this.listener = controller;

		selButton = new JButton("Select");
		selButton.setActionCommand(Commands.SELECT_CMD);
		selButton.addActionListener(controller);
		integrate(selButton, controller);

		firstButton = new JButton("<<");
		firstButton.setActionCommand(Commands.FIRST_CMD);
		firstButton.addActionListener(controller);
		integrate(firstButton, controller);

		JButton button = new JButton("<");
		button.setActionCommand(Commands.PREV_CMD);
		button.addActionListener(controller);
		integrate(button, controller);

		button = new JButton(">");
		button.setActionCommand(Commands.NEXT_CMD);
		button.addActionListener(controller);
		integrate(button, controller);

		slideShowButton = new JToggleButton("Slide Show");
		slideShowButton.setActionCommand(Commands.SLIDE_SHOW_CMD);
		slideShowButton.addActionListener(controller);
		integrate(slideShowButton, controller);

		slideShowSpinner = new JSpinner();
		slideShowSpinner.setValue(5);
		slideShowSpinner.setPreferredSize(new Dimension(50, 25));
		integrate(slideShowSpinner, controller);

		sortByDateButton = new JToggleButton("Sort by Date");
		sortByDateButton.setActionCommand(Commands.SORT_BY_DATE_CMD);
		sortByDateButton.addActionListener(controller);
		integrate(sortByDateButton, controller);

		undecorateButton = new JToggleButton("Undecorate");
		undecorateButton.setActionCommand(Commands.DECORATION_CMD);
		undecorateButton.addActionListener(controller);
		integrate(undecorateButton, controller);

		popup = new JPopupMenu();
		helpMenuItem = new JMenuItem("Help");
		helpMenuItem.setActionCommand(Commands.HELP_CMD);
		helpMenuItem.addActionListener(controller);
		popup.add(helpMenuItem);

		JMenuItem item = new JMenuItem("Use Photo as Map");
		item.setActionCommand(Commands.USE_PHOTO_AS_MAP_CMD);
		item.addActionListener(controller);
		popup.add(item);

		item = new JMenuItem("Clear Map");
		item.setActionCommand(Commands.CLEAR_MAP_CMD);
		item.addActionListener(controller);
		popup.add(item);

		item = new JMenuItem("Full-Screen");
		item.setActionCommand(Commands.FULL_SCREEN_CMD);
		item.addActionListener(controller);
		popup.add(item);
		
		exportMenuItem = new JMenuItem("Export Visible Photos");
		exportMenuItem.setActionCommand(Commands.EXPORT_CMD);
		exportMenuItem.addActionListener(controller);
		popup.add(exportMenuItem);
		
		deleteMenuItem = new JMenuItem("Delete Selected Photo");
		deleteMenuItem.setActionCommand(Commands.DELETE_SELECTED_PHOTO_CMD);
		deleteMenuItem.addActionListener(controller);
		popup.add(deleteMenuItem);
		
		
		item = new JMenuItem("Increase Font Size");
		item.setActionCommand(Commands.INC_FONT_SIZE_CMD);
		item.addActionListener(controller);
		popup.add(item);
		item = new JMenuItem("Decrease Font Size");
		item.setActionCommand(Commands.DEC_FONT_SIZE_CMD);
		item.addActionListener(controller);
		popup.add(item);
		
		item = new JMenuItem("Exit");
		item.setActionCommand(Commands.EXIT_CMD);
		item.addActionListener(controller);
		popup.add(item);

		popupListener = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger())
					popup.show(e.getComponent(), e.getX(), e.getY());
				selButton.requestFocusInWindow();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger())
					popup.show(e.getComponent(), e.getX(), e.getY());
				selButton.requestFocusInWindow();
			}
		};
		addMouseListener(popupListener);
	}

	public boolean isSlideShowSelected() {
		return slideShowButton.isSelected();
	}

	public int getSlideShowSec() {
		return (Integer) slideShowSpinner.getValue();
	}

	public void setSlideShowSec(int sec) {
		slideShowSpinner.setValue(sec);
	}

	public boolean isUndecorateSelected() {
		return undecorateButton.isSelected();
	}

	public boolean isSortByDateSelected() {
		return sortByDateButton.isSelected();
	}

	public void setSortByDateSelected(boolean b) {
		sortByDateButton.setSelected(b);
	}

	public void setUndecorateSelected(boolean b) {
		undecorateButton.setSelected(b);
	}

	public void setDialogTriggerEnabled(boolean b) {
		selButton.setEnabled(b);
		helpMenuItem.setEnabled(b);
		exportMenuItem.setEnabled(b);
		deleteMenuItem.setEnabled(b);
	}

	public void integrate(JComponent comp, Controller controller) {
		super.add(comp);
		comp.addKeyListener(controller);
	}
}
