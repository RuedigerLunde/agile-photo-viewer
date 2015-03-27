/*
 * Copyright (C) 2013 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileWriter;

import javax.swing.JLabel;
import javax.swing.JTextArea;

import rl.util.exceptions.ErrorHandler;
import rl.util.exceptions.SimpleErrorHandler;
import rl.util.persistence.PropertyManager;

/**
 * A label to display status information. A special error handler implementation
 * is provided which automatically adds error counter information to the status
 * text.
 * 
 * @author Ruediger Lunde
 * 
 */
public class StatusArea extends JTextArea {
	private static final long serialVersionUID = 1L;
	int warningCount;
	int errorCount;
	int fatalErrorCount;

	public StatusArea() {
		setBackground(Color.DARK_GRAY);
		setForeground(Color.LIGHT_GRAY);
		setFont(new JLabel().getFont());
		setLineWrap(true);
		setWrapStyleWord(true);
		setEditable(false);
		setFocusable(false);
	}

	@Override
	public void setFont(Font f) {
		super.setFont(f);
		this.setMinimumSize(new Dimension(10, f.getSize() * 3));
	}



	/**
	 * Sets the new label text by concatenating the given text and error counter
	 * information.
	 */
	public void setStatus(String text) {
		String info = formatErrorInfo();
		setText(text + info);
	}

	/**
	 * Represents the values of the current error/warning counters as a string
	 * and resets the counters.
	 */
	protected String formatErrorInfo() {
		StringBuffer result = new StringBuffer();
		if (fatalErrorCount > 0)
			result.append(" " + fatalErrorCount + "fe");
		if (errorCount > 0)
			result.append(" " + errorCount + "e");
		if (warningCount > 0)
			result.append(" " + warningCount + "w");
		fatalErrorCount = 0;
		errorCount = 0;
		warningCount = 0;
		return result.toString();
	}

	/**
	 * Checks whether the status error handler (if used) has signaled problems
	 * which have not yet been displayed.
	 */
	public boolean hasUndisplayedErrors() {
		return fatalErrorCount > 0 || errorCount > 0 || warningCount > 0;
	}

	/**
	 * Creates an error handler, which updates error and warning counters and
	 * extends the <code>SimpleErrorHandler</code> by a log file output.
	 */
	public ErrorHandler createStatusErrorHandler() {
		return new SPErrorHandler();
	}

	public class SPErrorHandler extends SimpleErrorHandler {
		File logFile;

		@Override
		public void handleWarning(Throwable warning) {
			warningCount++;
			super.handleWarning(warning);

		}

		@Override
		public void handleError(Throwable e) {
			errorCount++;
			super.handleError(e);

		}

		@Override
		public void handleFatalError(Throwable e) {
			fatalErrorCount++;
			super.handleFatalError(e);
		}

		@Override
		protected void print(String text) {
			try {
				if (logFile == null) {
					logFile = PropertyManager.getInstance().getPropertyFile(
							"errors.log");
					// clear the file before writing the first error message
					if (logFile.exists()) 
						logFile.delete();
				}
				FileWriter fw = new FileWriter(logFile, true);
				fw.append(text);
				fw.close();
			} catch (Exception e) {
				// We ignore errors during error handling ...
			}
			super.print(text);
		}
	}
}
