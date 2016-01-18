/*
 * Copyright (C) 2013-2016 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.swing.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import rl.util.exceptions.ErrorHandler;
import rl.util.exceptions.PersistenceException;

/**
 * Simple panel showing an image. The image is scaled so that it fills the
 * panel. To preserve aspect ratio, background colored bars are added if
 * necessary. The user can zoom into the picture using mouse wheel and mouse
 * drags. Supported mouse events: mouse-button1-drag, mouse-wheel,
 * mouse-wheel-shift, mouse-button2, mouse-button2-shift.
 * 
 * @author Ruediger Lunde
 * 
 */
public class ImagePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	protected File imageFile;
	protected Image image;
	private int imageRefPosX;
	private int imageRefPosY;
	/**
	 * Factor which transforms image pixel size to view pixel size (size_i *
	 * scaleFactor = size_v). Value NaN indicates that the image is not adjusted
	 * yet.
	 */
	protected double scaleFactor;
	private int viewWidth;
	private int viewHeight;
	protected Insets border;

	public ImagePanel() {
		setBackground(Color.DARK_GRAY);
		MouseAdapter ma = new MyMouseAdapter();
		addMouseListener(ma);
		addMouseMotionListener(ma);
		this.addMouseWheelListener(ma);
		setUnadjusted();
	}

	public void setImage(File imageFile, int orientation) {
		if (imageFile == null) {
			image = null;
			this.imageFile = null;
			repaint();
		} else if (!imageFile.equals(this.imageFile)) {
			Image save = this.image;
			try {
				image = ImageIO.read(imageFile);
				this.imageFile = imageFile;
				if (image != null
						&& (orientation == 6 || orientation == 8 || orientation == 3)) {
					int w = orientation == 3 ? image.getWidth(null) : image
							.getHeight(null);
					int h = orientation == 3 ? image.getHeight(null) : image
							.getWidth(null);
					this.image = new BufferedImage(w, h,
							BufferedImage.TYPE_3BYTE_BGR);
					AffineTransform trans = new AffineTransform();
					if (orientation == 6) {
						trans.translate(this.image.getWidth(null), 0);
						trans.rotate(Math.toRadians(90));
					} else if (orientation == 8) {
						trans.translate(0, this.image.getHeight(null));
						trans.rotate(Math.toRadians(-90));
					} else {
						trans.translate(this.image.getWidth(null),
								this.image.getHeight(null));
						trans.rotate(Math.toRadians(180));
					}
					((Graphics2D) this.image.getGraphics()).drawImage(image,
							trans, null);
					image.flush();
				}
			} catch (IOException ex) {
				Exception e = new PersistenceException(
						"Could not read image from file " + imageFile + ".", ex);
				ErrorHandler.getInstance().handleError(e);
			}
			if (save != null)
				save.flush();
			setUnadjusted();
			repaint();
		}
	}

	/**
	 * Calls super implementation (for borders, background etc), updates panel
	 * size values, adjusts the image, and prints it.
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (getWidth() != viewWidth || getHeight() != viewHeight) {
			viewWidth = getWidth();
			viewHeight = getHeight();
			border = getBorder() != null ? getBorder().getBorderInsets(this)
					: new Insets(0, 0, 0, 0);
			setUnadjusted();
		}
		adjust();
		paintImage(g);
	}

	/**
	 * Called during rendering. This implementation calls
	 * <code>adjustToFit()</code> if the image is unadjusted (due to image or
	 * panel size changes).
	 */
	protected void adjust() {
		if (isUnadjusted())
			adjustToFit();
	}

	protected void adjustToFit() {
		if (image != null && border != null) {
			int iWidth = image.getWidth(this);
			int iHeight = image.getHeight(this);
			int maxWidth = viewWidth - border.left - border.right;
			int maxHeight = viewHeight - border.top - border.bottom;
			scaleFactor = Math.min(1.0 * maxWidth / iWidth, 1.0 * maxHeight
					/ iHeight);
			imageRefPosX = (viewWidth - round(iWidth * scaleFactor)) / 2;
			imageRefPosY = (viewHeight - round(iHeight * scaleFactor)) / 2;
		}
	}

	protected void adjustForScaleFactor1() {
		if (image != null) {
			scaleFactor = 1.0f;
			int iWidth = image.getWidth(this);
			int iHeight = image.getHeight(this);
			imageRefPosX = (viewWidth - iWidth) / 2;
			imageRefPosY = (viewHeight - iHeight) / 2;
		}
	}

	protected void setUnadjusted() {
		scaleFactor = Double.NaN;
	}

	protected boolean isUnadjusted() {
		return Double.isNaN(scaleFactor);
	}

	protected void moveImage(int dx, int dy) {
		imageRefPosX += dx;
		imageRefPosY += dy;
	}

	protected void moveImageAndCheckBorders(int dx, int dy) {
		Point2D pos = imageToView(new Point2D.Double(image.getWidth(null),
				image.getHeight(null)));
		if (dx > 0)
			dx = Math.min(dx, border.left - imageRefPosX);
		else if (dx < 0)
			dx = Math.max(dx, viewWidth - border.right - round(pos.getX()));
		if (dy > 0)
			dy = Math.min(dy, border.top - imageRefPosY);
		else if (dy < 0)
			dy = Math.max(dy, viewHeight - border.bottom - round(pos.getY()));
		moveImage(dx, dy);
	}

	public Point2D viewToImage(Point2D pView) {
		double xImage = (pView.getX() - imageRefPosX) / scaleFactor;
		double yImage = (pView.getY() - imageRefPosY) / scaleFactor;
		return new Point2D.Double(xImage, yImage);
	}

	public Point2D imageToView(Point2D pImage) {
		double xView = imageRefPosX + pImage.getX() * scaleFactor;
		double yView = imageRefPosY + pImage.getY() * scaleFactor;
		return new Point2D.Double(xView, yView);
	}

	protected void paintImage(Graphics g) {
		int viewW = viewWidth - border.left - border.right;
		int viewH = viewHeight - border.top - border.bottom;
		((Graphics2D) g).setBackground(getBackground());
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
		        RenderingHints.VALUE_ANTIALIAS_ON);
		if (image != null) {
			int imageW = round(image.getWidth(this) * scaleFactor);
			int imageH = round(image.getHeight(this) * scaleFactor);
			g.setClip(border.left, border.top, viewW, viewH);
			g.drawImage(image, imageRefPosX, imageRefPosY, imageW, imageH, this);
			g.clearRect(border.left, border.top, imageRefPosX - border.left,
					viewH); // left
			g.clearRect(border.left, border.top, viewW, imageRefPosY
					- border.top); // top
			g.clearRect(imageRefPosX + imageW, border.top, viewW, viewH); // right
			g.clearRect(border.left, imageRefPosY + imageH, viewW, viewH); // bottom
		} else
			g.clearRect(border.left, border.top, viewW, viewH);
	}

	protected static int round(double d) {
		return (int) Math.round(d);
	}

	private class MyMouseAdapter extends MouseAdapter {
		int xp;
		int yp;

		@Override
		public void mousePressed(MouseEvent e) {
			xp = e.getX();
			yp = e.getY();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON2) {
				// if ((e.getModifiers() & MouseEvent.SHIFT_MASK) == 0)
				if (Math.abs(scaleFactor - 1.0) > 0.05)
					adjustForScaleFactor1();
				else
					adjustToFit();
				repaint();
			}
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int rot = e.getWheelRotation();
			int xm = e.getX();
			int ym = e.getY();
			float fac = ((e.getModifiers() & KeyEvent.SHIFT_MASK) != 0) ? 1.1f
					: 1.5f;
			if (rot == 1)
				fac = 1 / fac;
			scaleFactor *= fac;
			imageRefPosX = xm - Math.round(fac * (xm - imageRefPosX));
			imageRefPosY = ym - Math.round(fac * (ym - imageRefPosY));
			repaint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) > 0) {
				int xr = e.getX();
				int yr = e.getY();
				if (xr != xp || yr != yp) {
					moveImage(xr - xp, yr - yp);
					xp = xr;
					yp = yr;
					Graphics g = getGraphics();
					paintImage(g);
					g.dispose();
				}
			}
		}
	}
}
