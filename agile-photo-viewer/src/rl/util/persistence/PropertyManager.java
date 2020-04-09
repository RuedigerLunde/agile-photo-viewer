/*
 * Copyright (C) 2013 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.util.persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import rl.util.exceptions.ErrorHandler;
import rl.util.exceptions.PersistenceException;

/**
 * Singleton class for default value and session property management. An
 * application property consists of a unique name (a string) and a value
 * (internally represented as a string, too). This class provides methods, to
 * read application properties from XML files and write changed properties to
 * XML files. Additionally, convenient access methods are provided to convert
 * the strings into different data types.
 * 
 * @author Ruediger Lunde
 * @version 1.1
 * 
 */
public class PropertyManager {
	/** Contains the property manager in charge. */
	private static PropertyManager instance;
	/** Directory, in which the property manager files are located. */
	private static File applicationDataDirectory;
	/**
	 * Name of the file which contains static default values for application
	 * properties.
	 */
	private static final String STATIC_PROP_FILE_NAME = "static-properties.xml";
	/**
	 * Name of the file which contains session properties. Property values
	 * included here overwrite the static defaults.
	 */
	private static final String SESSION_PROP_FILE_NAME = "session-properties.xml";
	/** Character, used to separate items in a list of strings. */
	private String LIST_DELIMITER = ",";

	/**
	 * Sets the directory, in which the property manager maintains the property
	 * files. Calling this method after instantiating the property manager has
	 * no effect.
	 */
	public static void setApplicationDataDirectory(File dir) {
		if (instance == null)
			applicationDataDirectory = dir;
	}

	/** Returns the directory, in which the property manager files are located. */
	public static File getApplicationDataDirectory() {
		return applicationDataDirectory;
	}

	/**
	 * Returns the property manager in charge. The implementation uses lazy
	 * instantiation.
	 */
	public static PropertyManager getInstance() {
		if (instance == null)
			instance = new PropertyManager();
		return instance;
	}

	/** Hash table for default values. */
	protected Hashtable<String, String> staticProperties;
	/** Hash table for session properties. */
	protected Hashtable<String, String> sessionProperties;

	/**
	 * Standard constructor. It reads application properties from the two files
	 * {@link #STATIC_PROP_FILE_NAME} and {@link #SESSION_PROP_FILE_NAME}.
	 */
	private PropertyManager() {
		staticProperties = new Hashtable<>();
		sessionProperties = new Hashtable<>();
		try {
			File file = getPropertyFile(STATIC_PROP_FILE_NAME);
			if (file.exists())
				loadProperties(file, staticProperties);
			file = getPropertyFile(SESSION_PROP_FILE_NAME);
			if (file.exists())
				loadProperties(file, sessionProperties);
		} catch (PersistenceException e) {
			ErrorHandler.getInstance().handleWarning(e);
		}
	}

	/** Checks, whether properties are maintained by the property manager. */
	public boolean hasProperties() {
		return !staticProperties.isEmpty() || !sessionProperties.isEmpty();
	}

	/** Checks, whether a property with the specified name is known. */
	public boolean hasValue(String property) {
		return sessionProperties.containsKey(property)
				|| staticProperties.containsKey(property);
	}

	/**
	 * Basic method for accessing the value of a property. If the property is
	 * not found, a warning is signaled to the error handler. All other value
	 * access methods are based on this implementation.
	 * 
	 * @return Value null, if the property does not exist.
	 */
	public String getValue(String property) {
		String result = sessionProperties.get(property);
		if (result == null)
			result = staticProperties.get(property);
		if (result == null)
			ErrorHandler.getInstance().handleWarning(
					new PersistenceException("Property " + property
							+ " not found.", null));
		return result;
	}

	/**
	 * Safe method for accessing string properties.
	 * 
	 * @param ifUnknown
	 *            Specifies a default to be returned if the property is not
	 *            known.
	 */
	public String getStringValue(String property, String ifUnknown) {
		String value = getValue(property);
		return (value != null) ? value : ifUnknown;
	}

	/**
	 * Safe method for accessing boolean properties.
	 * 
	 * @param ifUnknown
	 *            Specifies a default to be returned if the property is not
	 *            known.
	 */
	public boolean getBooleanValue(String property, boolean ifUnknown) {
		String value = getValue(property);
		boolean result = ifUnknown;
		if (value != null)
			result = Boolean.parseBoolean(value);
		return result;
	}

	/**
	 * Safe method for accessing integer properties.
	 * 
	 * @param ifUnknown
	 *            Specifies a default to be returned if the property is not
	 *            known.
	 */
	public int getIntValue(String property, int ifUnknown) {
		String value = getValue(property);
		int result = ifUnknown;
		if (value != null)
			result = Integer.parseInt(value);
		return result;
	}

	/**
	 * Safe method for accessing double properties.
	 * 
	 * @param ifUnknown
	 *            Specifies a default to be returned if the property is not
	 *            known.
	 */
	public double getDoubleValue(String property, double ifUnknown) {
		String value = getValue(property);
		double result = ifUnknown;
		if (value != null)
			result = Double.parseDouble(value);
		return result;
	}

	/**
	 * Safe method for accessing file properties.
	 * 
	 * @param ifUnknown
	 *            Specifies a default to be returned if the property is not
	 *            known.
	 */
	public File getFileValue(String property, File ifUnknown) {
		String value = getValue(property);
		File result = ifUnknown;
		if (value != null)
			result = new File(value);
		return result;
	}

	/**
	 * Safe method for accessing string list properties.
	 * 
	 * @param ifUnknown
	 *            Specifies a default to be returned if the property is not
	 *            known.
	 */
	public List<String> getListValue(String property, List<String> ifUnknown) {
		String value = getValue(property);
		List<String> result = ifUnknown;
		if (value != null) {
			StringTokenizer tokenizer = new StringTokenizer(value,
					LIST_DELIMITER);
			List<String> newValue = new ArrayList<>();
			while (tokenizer.hasMoreElements())
				newValue.add(tokenizer.nextToken());
			result = newValue;
		}
		return result;
	}

	/**
	 * Converts the specified value to a string and adds a new name-value
	 * mapping to the session property hash table. The behavior of this template
	 * method can be customized by overriding the primitive operation
	 * {@link #convertToString(Object)}.
	 */
	public void setValue(String property, Object value) {
		String strValue = convertToString(value);
		if (!hasValue(property) || !getValue(property).equals(strValue)) {
			sessionProperties.put(property, strValue);
		}
	}

	/** Primitive operation which converts an object to a string. */
	protected String convertToString(Object obj) {
		String result = "";
		if (obj instanceof List) {
			for (Object item : (List<?>) obj) {
				if (!result.isEmpty())
					result += LIST_DELIMITER;
				result += convertToString(item);
			}
		} else {
			result = obj.toString();
		}
		return result;
	}

	/**
	 * Searches in the application data directory and also in the current
	 * directory for the specified file name.
	 */
	public File getPropertyFile(String fileName) {
		File result;
		if (applicationDataDirectory != null
				&& applicationDataDirectory.exists()) {
			result = new File(applicationDataDirectory, fileName);
			if (!result.exists()) {
				File file = new File(fileName);
				if (file.exists())
					result = file;
			}
		} else {
			result = new File(fileName);
		}
		return result;
	}

	/**
	 * Reads application properties from the specified file and adds
	 * corresponding name-value mappings to the specified hash table.
	 */
	protected void loadProperties(File propertyFile,
			Hashtable<String, String> hash) throws PersistenceException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			// factory.setValidating(true);
			// factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new MyErrorHandler());
			Document doc = builder.parse(propertyFile);
			NodeList nodes = doc.getElementsByTagName("property");
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				Node vNode = node.getFirstChild();
				String propName = node.getAttributes().getNamedItem("name")
						.getNodeValue();
				String propValue = "";
				if (vNode != null)
					propValue = ((Text) vNode).getData();
				hash.put(propName, propValue);
			}
		} catch (Exception e) {
			throw new PersistenceException("Loading property file " + propertyFile + " failed.", e);
		}
	}

	/**
	 * Saves properties about the dynamic state of the application. This method
	 * should be called before closing the application.
	 */
	public void saveSessionProperties() throws PersistenceException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			// factory.setValidating(true);
			// factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Element root = doc.createElement("application-properties");
			doc.appendChild(root);
			List<String> keys = new ArrayList<>(sessionProperties.keySet());
			Collections.sort(keys);
			for (String propName : keys) {
				String propValue = sessionProperties.get(propName);
				Element propNode = doc.createElement("property");
				propNode.setAttribute("name", propName);
				propNode.setTextContent(propValue);
				root.appendChild(propNode);
			}
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			// transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"exercise-set.dtd");
			DOMSource source = new DOMSource(doc);
			StreamResult resultStream = new StreamResult(
					getPropertyFile(SESSION_PROP_FILE_NAME));
			transformer.transform(source, resultStream);
		} catch (Exception e) {
			throw new PersistenceException("Saving session properties to file "
							+ getPropertyFile(SESSION_PROP_FILE_NAME)
							+ " failed.", e);
		}
	}

	/**
	 * Simple handler for XML parser exceptions.
	 * 
	 * @author Ruediger Lunde
	 */
	private static class MyErrorHandler implements org.xml.sax.ErrorHandler {
		public void warning(SAXParseException ex) {
			ErrorHandler.getInstance().handleWarning(ex);
		}

		public void error(SAXParseException ex) throws SAXException {
			throw ex;
		}

		public void fatalError(SAXParseException ex) throws SAXException {
			throw ex;
		}
	}
}
