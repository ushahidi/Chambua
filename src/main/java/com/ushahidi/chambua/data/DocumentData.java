package com.ushahidi.chambua.data;

import java.util.List;

/**
 * This class is used to represent the named entities
 * that have been extracted for a given piece of text
 * 
 * @author ekala
 *
 */
public class DocumentData {

	private List<String> people;
	
	private List<String> organizations;
	
	private List<Place> places;
	
	private List<String> nationalities;
	
	private List<String> ideologies;
	
	private List<String> religions;
	
	private List<String> dates;
	
	private List<String> titles;
	
	public List<String> getPeople() {
		return people;
	}

	public void setPeople(List<String> people) {
		this.people = people;
	}

	public List<String> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(List<String> organizations) {
		this.organizations = organizations;
	}

	public List<Place> getPlaces() {
		return places;
	}

	public void setPlaces(List<Place> places) {
		this.places = places;
	}
	
	public List<String> getNationalities() {
		return nationalities;
	}

	public void setNationalities(List<String> nationalities) {
		this.nationalities = nationalities;
	}

	public List<String> getIdeologies() {
		return ideologies;
	}

	public void setIdeologies(List<String> ideologies) {
		this.ideologies = ideologies;
	}

	public List<String> getReligions() {
		return religions;
	}

	public void setReligions(List<String> religions) {
		this.religions = religions;
	}

	public List<String> getDates() {
		return dates;
	}

	public void setDates(List<String> dates) {
		this.dates = dates;
	}

	public List<String> getTitles() {
		return titles;
	}

	public void setTitles(List<String> titles) {
		this.titles = titles;
	}


	public static class Place {
		
		private String name;
		
		private String placeType;
		
		private Float latitude;
		
		private Float longitude;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPlaceType() {
			return placeType;
		}

		public void setPlaceType(String placeType) {
			this.placeType = placeType;
		}

		public Float getLatitude() {
			return latitude;
		}

		public void setLatitude(Float latitude) {
			this.latitude = latitude;
		}

		public Float getLongitude() {
			return longitude;
		}

		public void setLongitude(Float longitude) {
			this.longitude = longitude;
		}
		
		
	}
}
