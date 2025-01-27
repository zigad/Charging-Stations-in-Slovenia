package si.deisinger.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.deisinger.business.controller.ApiController;
import si.deisinger.business.controller.FileController;
import si.deisinger.providers.enums.Providers;
import si.deisinger.providers.model.avant2go.Avant2GoLocations;
import si.deisinger.providers.model.efrend.EfrendLocationPins;
import si.deisinger.providers.model.gremonaelektriko.GNELocationPins;
import si.deisinger.providers.model.implera.ImpleraLocations;
import si.deisinger.providers.model.megatel.MegaTelLocationPins;
import si.deisinger.providers.model.mooncharge.MoonChargeLocation;
import si.deisinger.providers.model.petrol.PetrolLocations;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class ProviderProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ProviderProcessor.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public void checkProviderStations(Providers provider, Class<?>... locationClass) {
        Object locationData;
        try {
            locationData = OBJECT_MAPPER.readValue(ApiController.getLocationsFromApi(provider), locationClass[0]);
        } catch (JsonProcessingException e) {
            LOG.error("Mapping to POJO failed for provider: {}", provider);
            throw new RuntimeException(e);
        }

        int fetchedStations = getNumberOfStations(locationData);
        LOG.info("Fetched: {} stations for provider: {}", fetchedStations, provider);

        Set<Integer> stationsAroundSlovenia = getStationIds(locationData);

        Set<Integer> difference = checkDifference(provider, stationsAroundSlovenia);

        if (!difference.isEmpty()) {
            LOG.info("Found {} new stations for provider: {}", difference.size(), provider);
            if (provider.getAmpecoUrl() != null && !provider.getAmpecoUrl().isBlank()) {
                // Providers with Ampeco URL: Fetch detailed locations
                Object detailedLocationData;
                try {
                    detailedLocationData = OBJECT_MAPPER.readValue(ApiController.getAmpecoDetailedLocationsApi(buildPostRequestBody(difference), provider), locationClass[1]);
                } catch (JsonProcessingException e) {
                    LOG.error("Mapping to detailed POJO failed for provider: {}", provider);
                    throw new RuntimeException(e);
                }
                LOG.info("Fetched detailed data for provider: {}", provider);
                FileController.writeNewDataToJsonFile(provider, fetchedStations, difference);
                FileController.writeNewStationsToFile(provider, detailedLocationData);
            } else {

                // Providers without Ampeco URL: Write station IDs only
                LOG.info("Provider {} does not use Ampeco; skipping detailed location fetch", provider);
                List<Object> newLocations = new ArrayList<>();

                for (Object location : locationData) {
                    if (difference.contains(location.id)) {
                        newLocations.add(location);
                    }
                }
                LOG.info("Created list of stations");
                FileController.writeNewDataToJsonFile(provider, locations.length, difference);
                FileController.writeNewStationsToFile(provider, newLocations);
            }
        } else {
            LOG.info("No new stations found for provider: {}", provider);
        }
    }

    private int getNumberOfStations(Object locationData) {
        if (locationData instanceof GNELocationPins gneLocationPins) {
            return gneLocationPins.pins.size();
        } else if (locationData instanceof PetrolLocations[] petrolLocations) {
            return petrolLocations.length;
        } else if (locationData instanceof MoonChargeLocation[] moonChargeLocations) {
            return moonChargeLocations.length;
        } else if (locationData instanceof Avant2GoLocations avant2GoLocations) {
            return avant2GoLocations.results.size();
        } else if (locationData instanceof EfrendLocationPins efrendLocationPins) {
            return efrendLocationPins.pins.size();
        } else if (locationData instanceof MegaTelLocationPins megaTelLocationPins) {
            return megaTelLocationPins.pins.size();
        } else if (locationData instanceof ImpleraLocations impleraLocations) {
            return impleraLocations.marker.size();
        }
        throw new IllegalArgumentException("Unsupported location data type");
    }

    private Set<Integer> getStationIds(Object locationData) {
        Set<Integer> stationIds = new LinkedHashSet<>();
        if (locationData instanceof GNELocationPins gneLocationPins) {
            gneLocationPins.pins.forEach(pin -> stationIds.add(pin.id));
        } else if (locationData instanceof PetrolLocations[] petrolLocations) {
            for (PetrolLocations location : petrolLocations) {
                stationIds.add(location.id);
            }
        } else if (locationData instanceof MoonChargeLocation[] moonChargeLocations) {
            for (MoonChargeLocation location : moonChargeLocations) {
                stationIds.add(location.id);
            }
        } else if (locationData instanceof Avant2GoLocations avant2GoLocations) {
            avant2GoLocations.results.forEach(result -> stationIds.add(result.hashCode()));
        } else if (locationData instanceof EfrendLocationPins efrendLocationPins) {
            efrendLocationPins.pins.forEach(pin -> stationIds.add(pin.id));
        } else if (locationData instanceof MegaTelLocationPins megaTelLocationPins) {
            megaTelLocationPins.pins.forEach(pin -> stationIds.add(pin.id));
        } else if (locationData instanceof ImpleraLocations impleraLocations) {
            impleraLocations.marker.forEach(marker -> stationIds.add(marker.id));
        } else {
            throw new IllegalArgumentException("Unsupported location data type");
        }
        return stationIds;
    }

    private Set<Integer> checkDifference(Providers provider, Set<Integer> stationsAroundSlovenia) {
        Set<Integer> oldStations = FileController.getStationIdsFromFile(provider);
        Set<Integer> newStations = new LinkedHashSet<>(stationsAroundSlovenia);
        LOG.info("Number of old stations: {}", oldStations.size());
        LOG.info("Number of new stations: {}", newStations.size());
        newStations.removeAll(oldStations);
        return newStations;
    }

    /**
     * Builds the body of a post request by creating a JSON string with the specified new values.
     *
     * @param newValues
     *         the new values to be included in the post request body
     *
     * @return a string representing the post request body in JSON format
     */
    private String buildPostRequestBody(Set<Integer> newValues) {
        StringBuilder postRequestBody = new StringBuilder("{\"locations\": {");
        for (Integer newValue : newValues) {
            postRequestBody.append("\"").append(newValue).append("\": null,");
        }
        postRequestBody.deleteCharAt(postRequestBody.length() - 1).append("}}");
        return postRequestBody.toString();
    }
}
