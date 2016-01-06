/*
 * Copyright (C) 2013 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.controller.swing;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Controller, which delegates all calls to another controller if
 * available. It allows to replace the controller in charge
 * at runtime.
 * @author Ruediger Lunde
 */
public class ControllerProxy implements Controller {
	
	protected Controller controller;
	
	/**
	 * Sets the controller to which all calls are forwarded.
	 */
	public void setController(Controller controller) {
		this.controller = controller;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (controller != null)
			controller.actionPerformed(e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (controller != null)
			controller.mouseClicked(e);

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (controller != null)
			controller.mouseEntered(e);

	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (controller != null)
			controller.mouseExited(e);

	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (controller != null)
			controller.mousePressed(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (controller != null)
			controller.mouseReleased(e);
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if (controller != null)
			controller.keyPressed(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (controller != null)
			controller.keyReleased(e);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (controller != null)
			controller.keyTyped(e);
	}
}
