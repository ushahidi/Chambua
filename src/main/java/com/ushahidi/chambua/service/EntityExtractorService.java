package com.ushahidi.chambua.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ushahidi.chambua.data.DocumentData;
import com.ushahidi.chambua.data.DocumentData.Place;
import com.ushahidi.chambua.web.dto.APIResponseDTO;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

/**
 * This class performs entity extraction using the Stanford
 * NLP tools.
 * 
 * @author ekala
 *
 */
@Service
public class EntityExtractorService {

	private String gisgraphyUrl;

	private String gisgraphyUrlParameters;

	private CRFClassifier<CoreLabel> classifier;

	private ArticleExtractor articleExtractor;
	
	private Mapper beanMapper;
	
	private ObjectMapper objectMapper;
	
	final static Logger LOGGER = LoggerFactory.getLogger(EntityExtractorService.class);

	/**
	 * Sets the base url for the Gisgraphy REST API
	 * 
	 * @param gisgraphyUrl
	 */
	public void setGisgraphyUrl(String gisgraphyUrl) {
		this.gisgraphyUrl = gisgraphyUrl;
	}

	/**
	 * Sets the request parameters used to format the output of the Gisgraphy
	 * geocoding request
	 * 
	 * @param gisgraphyUrlParameters
	 */
	public void setGisgraphyUrlParameters(String gisgraphyUrlParameters) {
		this.gisgraphyUrlParameters = gisgraphyUrlParameters;
	}

	/**
	 * Creates a {@link CRFClassifier} object using from the serialized
	 * classifier specified in <code>classifierPath</code>
	 *  
	 * @param classifierFilePath The absolute file path of the serialized classifier
	 */
	public void setClassifierFilePath(String classifierFilePath) {
		this.classifier = CRFClassifier.getClassifierNoExceptions(classifierFilePath);
	}
	
	public void setArticleExtractor(ArticleExtractor articleExrtactor) {
		this.articleExtractor = articleExrtactor;
	}
	
	@Autowired
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	
	@Autowired
	public void setBeanMapper(Mapper beanMapper) {
		this.beanMapper = beanMapper;
	}

	/**
	 * Extracts named entities from the provided text. The first
	 * step is to determine the content type of the text. 
	 * 
	 * @param text
	 * @return com.ushahidi.swiftriver.tagger.dto.APIResponseDTO
	 */
	public APIResponseDTO getEntities(String text) {
		String cleanedContent = null;
		try {
			cleanedContent = articleExtractor.getText(text);
		} catch (BoilerpipeProcessingException e) {
			LOGGER.error("An error occurred while cleaning the input: {}", e.getMessage());
		}
		String labeledText = classifier.classifyWithInlineXML(cleanedContent);
		
		LOGGER.debug("Labeled text: {}", labeledText);
		
		// Entity types/classes available in the classifier e.g. PERSON, LOCATION, ORGANIZATION
		Set<String> tags = classifier.labels();
		String background = classifier.backgroundSymbol();
		
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
		Map<String, Set<String>> entityMap = new HashMap<String, Set<String>>();
		
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
		
		DocumentData apiResponse = new DocumentData();
		if (entityMap.containsKey("person")) {
			apiResponse.setPeople(new ArrayList<String>(entityMap.get("person")));
		}
		
		if (entityMap.containsKey("organization")) {
			apiResponse.setOrganizations(new ArrayList<String>(entityMap.get("organization")));
		}

		// Geocode the location entities via the Gisgraphy REST API
		if (entityMap.containsKey("location")) {
			List<Place> places;
			try {
				places = geocodeLocationNames(entityMap.get("location"));
				apiResponse.setPlaces(places);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return beanMapper.map(apiResponse, APIResponseDTO.class);
	}

	/**
	 * Geocodes each of the location names in the <code>java.util.Set</code>
	 * contained in <code>locationNames</code>
	 *  
	 * @param locationNames
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private List<Place> geocodeLocationNames(Set<String> locationNames) 
			throws MalformedURLException, IOException {

		LOGGER.debug("Preparing to geocode {} possible location name(s)", locationNames.size());

		List<Place> locations = new ArrayList<DocumentData.Place>();
		for (String locationName: locationNames) {
			LOGGER.debug("Geocoding '{}'", locationName);

			// Build the request URL
			String requestUrl = gisgraphyUrl + String.format(
					gisgraphyUrlParameters, URLEncoder.encode(locationName, "UTF-8"));
			
			URLConnection connection = new URL(requestUrl).openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			// Read the contents of the buffer into a string buffer
			StringBuffer responseBuffer = new StringBuffer();
			String line = null;
			while ((line = reader.readLine()) != null) {
				responseBuffer.append(line);
			}
			
			// Deserialize the JSON response
			Map<String, Object> apiResponseJSON = objectMapper.readValue(
					responseBuffer.toString(), new TypeReference<Map<String, Object>>() {});
			
			// Any results found?
			Map<String, Object> responseData = (Map<String, Object>) apiResponseJSON.get("response");
			if (((Integer) responseData.get("numFound")) > 0) {
				List<Map<String, Object>> geolocated = (List<Map<String, Object>>) responseData.get("docs");
				for (Map<String, Object> entry: geolocated) {
					Place location = new Place();
					location.setName(locationName);
					location.setLatitude(((Number) entry.get("lat")).floatValue());
					location.setLongitude(((Number) entry.get("lng")).floatValue());
					location.setPlaceType((String) entry.get("placetype"));

					locations.add(location);
				}
			}
		}
		
		LOGGER.debug("Successfully geocoded {} location name(s)", locations.size());

		return locations;
	}
	

}
