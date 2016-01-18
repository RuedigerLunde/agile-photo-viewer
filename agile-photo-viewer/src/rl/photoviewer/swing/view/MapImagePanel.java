/*
 * Copyright (C) 2013-2016 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.swing.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.Set;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import rl.photoviewer.model.GeoRefPoint;
import rl.photoviewer.model.IndexedGeoPoint;
import rl.photoviewer.model.MapData;
import rl.photoviewer.model.PVModel;
import rl.photoviewer.model.PhotoMetadata;
import rl.photoviewer.swing.controller.Controller;

/**
 * Extended version of the <code>ImagePanel</code> for showing map images and
 * locating geotagged photos on the map.
 * 
 * @author Ruediger Lunde
 */
public class MapImagePanel extends ImagePanel {
	private static final long serialVersionUID = 1L;

	private Controller controller;
	private PVModel model;

	Set<? extends IndexedGeoPoint> allPhotoPositions;
	private PhotoMetadata photoData; // caution: may be null

	private boolean photoChanged;

	private boolean showAllPhotoPositions;

	public MapImagePanel(PVModel model, Controller controller) {
		this.model = model;
		this.controller = controller;
		addMouseListener(new MyMouseAdapter());
		addMouseListener(controller);
	}

	public boolean isShowAllPhotoPositions() {
		return showAllPhotoPositions;
	}

	public void setShowAllPhotoPositions(boolean b) {
		showAllPhotoPositions = b;
	}

	// photoData may be null
	public void update(PhotoMetadata photoData,
			Set<? extends IndexedGeoPoint> allPhotoPositions) {
		this.allPhotoPositions = allPhotoPositions;
		if (imageFile != model.getMapData().getFile()) {
			setImage(model.getMapData().getFile(), 0);
		}
		if (image != null && photoData != null) {
			this.photoData = photoData;
			photoChanged = true;
		} else {
			photoData = null;
		}
		repaint();
	}

	protected void adjustForPhoto() {
		if (model.getMapData().hasData() && photoData != null) {
			double[] mp = model.getMapData().latLonToImagePos(photoData.getLat(),
					photoData.getLon());
			Point2D mPos = imageToView(new Point2D.Double(mp[0], mp[1]));
			int moveX = 0;
			int moveY = 0;
			double size = getWidth() - border.left - border.right;
			double dist = mPos.getX() - border.left;
			if (dist < size / 3.0)
				moveX = round(size / 2.5 - dist); // move image to the right
			dist = border.left + size - mPos.getX();
			if (dist < size / 3.0)
				moveX = round(dist - size / 2.5); // move image to the left
			size = getHeight() - border.top - border.bottom;
			dist = mPos.getY() - border.top;
			if (dist < size / 5.0)
				moveY = round(size / 4.0 - dist); // move image downwards
			dist = border.top + size - mPos.getY();
			if (dist < size / 5.0)
				moveY = round(dist - size / 4.0); // move image upwards

			moveImageAndCheckBorders(moveX, moveY);
		}
	}

	@Override
	protected void adjust() {
		boolean unadjusted = isUnadjusted();
		if (unadjusted)
			adjustForScaleFactor1();
		if (unadjusted || photoChanged) {
			adjustForPhoto();
			photoChanged = false;
		}
	}

	public void paintImage(Graphics g) {
		super.paintImage(g);
		MapData mapData = model.getMapData();
		int viewW = getWidth() - border.left - border.right;
		int viewH = getHeight() - border.top - border.bottom;
		g.setClip(border.left, border.top, viewW, viewH);
		for (GeoRefPoint p : mapData.getRefPoints()) {
			drawRefPoint(g, p.getXImage(), p.getYImage(), getFont().getSize());
		}
		if (mapData.hasData()) {
			if (isShowAllPhotoPositions()) {
				for (IndexedGeoPoint pt : allPhotoPositions) {
					if (!Double.isNaN(pt.getLat())) {
						double[] p = mapData.latLonToImagePos(pt.getLat(),
								pt.getLon());
						drawPhotoPosition(g, p[0], p[1], getFont().getSize() / 2);
					}
				}
			}
			if (photoData != null && !Double.isNaN(photoData.getLat())) {
				double[] mp = mapData.latLonToImagePos(photoData.getLat(),
						photoData.getLon());
				drawMarker(g, mp[0], mp[1], getFont().getSize());
			}
		}
	}

	private void drawRefPoint(Graphics g, double xImage, double yImage, int size) {
		Point2D p = imageToView(new Point2D.Double(xImage, yImage));
		((Graphics2D) g).setStroke(new BasicStroke(3));
		int x = round(p.getX() - size / 2.0);
		int y = round(p.getY() - size / 2.0);
		for (int i = 0; i < 2; i++) {
			g.setColor(i == 0 ? Color.BLACK : Color.LIGHT_GRAY);
			((Graphics2D) g).setStroke(i == 0 ? new BasicStroke(3)
					: new BasicStroke(2));
			g.drawLine(x, round(p.getY()), x + size, round(p.getY()));
			g.drawLine(round(p.getX()), y, round(p.getX()), y + size);
		}
	}

	private void drawPhotoPosition(Graphics g, double xImage, double yImage,
			int size) {
		Point2D p = imageToView(new Point2D.Double(xImage, yImage));
		((Graphics2D) g).setStroke(new BasicStroke(1));
		g.setColor(Color.DARK_GRAY);
		g.fillRect(round(p.getX() - size / 2.0) + 1, round(p.getY() - size
				/ 2.0) + 1, size, size);
		g.setColor(Color.WHITE);
		g.fillRect(round(p.getX() - size / 2.0) - 1, round(p.getY() - size
				/ 2.0) - 1, size, size);
	}

	private void drawMarker(Graphics g, double xImage, double yImage, int size) {
		Point2D mPos = imageToView(new Point2D.Double(xImage, yImage));
		int x = round(mPos.getX() - size / 2.0);
		int y = round(mPos.getY() - size / 2.0);
		for (int i = 0; i < 2; i++) {
			g.setColor(i == 0 ? Color.DARK_GRAY : Color.RED);
			((Graphics2D) g).setStroke(new BasicStroke(size / 4));
			g.drawOval(x, y, size, size);
			x -= 1;
			y -= 1;
		}
	}

	public double viewToImage(double distance) {
		return distance / scaleFactor;
	}

	private class MyPopup extends JPopupMenu implements ActionListener {
		private static final long serialVersionUID = 1L;
		int mouseX;
		int mouseY;
		GeoRefPoint nextRefPoint;
		JMenuItem refPointItem;
		JCheckBoxMenuItem allPositionsCheckBox;

		private MyPopup() {
			refPointItem = new JMenuItem();
			refPointItem.addActionListener(this);
			add(refPointItem);
			allPositionsCheckBox = new JCheckBoxMenuItem(
					"Show Visible Photo Positions");
			allPositionsCheckBox.addActionListener(this);
			add(allPositionsCheckBox);
			addSeparator();
			JMenuItem item;
			for (File file : model.getMapData().getAllMapFiles()) {
				item = new JMenuItem(file.getName());
				item.setActionCommand("LoadMap " + file.getAbsolutePath());
				item.addActionListener(controller);
				add(item);
			}
		}

		@Override
		public void show(Component invoker, int x, int y) {
			mouseX = x;
			mouseY = y;
			Point2D posIm = viewToImage(new Point2D.Double(x, y));
			nextRefPoint = model.getMapData().findRefPointAt(posIm.getX(), posIm.getY(), viewToImage(20));
			refPointItem.setText((nextRefPoint == null ? "Set" : "Remove")
					+ " Reference Point Here");
			allPositionsCheckBox.setSelected(isShowAllPhotoPositions());
			super.show(invoker, x, y);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == refPointItem) {
				if (nextRefPoint != null) {
					model.removeMapRefPoint(nextRefPoint);
					photoChanged = true;
				} else {
					Point2D posIm = viewToImage(new Point2D.Double(mouseX,
							mouseY));
					model.addMapRefPoint(new GeoRefPoint(posIm.getX(), posIm
							.getY(), photoData.getLat(), photoData.getLon()));
				}
			} else if (e.getSource() == allPositionsCheckBox) {
				setShowAllPhotoPositions(allPositionsCheckBox.isSelected());
				MapImagePanel.this.repaint();
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
