/*
 * Copyright (C) 2013 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.view;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;

import rl.photoviewer.controller.Controller;
import rl.photoviewer.model.PhotoMetadata;

public class InfoPanel extends JTextArea {
	private static final long serialVersionUID = 1L;
	Controller controller;
	private boolean showCaptionInStatus;

	public InfoPanel(Controller controller) {
		this.controller = controller;
		setBackground(Color.DARK_GRAY);
		setForeground(Color.LIGHT_GRAY);
		setEditable(false);
		addMouseListener(new MyMouseAdapter());
	}
	
	public boolean isShowCaptionInStatus() {
		return showCaptionInStatus;
	}

	public void setShowCaptionInStatus(boolean showCaptionInStatus) {
		this.showCaptionInStatus = showCaptionInStatus;
	}

	public void update(PhotoMetadata data) {
		StringBuffer text = new StringBuffer();
		if (data != null) {
			text.append("File:\n  " + data.getFileName());
			if (data.getCaption() != null)
				text.append("\nCaption:\n  " + data.getCaption());
			if (data.getDate() != null)
				text.append("\nDate:\n  " + data.getDate());
			if (data.getModel() != null)
				text.append("\nModel:\n  " + data.getModel());
			if (!Double.isNaN(data.getLat())) {
				DecimalFormat df = new DecimalFormat("###.####");
				text.append("\nLat:\n  " + df.format(data.getLat()) + "\nLon:\n  "
						+ df.format(data.getLon()));
			}
			if (!data.getKeywords().isEmpty()) {
				text.append("\nKeywords:");
				for (String key : data.getKeywords())
					text.append("\n  " + key);
			}
		}
		setText(text.toString());
	}
	
	private class MyPopup extends JPopupMenu implements ActionListener {
		private static final long serialVersionUID = 1L;
		JCheckBoxMenuItem showCaptionCheckBox;

		private MyPopup() {
			showCaptionCheckBox = new JCheckBoxMenuItem(
					"Show Caption in Status Field");
			showCaptionCheckBox.setActionCommand(Commands.SHOW_CAPTION_IN_STATUS_CMD);
			showCaptionCheckBox.setSelected(showCaptionInStatus);
			showCaptionCheckBox.addActionListener(this);
			add(showCaptionCheckBox);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == showCaptionCheckBox) {
				showCaptionInStatus = showCaptionCheckBox.isSelected();
				controller.actionPerformed(e);
			}
		}
	}

	private class MyMouseAdapter extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				new MyPopup().show(e.getComponent(), e.getX(), e.getY());
			}
		}

		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				new MyPopup().show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}
}
