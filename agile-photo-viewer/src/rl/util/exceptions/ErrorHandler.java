/*
 * Copyright (C) 2013 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.util.exceptions;

/**
 * Abstract class which defines a standard interface for communicating errors
 * and warnings to the user. The singleton pattern is used to give all parts
 * of the application the chance to inform the user about unexpected events.
 * <p>Call {@link #getInstance()} to access the currently used error handler.
 * Extend this class, implement the abstract interface methods, and provide
 * an instance of the extended class to {@link #setInstance(ErrorHandler)}
 * to replace the error handler in use.</p>
 * @author Ruediger Lunde
 */
public abstract class ErrorHandler {
	/** Maintains the error handler currently in use. */
	private static ErrorHandler usedErrorHandler;
	/**
	 * Flag, controlling the level of detail used for explaining problematic
	 * situations to the user.
	 */
	protected static boolean debugMode = false;
	
	/**
	 * Returns the error handler currently in use. By default, an instance of
	 * {@link SimpleErrorHandler} is used.
	 */
	public static ErrorHandler getInstance() {
		if (usedErrorHandler == null)
			usedErrorHandler = new SimpleErrorHandler();
		return usedErrorHandler;
	}
	/** Replaces the default error handler. */
	public static void setInstance(ErrorHandler errorHandler) {
		usedErrorHandler = errorHandler;
	}
	/** Enables the presentation of low level informations such as stack traces. */
	public static void enableDebugMode(boolean state) {
		debugMode = state;
	}
	
	
	/////////////////////////////////////////////////////////////////
	// abstract methods
	
	/**
	 * Interface method, responsible for communicating warnings
	 * to the user.
	 */
	public abstract void handleWarning(Throwable warning);
	/**
	 * Interface method, responsible for communicating recoverable errors
	 * to the user.
	 */
	public abstract void handleError(Throwable e);
	/**
	 * Interface method, responsible for communicating unrecoverable errors
	 * to the user and terminating the application.
	 */
	public abstract void handleFatalError(Throwable e);
}
