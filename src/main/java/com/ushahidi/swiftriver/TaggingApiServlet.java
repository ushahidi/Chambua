/**
 * SwiftRiver Tagger
 * 
 * Copyright (c) 2012
 * Ushahidi Inc <http://www.ushahidi.com> 
 */

package com.ushahidi.swiftriver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.*;

/**
 * This servlet exposes an endpoint that is used for extracting
 * semantics from text submitted via HTTP POST. All HTTP GET requests
 * sent to this servlet return a HTTP 405 status
 * 
 * @author Ushahidi Dev Team <team@ushahidi.com>
 * @version 0.1
 */
public class TaggingApiServlet extends HttpServlet {
       
	private static final long serialVersionUID = 7470192083383102849L;
	
	/** Logger */
	private static final Logger LOG = Logger.getLogger(TaggingApiServlet.class);
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}
	
	/**
	 * Return 405 response for HTTP PUT requests
	 */
	public void doPut(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}
	
	/**
	 * Return 405 response for HTTP DELETE requests
	 */
	public void doDelete(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, 
		IOException {
		
		// Get the submitted text
		String txtContent = request.getParameter("text");
		if (txtContent != null) {
			try {
				// Fetch the entities
				JSONObject entities = getEntities(txtContent);
				
				// Status to be returned by the API
				String statusStr = entities.length() > 0 ? "OK" : "NO_DATA";
				
				JSONObject apiResult = new JSONObject();
				apiResult.put("status", statusStr);
				apiResult.put("results", entities);
				
				// Set the MIME type
				response.setContentType("application/json; charset=utf-8");
				
				// Write to output stream
				PrintWriter writer = response.getWriter();
				writer.println(apiResult.toString());
				
				// Cleanup
				writer.flush();
				writer.close();
				
			} catch (JSONException ex) {
				LOG.error("Error extracting semantics", ex);
			}
			
		} else {
			// Return an error - Bad request
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		
	}
	
	/**
	 * Extracts the entities/tags from the provided text and returns a JSON
	 * representation in the format:
	 *  {
	 *  	labelA: [tag1, tag2,...tagN],
	 *  	labelB: [tag1, tag2,...tagN]
	 *  }
	 *  
	 * @param text Text to undergo entity extraction
	 * @return
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	private JSONObject getEntities(String text) throws IOException, JSONException {
		
		if (TaggingInit.classifier == null) {
			LOG.error("The CRF classifier has not been initialised");
			return null;
		}
		
		// Log the received text - Assists in identifying encoding errors 
		LOG.info(String.format("Extracting entites from %s\n\n", text));
		
		// Run classification 
		String labeledText = TaggingInit.classifier.classifyWithInlineXML(text.trim());
		
		// Entity types/classes available in the classifier e.g. PERSON, LOCATION, ORGANIZATION
		Set<String> tags = TaggingInit.classifier.labels();
		String background = TaggingInit.classifier.backgroundSymbol();
		
		// Build out the regex string
		String tagPattern = "";
		for (String tag: tags) {
			if (background.equals(tag))
				continue;
			
			if (tagPattern.length() > 0) {
				tagPattern += "|";
			}
			tagPattern += tag;
		}
		
		// Patterns for extracting the labeled text
		Pattern startPattern = Pattern.compile("<("+tagPattern+")>");
		Pattern endPattern = Pattern.compile("</("+tagPattern+")>");
		
		// Map to store the extracted entities/tags
		HashMap<String, Set<String>> entityMap = new HashMap<String, Set<String>>();
		
		// Begin extraction
		Matcher m = startPattern.matcher(labeledText);
		while (m.find()) {
			int start = m.start();
			labeledText = m.replaceFirst("");
			m = endPattern.matcher(labeledText);
			if (m.find()) {
				int end = m.start();
				String tag = m.group(1).toLowerCase();
				labeledText = m.replaceFirst("");
				String entity = labeledText.substring(start, end);
				
				if (entityMap.containsKey(tag)) {
					Set<String> current = entityMap.get(tag);
					current.add(entity);
					entityMap.put(tag, current);
				} else {
					Set<String> entities = new HashSet<String>();
					entities.add(entity);
					entityMap.put(tag, entities);
				}
			}
			// Adjust the matcher
			m = startPattern.matcher(labeledText);
		}
		LOG.info("Entity extraction complete");
		
		// JSON object to store the output to be returned
		JSONObject jsonTags = new JSONObject();

		// Geocode the location entities
		Set<String> locations = entityMap.get("location");
		if (locations.size() > 0) {
			LOG.info("Geocoding location tags...");

			String baseURL = TaggingInit.geocoderURL();
			String urlParameters = TaggingInit.gecoderURLParams();
			JSONArray geocodedLocations = new JSONArray();
			for (String location : locations) {
				geocodeLocationTag(baseURL, urlParameters, location, geocodedLocations);
			}
			
			// Remove the location entry from the entity mapping
			entityMap.remove("location");
			
			LOG.info(String.format("Geocoded %d location items", locations.size()));
			jsonTags.put("location", geocodedLocations);
		} else {
			// No location entries found
			LOG.info("No location entries found");
		}
		
		// Build out the JSON
		for (String entityType: entityMap.keySet()) {
			jsonTags.put(entityType, entityMap.get(entityType));
		}
		
		return jsonTags;
	}
	
	/**
	 * Geocodes the location entity specified in @param location. If successful, the
	 * location is added to @param geocodedLocations together with its location data
	 * 
	 * Each entry made in geocodedLocations has the following format:
	 * 
	 * locationName: {
	 *     place_type: <placeType>,
	 *     place_name: <locationName>,
	 *     coordinates: {
	 *         latitude: <latitude>,
	 *         longitude: <longitude>    
	 *     }
	 * }
	 * 
	 * @param baseURL Base URL for the geocoder
	 * @param urlParams Parameters to be passed to the base URL
	 * @param location Name of the location to be geocoded
	 * @param geocodedLocations
	 * @throws IOException
	 * @throws JSONException
	 */
	private void geocodeLocationTag(String baseURL, String urlParams, String location, 
			JSONArray geocodedLocations) throws IOException, JSONException {
		
		// Encode the location parameters to UTF-8
		urlParams = String.format(urlParams, URLEncoder.encode(location, "UTF-8"));

		// Build the request URL
		String requestURL = baseURL + urlParams;
		LOG.info("Submitting geocoding request: " + requestURL);
		
		// Open a connection to the geocoder
		URL url = new URL(requestURL);
		URLConnection connection = url.openConnection();
		
		// Get the response
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String line = null;
		StringBuffer buffer = new StringBuffer();
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
		}
		
		JSONObject payload = new JSONObject(buffer.toString());
		
		
		if (payload.getJSONObject("response").getInt("numFound") > 0) {
			if (payload.getJSONObject("response").has("docs")) {
				
				// Get the geo data from the docs property
				JSONObject geoData = payload.getJSONObject("response")
					.getJSONArray("docs")
					.getJSONObject(0);
				
				// Places data
				JSONObject places = new JSONObject();
				places.put("place_name", location);
				places.put("place_type", geoData.get("placetype"));
				
				// Get the coordinates
				JSONObject coordinates = new JSONObject();
				coordinates.put("latitude", geoData.get("lat"));
				coordinates.put("longitude", geoData.get("lng"));
				places.put("coordinates", coordinates);
				
				geocodedLocations.put(places);
			}
		}
		
		// Cleanup
		reader.close();
	}
}
