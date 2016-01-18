/*
 * Copyright (C) 2013-2016 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.swing.controller;

import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import rl.photoviewer.model.IndexedGeoPoint;
import rl.photoviewer.model.KeywordExpression;
import rl.photoviewer.model.PVModel;
import rl.photoviewer.swing.view.Commands;
import rl.photoviewer.swing.view.HelpDialog;
import rl.photoviewer.swing.view.MapImagePanel;
import rl.photoviewer.swing.view.PVView;
import rl.photoviewer.swing.view.VisibilityPanel;
import rl.util.exceptions.ErrorHandler;

/**
 * Controller of the photo viewer. It translates view events into model method
 * calls. The slide show control thread is also included.
 * 
 * @author Ruediger Lunde
 * 
 */
public class PVController extends MouseAdapter implements Controller {

	private SlideShowThread slideShowThread;

	private PVView view;
	private PVModel model;

	public PVController(PVView view, PVModel model) {
		this.model = model;
		this.view = view;
		view.setController(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String statusMsg = "";
		try {
			if (e.getActionCommand() == Commands.SELECT_CMD) {
				File file = model.getSelectedPhoto();
				file = view.showInputFileChooser(file);
				if (file != null) {
					if (model.selectPhoto(file))
						statusMsg = model.getVisiblePhotoCount()
								+ " photo(s) found.";
					view.getVisibilityPanel().resetSelectedRating();
					view.getVisibilityPanel().clear();
				}
			} else if (e.getActionCommand() == Commands.FIRST_CMD) {
				model.selectFirstPhoto();
			} else if (e.getActionCommand() == Commands.PREV_CMD) {
				model.selectPrevPhoto();
			} else if (e.getActionCommand() == Commands.NEXT_CMD) {
				model.selectNextPhoto();
			} else if (e.getActionCommand() == Commands.SLIDE_SHOW_CMD) {
				if (view.getCtrlPanel().isSlideShowSelected()) {
					slideShowThread = new SlideShowThread();
					slideShowThread.start();
				} else {
					slideShowThread.exit();
				}
			} else if (e.getActionCommand() == Commands.SORT_BY_DATE_CMD) {
				model.setSortByDate(view.getCtrlPanel().isSortByDateSelected());
			} else if (e.getActionCommand() == Commands.FULL_SCREEN_CMD) {
				toggleFullScreen();
			} else if (e.getActionCommand() == Commands.DECORATION_CMD) {
				updateDecoration();
			} else if (e.getActionCommand() == Commands.HELP_CMD) {
				HelpDialog.showHelpDialog(view.getFrame());
			} else if (e.getActionCommand() == Commands.USE_PHOTO_AS_MAP_CMD) {
				model.setMap(model.getSelectedPhoto());
				view.selectMapTab();
			} else if (e.getActionCommand() == Commands.CLEAR_MAP_CMD) {
				model.clearCurrentMap();
			} else if (e.getActionCommand() == Commands.EXPORT_CMD) {
				int copied = exportPhotos();
				if (copied != -1)
					statusMsg = copied + " files copied.";
			} else if (e.getActionCommand() == Commands.DELETE_SELECTED_PHOTO_CMD) {
				deleteSelectedPhoto();
			} else if (e.getActionCommand() == Commands.RESTORE_SESSION_CMD) {
				view.restoreSession();
				if (view.getStatusPanel().hasUndisplayedErrors())
					statusMsg = model.getVisiblePhotoCount()
							+ " photo(s) found.";
			} else if (e.getActionCommand() == Commands.EXIT_CMD) {
				view.storeSession();
				System.exit(0);
			} else if (e.getActionCommand() == Commands.INC_FONT_SIZE_CMD) {
				Font font = view.getStatusPanel().getFont();
				Font nf = font.deriveFont(font.getSize() + 2.0f);
				view.setInfoFont(nf);
			} else if (e.getActionCommand() == Commands.DEC_FONT_SIZE_CMD) {
				Font font = view.getStatusPanel().getFont();
				Font nf = font
						.deriveFont(Math.max(font.getSize() - 2.0f, 4.0f));
				view.setInfoFont(nf);
			} else if (e.getActionCommand() == Commands.SET_RATING_FILTER) {
				model.setVisibility(view.getVisibilityPanel().getSelectedRating(), model
						.getVisibilityExpression());
			} else if (e.getActionCommand() == Commands.KEYWORDS_CHANGED_SELECTION_CMD) {
				VisibilityPanel vPanel = view.getVisibilityPanel();
				List<String> keywords = vPanel.getSelectedKeywords();
				KeywordExpression expression = model
						.getVisibilityExpression();
				for (String keyword : keywords)
					expression.addLiteral(keyword, vPanel.isNegationSelected());
				view.getVisibilityPanel().setNegationSelected(false);
				model.setVisibility(vPanel.getSelectedRating(), expression);
			} else if (e.getActionCommand() == Commands.KEYWORDS_ADD_CLAUSE_CMD) {
				model.getVisibilityExpression().addClause();
				view.getVisibilityPanel().clear();
			} else if (e.getActionCommand() == Commands.KEYWORDS_DELETE_CMD) {
				model.getVisibilityExpression().deleteLastClause();
				view.getVisibilityPanel().clear();
			} else if (e.getActionCommand().startsWith("LoadMap ")) {
				File file = new File(e.getActionCommand().substring(8));
				model.setMap(file);
			}
		} catch (Exception ex) {
			ErrorHandler.getInstance().handleError(ex);
			statusMsg = "Unforeseen error occured, sorry.";
			ex.printStackTrace();
		}
		updateStatus(statusMsg);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == view.getPhotoPanel()) {
			if (e.getButton() == MouseEvent.BUTTON3)
				model.selectPrevPhoto();
			else if (e.getButton() == MouseEvent.BUTTON1)
				model.selectNextPhoto();
		} else if (e.getSource() == view.getMapImagePanel()
				&& e.getButton() == MouseEvent.BUTTON1
				&& e.getClickCount() == 1) {
			MapImagePanel mip = view.getMapImagePanel();
			Point2D mouseImg = mip.viewToImage(new Point2D.Double(e.getX(), e.getY()));
			double radius = mip.viewToImage(20);
			double tolerance = mip.viewToImage(5);
			IndexedGeoPoint gpoint = model.getMapData().findPhotoPositionAt(model.getVisiblePhotoPositions(), 
					mouseImg.getX(), mouseImg.getY(), radius, tolerance);
			if (gpoint != null)
				model.selectPhotoByMetadata(gpoint);
		}
		updateStatus("");
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// System.out.println(e.getKeyCode());
		if (e.getKeyCode() == KeyEvent.VK_LEFT
				|| e.getKeyCode() == KeyEvent.VK_PAGE_UP)
			model.selectPrevPhoto();
		else if (e.getKeyCode() == KeyEvent.VK_RIGHT
				|| e.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
			model.selectNextPhoto();
		else if (e.getKeyCode() == KeyEvent.VK_F11
				|| (e.getKeyCode() == KeyEvent.VK_ESCAPE && view.getCtrlPanel()
						.isUndecorateSelected())) {
			toggleFullScreen();
		} else if (e.getKeyCode() == KeyEvent.VK_PLUS
				&& e.getModifiers() == InputEvent.CTRL_MASK) {
			Font font = view.getStatusPanel().getFont();
			Font nf = font.deriveFont(font.getSize() + 2.0f);
			view.setInfoFont(nf);
		} else if (e.getKeyCode() == KeyEvent.VK_MINUS
				&& e.getModifiers() == InputEvent.CTRL_MASK) {
			Font font = view.getStatusPanel().getFont();
			Font nf = font.deriveFont(Math.max(font.getSize() - 2.0f, 4.0f));
			view.setInfoFont(nf);
		}
		updateStatus("");
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	protected void updateStatus(String statusMsg) {
		if (statusMsg.isEmpty() && view.getInfoPanel().isShowCaptionInStatus()) {
			String caption = null;
			if (model.getSelectedPhotoData() != null)
				caption = model.getSelectedPhotoData().getCaption();
			statusMsg = (caption != null) ? caption : "";
		}
		view.getStatusPanel().setStatus(statusMsg);
	}

	protected void toggleFullScreen() {
		JFrame frame = view.getFrame();
		GraphicsEnvironment environment = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice device;
		// device = frame.getGraphicsConfiguration().getDevice();
		device = environment.getDefaultScreenDevice();
		if (device.isFullScreenSupported()) {
			boolean newState = !view.getCtrlPanel().isUndecorateSelected();
			view.getCtrlPanel().setUndecorateSelected(newState);
			frame.dispose();
			frame.setUndecorated(newState);
			if (newState) {
				view.getCtrlPanel().setDialogTriggerEnabled(false);
				device.setFullScreenWindow(frame);
			} else {
				device.setFullScreenWindow(null);
				view.getCtrlPanel().setDialogTriggerEnabled(true);
				frame.setVisible(true);
			}
		}
	}

	protected void updateDecoration() {
		boolean newState = view.getCtrlPanel().isUndecorateSelected();
		view.getCtrlPanel().setUndecorateSelected(newState);
		JFrame frame = view.getFrame();
		frame.dispose();
		frame.setUndecorated(newState);
		frame.setVisible(true);
	}

	protected int exportPhotos() {
		List<File> photos = model.getVisiblePhotos();
		File destination = view.showOutputFileChooser(photos.size());
		int copied = -1;
		if (destination != null) {
			File destDir = destination;
			String destFileNameTemplate = null;
			if (!destDir.isDirectory()) {
				destDir = destDir.getParentFile();
				destFileNameTemplate = destination.getName();
			}
			if (destDir.list().length == 0
					|| JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
							view.getFrame(), new String[] {
									"Destination directory is not empty:",
									destDir.getAbsolutePath(),
									"Continue anyway?" }, "Warning",
							JOptionPane.OK_CANCEL_OPTION)) {
				copied = model.exportPhotos(photos, destDir,
						destFileNameTemplate);
			}
		}
		return copied;
	}

	protected void deleteSelectedPhoto() {
		File file = model.getSelectedPhoto();
		String txt = "Delete the following file?";
		if (file != null
				&& JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
						view.getFrame(),
						new String[] { txt, file.getAbsolutePath() },
						"Warning", JOptionPane.OK_CANCEL_OPTION)) {
			model.deleteSelectedPhoto();
		}
	}

	class SlideShowThread extends Thread {
		boolean exit;

		public void exit() {
			exit = true;
		}

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(Math.max(1000 * view.getCtrlPanel()
							.getSlideShowSec(), 1));
					if (exit)
						break;
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							ActionEvent e = new ActionEvent(this, 0,
									Commands.NEXT_CMD);
							actionPerformed(e);
						}
					});
				} catch (Exception ex) {
					ErrorHandler.getInstance().handleWarning(ex);
				}
			}
		}
	}
}
