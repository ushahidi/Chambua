/**
 * SwiftRiver Tagger
 * 
 * Copyright 2012 Ushahidi Inc
 * Ushahidi Dev Team <team@ushahidi.com>
 */
package com.ushahidi.swiftriver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;

/**
 * This servlet initializes logging and loads up the classifier to be used
 *  for semantic extraction
 *  
 * @author Ushahidi Dev Team <team@ushahidi.com>
 * @version 0.1
 */
public class TaggingInit extends HttpServlet {
//> CONSTANTS
	private static final long serialVersionUID = 1991350358696163842L;
	
	/** Log configuration file */
	private static final String LOG_CONFIG_FILE = "log4j.properties";
	
	/** Application properties file */
	private static final String APP_PROPERTIES_FILE = "tagger.properties";
	
	/** Context parameter for the resources directory name */
	private static final String RESOURCE_DIR_PARAM = "resourcesDir";
	
	/** Property with the name of the default CRF classifier */
	private static final String PROP_CLASSIFIER_DEFAULT = "tagger.classifier.default";
	
	/** Property with the URL for the geocoder */
	private static final String PROP_GEOCODER_URL = "tagger.url.geocoder";

	/** Property with the URL parameters for the geocoder */
	private static final String PROP_GEOCODER_URL_PARAMS = "tagger.url.geocoder.params";
	
	/** Reference for the classifier */
	@SuppressWarnings("rawtypes")
	public static AbstractSequenceClassifier classifier = null;
	
	/** Geocoder URL */
	private static String geocoderURL = "";

	/** Parameters to be passed along with {@link TaggingInit#geocoderURL} */
	private static String geocoderParameters;
	
	public void init(ServletConfig config) throws ServletException {
		// Load the configuration properties
		ServletContext context = config.getServletContext();
		
		// Get the resources directory
		String resourcesDir = context.getInitParameter(RESOURCE_DIR_PARAM);
		
		// Build out the file name
		String log4jFile = context.getRealPath("/") + resourcesDir + LOG_CONFIG_FILE;
		
		File f = new File(log4jFile);
		if (f.exists()) {
			PropertyConfigurator.configure(log4jFile);
			initApplicationProperties(context, resourcesDir);
		} else {
			System.err.println(String.format("log4j file %s not found", log4jFile));
			BasicConfigurator.configure();
		}
				
		super.init(config);
		
	}
	
	/**
	 * Loads the application properties from the file specified in {@link TaggingInit#APP_PROPERTIES_FILE}
	 * @param context
	 * @param resourcesDir
	 */
	private void initApplicationProperties(ServletContext context, String resourcesDir) {
		// Properties file
		String propertiesFile = resourcesDir + APP_PROPERTIES_FILE;
		
		Properties applicationProperties = new Properties();
		try {
			InputStream stream = context.getResourceAsStream(propertiesFile);
			applicationProperties.load(stream);
			
			// Get the property name of the default classifier
			String defaultClassifierProperty = applicationProperties.getProperty(PROP_CLASSIFIER_DEFAULT);
			
			// Get the name of the classifier
			String classifierName = applicationProperties.getProperty(defaultClassifierProperty);
			
			// Properties for the Geocoder
			geocoderURL = applicationProperties.getProperty(PROP_GEOCODER_URL);
			geocoderParameters = applicationProperties.getProperty(PROP_GEOCODER_URL_PARAMS);
			
			try {
				// Load the classifer
				classifier = CRFClassifier.getClassifier(classifierName);
			} catch (Throwable t) {
				// Catch Throwable so that we also get an OutofMemoryError
				String message = String.format("Error loading CRF: %s\\n %s", 
						classifierName, t.getMessage());
				System.out.println(message);
			}
		} catch (IOException e) {
			// Log the exception
			e.printStackTrace();
		}
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
		throws IOException, ServletException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
		throws IOException, ServletException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}
	
	/**
	 * Gets the URL for the geocoding service
	 * @return
	 */
	public static String geocoderURL() {
		return geocoderURL;
	}
	
	/**
	 * Gets the parameters to be supplied to the base geocoder URL
	 * @return
	 */
	public static String gecoderURLParams() {
		return geocoderParameters;
	}
}
