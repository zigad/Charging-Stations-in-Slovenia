package si.deisinger.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.deisinger.business.controller.ApiController;
import si.deisinger.business.controller.FileController;
import si.deisinger.providers.enums.Providers;
import si.deisinger.providers.model.ampeco.AmpecoLocationPins;
import si.deisinger.providers.model.avant2go.Avant2GoLocations;
import si.deisinger.providers.model.efrend.EfrendDetailedLocation;
import si.deisinger.providers.model.efrend.EfrendLocationPins;
import si.deisinger.providers.model.gremonaelektriko.GNEDetailedLocation;
import si.deisinger.providers.model.gremonaelektriko.GNELocationPins;
import si.deisinger.providers.model.implera.ImpleraLocations;
import si.deisinger.providers.model.mooncharge.MoonChargeLocation;
import si.deisinger.providers.model.petrol.PetrolLocations;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ProviderProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(ProviderProcessor.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	public void checkGremoNaElektriko(Providers provider) {
		GNELocationPins gneLocationPins;
		try {
			gneLocationPins = OBJECT_MAPPER.readValue(ApiController.getLocationsFromApi(provider), GNELocationPins.class);
		} catch (JsonProcessingException e) {
			LOG.error("Mapping to POJO failed");
			throw new RuntimeException(e);
		}
		LOG.info("Fetched: " + gneLocationPins.pins.size() + " stations");

		Set<Integer> stationsAroundSlovenia = restrictToGeoLocation(gneLocationPins);

		Set<Integer> difference = checkDifference(provider, stationsAroundSlovenia);

		if (!difference.isEmpty()) {
			LOG.info("Found " + difference.size() + " new stations");
			GNEDetailedLocation gneDetailedLocation;
			try {
				gneDetailedLocation = OBJECT_MAPPER.readValue(ApiController.getAmpecoDetailedLocationsApi(buildPostRequestBody(difference), provider), GNEDetailedLocation.class);
			} catch (JsonProcessingException e) {
				LOG.error("Mapping to POJO failed");
				throw new RuntimeException(e);
			}
			LOG.info("Mapped " + gneDetailedLocation.locations.size() + " stations to POJO");
			FileController.writeNewDataToJsonFile(provider, gneLocationPins.pins.size(), difference);
			FileController.writeNewStationsToFile(provider, gneDetailedLocation);
		} else {
			LOG.info("No new stations found");
		}
	}

	public void checkPetrol(Providers provider) {
		PetrolLocations[] locations;
		try {
			locations = OBJECT_MAPPER.readValue(ApiController.getLocationsFromApi(provider), PetrolLocations[].class);
		} catch (JsonProcessingException e) {
			LOG.error("Mapping to POJO failed");
			throw new RuntimeException(e);
		}
		LOG.info("Fetched: " + locations.length + " stations");

		Set<Integer> stationsAroundSlovenia = new LinkedHashSet<>();
		for (PetrolLocations location : locations) {
			stationsAroundSlovenia.add(location.id);
		}

		Set<Integer> difference = checkDifference(provider, stationsAroundSlovenia);

		if (!difference.isEmpty()) {
			LOG.info("Found " + difference.size() + " new stations");
			List<PetrolLocations> newLocations = new ArrayList<>();
			for (PetrolLocations location : locations) {
				if (difference.contains(location.id)) {
					newLocations.add(location);
				}
			}
			LOG.info("Created list of stations");
			FileController.writeNewDataToJsonFile(provider, locations.length, difference);
			FileController.writeNewStationsToFile(provider, newLocations);
		} else {
			LOG.info("No new stations found");
		}
	}

	public void checkMoonCharge(Providers provider) {
		MoonChargeLocation[] locations;
		try {
			locations = OBJECT_MAPPER.readValue(ApiController.getLocationsFromApi(provider), MoonChargeLocation[].class);
		} catch (JsonProcessingException e) {
			LOG.error("Mapping to POJO failed");
			throw new RuntimeException(e);
		}
		LOG.info("Fetched: " + locations.length + " stations");

		Set<Integer> stationsAroundSlovenia = new LinkedHashSet<>();
		for (MoonChargeLocation location : locations) {
			stationsAroundSlovenia.add(location.id);
		}

		Set<Integer> difference = checkDifference(provider, stationsAroundSlovenia);

		if (!difference.isEmpty()) {
			LOG.info("Found " + difference.size() + " new stations");
			List<MoonChargeLocation> newLocations = new ArrayList<>();
			for (MoonChargeLocation location : locations) {
				if (difference.contains(location.id)) {
					newLocations.add(location);
				}
			}
			LOG.info("Created list of stations");
			FileController.writeNewDataToJsonFile(provider, locations.length, difference);
			FileController.writeNewStationsToFile(provider, newLocations);
		} else {
			LOG.info("No new stations found");
		}
	}

	public void checkAvant2Go(Providers provider) {
		Avant2GoLocations locations;
		try {
			locations = OBJECT_MAPPER.readValue(ApiController.getLocationsFromApi(provider), Avant2GoLocations.class);
		} catch (JsonProcessingException e) {
			LOG.error("Mapping to POJO failed");
			throw new RuntimeException(e);
		}
		LOG.info("Fetched: " + locations.results.size() + " stations");

		Integer numberOfStationsInFile = FileController.getNumberOfStationsFromFile(provider);

		if (locations.results.size() != numberOfStationsInFile) {
			LOG.info("Change detected");
			FileController.writeNewDataToJsonFile(provider, locations.results.size(), null);
			FileController.writeNewStationsToFile(provider, locations);
		} else {
			LOG.info("No new stations found");
		}
	}

	private Set<Integer> checkDifference(Providers provider, Set<Integer> stationsAroundSlovenia) {
		Set<Integer> oldStations = FileController.getStationIdsFromFile(provider);
		Set<Integer> newStations = new LinkedHashSet<>(stationsAroundSlovenia);
		LOG.info("Number of Old Stations: " + oldStations.size());
		LOG.info("Number of New Stations: " + newStations.size());
		newStations.removeAll(oldStations);

		return newStations;
	}

	/**
	 * Extracts the station ids that are within the desired geographical location
	 */
	private Set<Integer> restrictToGeoLocation(AmpecoLocationPins ampecoLocationPins) {
		LOG.info("Starting to filter " + ampecoLocationPins.pins.size() + " stations near Slovenia");
		Set<Integer> stationsAroundSlovenia = new LinkedHashSet<>();
		for (int counter = 0; counter < ampecoLocationPins.pins.size(); counter++) {
			try {
				int lat = Integer.parseInt(ampecoLocationPins.pins.get(counter).geo.split(",")[0].substring(0, 2));
				int lon = Integer.parseInt(ampecoLocationPins.pins.get(counter).geo.split(",")[1].substring(0, 2));
				if ((lat == 45 || lat == 46 || lat == 47) && (lon == 13 || lon == 14 || lon == 15 || lon == 16 || lon == 17)) {
					stationsAroundSlovenia.add(ampecoLocationPins.pins.get(counter).id);
				}
			} catch (NumberFormatException ignored) {
			}
		}
		LOG.info("Stations filtered. Total number of stations around Slovenia: " + stationsAroundSlovenia.size());
		return stationsAroundSlovenia;
	}

	/**
	 * Builds a post request body for the detailed location API
	 */
	private String buildPostRequestBody(Set<Integer> newValues) {
		StringBuilder postRequestBody = new StringBuilder("{\"locations\": {");
		for (Integer newValue : newValues) {
			postRequestBody.append("\"").append(newValue).append("\": null,");
		}
		postRequestBody.deleteCharAt(postRequestBody.length() - 1).append("}}");
		return postRequestBody.toString();
	}

	public void checkEFrend(Providers provider) {
		EfrendLocationPins efrendLocationPins;
		try {
			efrendLocationPins = OBJECT_MAPPER.readValue(ApiController.getLocationsFromApi(provider), EfrendLocationPins.class);
		} catch (JsonProcessingException e) {
			LOG.error("Mapping to POJO failed");
			throw new RuntimeException(e);
		}
		LOG.info("Fetched: " + efrendLocationPins.pins.size() + " stations");

		Set<Integer> stationsAroundSlovenia = restrictToGeoLocation(efrendLocationPins);

		Set<Integer> difference = checkDifference(provider, stationsAroundSlovenia);

		if (!difference.isEmpty()) {
			LOG.info("Found " + difference.size() + " new stations");
			EfrendDetailedLocation efrendDetailedLocation;
			try {
				efrendDetailedLocation = OBJECT_MAPPER.readValue(ApiController.getAmpecoDetailedLocationsApi(buildPostRequestBody(difference), provider), EfrendDetailedLocation.class);
			} catch (JsonProcessingException e) {
				LOG.error("Mapping to POJO failed");
				throw new RuntimeException(e);
			}
			LOG.info("Mapped " + efrendDetailedLocation.locations.size() + " stations to POJO");
			FileController.writeNewDataToJsonFile(provider, efrendLocationPins.pins.size(), difference);
			FileController.writeNewStationsToFile(provider, efrendDetailedLocation);
		} else {
			LOG.info("No new stations found");
		}
	}

	public void checkImplera(Providers provider) {
		ObjectMapper xmlMapper = new XmlMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		ImpleraLocations impleraLocations;
		try {
			impleraLocations = xmlMapper.readValue(ApiController.getLocationsFromApi(provider), ImpleraLocations.class);
		} catch (JsonProcessingException e) {
			LOG.error("Mapping to POJO failed");
			throw new RuntimeException(e);
		}
		LOG.info("Fetched: " + impleraLocations.marker.size() + " stations");

		Set<Integer> stationsAroundSlovenia = new LinkedHashSet<>();
		for (int i = 0; i < impleraLocations.marker.size(); i++) {
			stationsAroundSlovenia.add(impleraLocations.marker.get(i).id);
		}

		Set<Integer> difference = checkDifference(provider, stationsAroundSlovenia);

		if (!difference.isEmpty()) {
			LOG.info("Found " + difference.size() + " new stations");
			List<ImpleraLocations.marker> newLocations = new ArrayList<>();
			for (int i = 0; i < impleraLocations.marker.size(); i++) {
				if (difference.contains(impleraLocations.marker.get(i).id)) {
					newLocations.add(impleraLocations.marker.get(i));
				}
			}
			LOG.info("Created list of stations");
			FileController.writeNewDataToJsonFile(provider, impleraLocations.marker.size(), difference);
			FileController.writeNewStationsToFile(provider, newLocations);
		} else {
			LOG.info("No new stations found");
		}

	}
}
