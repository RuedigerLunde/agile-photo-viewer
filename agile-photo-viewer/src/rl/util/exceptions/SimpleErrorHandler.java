/*
 * Copyright (C) 2013 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.util.exceptions;

import org.xml.sax.SAXParseException;

/**
 * Simple text-based error handler which writes messages to the standard
 * error stream.
 * @author Ruediger Lunde
 */
public class SimpleErrorHandler extends ErrorHandler {
	/**
	 * Communicates warning messages to the user by calling
	 * method {@link #printMessageAndCause(String, Throwable, int)}
	 * and additionally provides a stack trace if <code>debugMode</code>
	 * is enabled.
	 */ 
	@Override
	public void handleWarning(Throwable t) {
		String type = "Warning";
		printMessageAndCause(type, t, 0);
		if (t.getMessage() == null || debugMode) {
			System.err.print(type + ": ");
			t.printStackTrace();
		}
	}
	/**
	 * Communicates error messages to the user by calling
	 * method {@link #printMessageAndCause(String, Throwable, int)}
	 * and additionally provides a stack trace if <code>debugMode</code>
	 * is enabled.
	 */ 
	@Override
	public void handleError(Throwable t) {
		String type = "Error";
		printMessageAndCause(type, t, 0);
		if (t.getMessage() == null || debugMode) {
			System.err.print(type + ": ");
			t.printStackTrace();
		}
	}
	/**
	 * Communicates fatal error messages to the user by calling
	 * method {@link #printMessageAndCause(String, Throwable, int)},
	 * provides a stack trace if <code>debugMode</code> is enabled,
	 * and exits the application.
	 */ 
	@Override
	public void handleFatalError(Throwable t) {
		String type = "Fatal Error";
		printMessageAndCause(type, t, 0);
		if (t.getMessage() == null || debugMode) {
			System.err.print(type + ": ");
			t.printStackTrace();
		}
		try {
			Thread.sleep(5000);
		} catch (Exception e) {}
		System.exit(1);
	}
	
	/**
	 * Template method, which prints a message text for a given exception and
	 * recursively messages explaining the cause of the exception. The concrete
	 * behavior can be modified by overriding the primitive operations
	 * {@link #getExtendedMessage(Throwable)} and {@link #print(String)}.
	 */
	protected void printMessageAndCause(String type, Throwable t, int indent) {
		StringBuilder text = new StringBuilder();
		text.append("  ".repeat(Math.max(0, indent)));
		if (type != null)
			text.append(type).append(": ");
		if (t.getMessage() != null)
			text.append(getExtendedMessage(t));
		text.append("\n");
		print(text.toString());
		if (t.getCause() != null) {
			printMessageAndCause
			(t.getCause().getClass().getSimpleName(), t.getCause(), indent+1);
		}
	}
	
	/**
	 * Primitive operation, responsible for integrating exception class specific
	 * information into the message string. This implementation adds column
	 * and row information to <code>SAXParseException</code> messages.
	 */
	protected String getExtendedMessage(Throwable t) {
		String result = t.getMessage();
		if (t instanceof SAXParseException)
			result += " (line " + ((SAXParseException) t).getLineNumber()
			+ ", column " + ((SAXParseException) t).getColumnNumber() + ")";
		return result;
	}
	
	/**
	 * Primitive operation, responsible for printing messages. This
	 * implementation simply uses the standard error stream.
	 */
	protected void print(String text) {
		System.err.print(text);
	}
}
