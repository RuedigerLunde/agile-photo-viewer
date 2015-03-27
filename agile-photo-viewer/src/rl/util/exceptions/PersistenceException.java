/*
 * Copyright (C) 2013 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.util.exceptions;

/**
 * Exception class, covering all kinds of exceptions which
 * have to do with persistence.
 * @author Ruediger Lunde
 */
@SuppressWarnings("serial")
public class PersistenceException extends Exception {
	
	public PersistenceException(String message) {
		super(message);
	}
	
	public PersistenceException(String message, Throwable cause) {
		super(message, cause);
	}
}
