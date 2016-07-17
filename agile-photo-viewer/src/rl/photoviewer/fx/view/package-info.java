/*
 * Copyright (C) 2013-2016 Ruediger Lunde Licensed under the GNU General Public
 * License, Version 3
 */

/**
 * Contains classes to create the JavaFX-based version of the photo viewer's
 * view component. Class {@link AgilePhotoViewerApp} starts the
 * application. The architecture is inspired by the model - view - presenter pattern.
 * File <code>AgilePhotoViewer.fxml</code> defines the layout of
 * the view component. <code>AgilePhotoViewer.css</code> provides a dark theme
 * for the GUI. Class {@link AgilePhotoViewerCtrl} is the entry point
 * for event handling. It mediates between view and model. 
 * 
 * @author Ruediger Lunde
 */
package rl.photoviewer.fx.view;