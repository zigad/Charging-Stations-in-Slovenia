package si.deisinger.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.deisinger.business.controller.ApiController;
import si.deisinger.business.controller.FileController;
import si.deisinger.business.entity.ChargingStationsEntity;
import si.deisinger.business.service.ChargingStationsService;
import si.deisinger.providers.enums.Providers;
import si.deisinger.providers.model.ampeco.AmpecoLocationPins;
import si.deisinger.providers.model.avant2go.Avant2GoLocations;
import si.deisinger.providers.model.efrend.EfrendLocationPins;
import si.deisinger.providers.model.gremonaelektriko.GNELocationPins;
import si.deisinger.providers.model.implera.ImpleraLocations;
import si.deisinger.providers.model.megatel.MegaTelLocationPins;
import si.deisinger.providers.model.mooncharge.MoonChargeLocation;
import si.deisinger.providers.model.petrol.PetrolLocations;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Processor class to handle provider station operations such as fetching, processing, and storing data.
 */
@ApplicationScoped
public class ProviderProcessor {

    private final FileController fileController;
    private final ApiController apiController;
    private final ChargingStationsService chargingStationsService;

    private static final Logger LOG = LoggerFactory.getLogger(ProviderProcessor.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public ProviderProcessor(FileController fileController, ApiController apiController, ChargingStationsService chargingStationsService) {
        this.fileController = fileController;
        this.apiController = apiController;
        this.chargingStationsService = chargingStationsService;
    }

    /**
     * Checks and processes provider stations.
     *
     * @param provider
     *         the provider to process
     * @param locationClass
     *         array of location classes for deserialization
     */
    public void checkProviderStations(Providers provider, Class<?>... locationClass) {
        Object locationData = fetchLocationDataFromAPI(provider, locationClass[0]);

        int numberOfFetchedStations = getNumberOfStations(locationData);
        LOG.info("Fetched {} stations for provider: {}", numberOfFetchedStations, provider);
        Set<Integer> currentStationIds = getStationIds(locationData);
        List<ChargingStationsEntity> numberOfStationsForProvider = chargingStationsService.getListOfChargingStationsPerProvider(provider);
        if (!provider.equals(Providers.AVANT2GO)) {
            Set<Integer> newStations = findNewStations(provider, currentStationIds);
            if (newStations.isEmpty()) {
                LOG.info("No new stations found for provider: {}", provider);
                return;
            }
            LOG.info("Found {} new stations for provider: {}", newStations.size(), provider);
            processNewStations(provider, locationData, newStations, locationClass);
        } else {

            List<ChargingStationsEntity> numberOfStationsForProviderq = chargingStationsService.getListOfChargingStationsPerProvider(provider.getId());
            Integer numberOfStationsInFile = fileController.getNumberOfStationsFromFile(provider);

            if (numberOfFetchedStations != numberOfStationsInFile) {
                LOG.info("Change detected");
                fileController.writeNewDataToJsonFile(provider, numberOfFetchedStations, null);
                fileController.writeNewStationsToFile(provider, locationData);
            } else {
                LOG.info("No new stations found");
            }
        }

    }

    /**
     * Fetches location data from the API and deserializes it.
     *
     * @param provider
     *         the provider to fetch data for
     * @param locationClass
     *         the class type for deserialization
     *
     * @return deserialized location data
     */
    private Object fetchLocationDataFromAPI(Providers provider, Class<?> locationClass) {
        try {
            String apiResponse = apiController.getLocationsFromApi(provider);
            return OBJECT_MAPPER.readValue(apiResponse, locationClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to fetch location data", e);
        }
    }

    /**
     * A utility method to extract IDs from different data types.
     *
     * @param data
     *         the data to process
     * @param idExtractor
     *         a function to extract IDs from individual objects
     *
     * @return a set of IDs
     */
    private <T> Set<Integer> extractIdsFromData(Object data, java.util.function.Function<T, Integer> idExtractor) {
        if (data instanceof Iterable<?> iterable) {
            return StreamSupport.stream(iterable.spliterator(), false).map(item -> idExtractor.apply((T) item)).collect(Collectors.toSet());
        } else if (data.getClass().isArray()) {
            return Arrays.stream((Object[]) data).map(item -> idExtractor.apply((T) item)).collect(Collectors.toSet());
        }
        throw new IllegalArgumentException("Unsupported data type");
    }

    /**
     * Finds new stations by comparing current stations with stored ones.
     *
     * @param provider
     *         the provider to process
     * @param currentStations
     *         the current station IDs
     *
     * @return a set of new station IDs
     */
    private Set<Integer> findNewStations(Providers provider, Set<Integer> currentStations) {
        Set<Integer> storedStations = fileController.getStationIdsFromFile(provider);
        LOG.info("Stored stations: {}, Current stations: {}", storedStations.size(), currentStations.size());

        return currentStations.stream().filter(station -> !storedStations.contains(station)).collect(Collectors.toSet());
    }

    /**
     * Processes new stations and writes data to appropriate files.
     *
     * @param provider
     *         the provider to process
     * @param locationData
     *         the fetched location data
     * @param newStations
     *         the new station IDs
     * @param locationClass
     *         array of location classes for detailed processing
     */
    private void processNewStations(Providers provider, Object locationData, Set<Integer> newStations, Class<?>... locationClass) {
        if (provider.getAmpecoUrl() != null && !provider.getAmpecoUrl().isBlank()) {
            Object detailedLocationData = fetchDetailedLocationData(provider, newStations, locationClass[1]);
            fileController.writeNewStationsToFile(provider, detailedLocationData);
        } else {
            List<Object> newLocationData = filterLocationData(locationData, newStations);
            fileController.writeNewStationsToFile(provider, newLocationData);
        }

        fileController.writeNewDataToJsonFile(provider, newStations.size(), newStations);
    }

    /**
     * Fetches detailed location data for providers with Ampeco URLs.
     *
     * @param provider
     *         the provider to process
     * @param newStations
     *         the new station IDs
     * @param detailClass
     *         the class type for detailed deserialization
     *
     * @return deserialized detailed location data
     */
    private Object fetchDetailedLocationData(Providers provider, Set<Integer> newStations, Class<?> detailClass) {
        try {
            String requestBody = buildPostRequestBody(newStations);
            String apiResponse = apiController.getAmpecoDetailedLocationsApi(requestBody, provider);
            return OBJECT_MAPPER.readValue(apiResponse, detailClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to fetch detailed location data", e);
        }
    }

    /**
     * Filters location data to include only new stations.
     *
     * @param locationData
     *         the fetched location data
     * @param newStations
     *         the new station IDs
     *
     * @return a list of new location data
     */
    private List<Object> filterLocationData(Object locationData, Set<Integer> newStations) {
        return extractIdsFromData(
                locationData, location -> {
                    if (newStations.contains(getStationId(location))) {
                        return getStationId(location);
                    }
                    return null;
                }
        ).stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Gets the number of stations from the location data.
     *
     * @param locationData
     *         the location data to process
     *
     * @return the number of stations
     */
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

    /**
     * Extracts the station ID from a location object.
     *
     * @param location
     *         the location object
     *
     * @return the station ID
     */
    private int getStationId(Object location) {
        return switch (location) {
            case AmpecoLocationPins.Pin ampecoPin -> ampecoPin.id;
            case PetrolLocations petrolLocation -> petrolLocation.id;
            case MoonChargeLocation moonChargeLocation -> moonChargeLocation.id;
            case Avant2GoLocations.Result avant2GoResult -> avant2GoResult.hashCode();
            case ImpleraLocations.marker impleraMarker -> impleraMarker.id;
            default -> throw new IllegalArgumentException("Unsupported location type");
        };
    }

    /**
     * Builds a JSON request body for fetching detailed station data.
     *
     * @param stationIds
     *         the IDs of stations to include in the request
     *
     * @return the JSON request body as a string
     */
    private String buildPostRequestBody(Set<Integer> stationIds) {
        return stationIds.stream().map(id -> String.format("\"%d\":null", id)).collect(Collectors.joining(",", "{\"locations\":{", "}}"));
    }

    private Set<Integer> getStationIds(Object locationData) {
        Set<Integer> stationIds = new LinkedHashSet<>();
        switch (locationData) {
            case GNELocationPins gneLocationPins -> gneLocationPins.pins.forEach(pin -> stationIds.add(pin.id));
            case PetrolLocations[] petrolLocations -> {
                for (PetrolLocations location : petrolLocations) {
                    stationIds.add(location.id);
                }
            }
            case MoonChargeLocation[] moonChargeLocations -> {
                for (MoonChargeLocation location : moonChargeLocations) {
                    stationIds.add(location.id);
                }
            }
            case Avant2GoLocations avant2GoLocations -> avant2GoLocations.results.forEach(result -> stationIds.add(result.hashCode()));
            case EfrendLocationPins efrendLocationPins -> efrendLocationPins.pins.forEach(pin -> stationIds.add(pin.id));
            case MegaTelLocationPins megaTelLocationPins -> megaTelLocationPins.pins.forEach(pin -> stationIds.add(pin.id));
            case ImpleraLocations impleraLocations -> impleraLocations.marker.forEach(marker -> stationIds.add(marker.id));
            case null, default -> throw new IllegalArgumentException("Unsupported location data type");
        }
        return stationIds;
    }
}
