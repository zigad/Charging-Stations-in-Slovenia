package si.deisinger.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.deisinger.business.controller.ApiController;
import si.deisinger.business.controller.EmailController;
import si.deisinger.business.entity.ChargingStationsEntity;
import si.deisinger.business.repository.ChargingStationsRepository;
import si.deisinger.providers.enums.Providers;
import si.deisinger.providers.model.ampeco.AmpecoDetailedLocation;
import si.deisinger.providers.model.ampeco.AmpecoLocationPins;
import si.deisinger.providers.model.avant2go.Avant2GoLocations;
import si.deisinger.providers.model.implera.ImpleraLocations;
import si.deisinger.providers.model.mooncharge.MoonChargeLocation;
import si.deisinger.providers.model.petrol.PetrolLocations;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Processor class to handle provider station operations such as fetching, processing, and storing data.
 */
@ApplicationScoped
public class ProviderProcessor {

    private final ChargingStationsRepository chargingStationsRepository;
    private final EmailController emailController;
    private final ApiController apiController;

    private static final Logger LOG = LoggerFactory.getLogger(ProviderProcessor.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public ProviderProcessor(ChargingStationsRepository chargingStationsRepository, EmailController emailController, ApiController apiController) {
        this.chargingStationsRepository = chargingStationsRepository;
        this.emailController = emailController;
        this.apiController = apiController;
    }

    /**
     * Checks and processes provider stations.
     *
     * @param provider
     *         the provider to process
     * @param locationClass
     *         the expected class type for deserialization
     */
    public void checkProviderStations(Providers provider, Class<?> locationClass) {
        Object locationDataFromApi = fetchLocationDataFromAPI(provider, locationClass);
        int numberOfStationsFromApi = getNumberOfStationsFromApi(locationDataFromApi);
        LOG.info("Fetched {} stations for provider: {}", numberOfStationsFromApi, provider);
        Set<Long> apiStationIds = getStationIdsFromApiData(locationDataFromApi);

        // For providers other than Avant2Go, compare API IDs with the ones in DB.
        if (!provider.equals(Providers.AVANT2GO)) {
            Set<Long> newStations = findNewStationIds(provider, apiStationIds);
            if (newStations.isEmpty()) {
                LOG.info("No new stations found for provider: {}", provider);
                return;
            }
            LOG.info("Found {} new stations for provider: {}", newStations.size(), provider);
            processNewStations(provider, locationDataFromApi, newStations);
        }
        // You can extend processing for AVANT2GO (or any other provider) here if needed.
    }

    /**
     * Fetches location data from the API and deserializes it.
     *
     * @param provider
     *         the provider to fetch data for
     * @param locationClass
     *         the expected class type for deserialization
     *
     * @return the deserialized location data
     */
    private Object fetchLocationDataFromAPI(Providers provider, Class<?> locationClass) {
        switch (provider) {
            // For providers using Ampeco URLs, fetch two regions (west and east) then combine.
            case GREMONAELEKTRIKO, MEGATEL, EFREND -> {
                String queryParamsWest = "?includeAvailability=false&minLatitude=45.4215&minLongitude=13.3753&maxLatitude=46.8763&maxLongitude=14.5000&limit=5000";
                String queryParamsEast = "?includeAvailability=false&minLatitude=45.4215&minLongitude=14.5000&maxLatitude=46.8763&maxLongitude=16.6106&limit=5000";
                String locationsWest = apiController.getLocationsFromApi(provider, queryParamsWest);
                String locationsEast = apiController.getLocationsFromApi(provider, queryParamsEast);

                AmpecoLocationPins pinsWest;
                AmpecoLocationPins pinsEast;
                try {
                    pinsWest = (AmpecoLocationPins) OBJECT_MAPPER.readValue(locationsWest, locationClass);
                    pinsEast = (AmpecoLocationPins) OBJECT_MAPPER.readValue(locationsEast, locationClass);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to parse Ampeco location pins", e);
                }

                // Ensure pins list is non-null
                if (pinsWest.pins == null) {
                    pinsWest.pins = new ArrayList<>();
                }
                if (pinsEast.pins != null) {
                    pinsWest.pins.addAll(pinsEast.pins);
                }

                // Fetch detailed data based on the combined set of IDs.
                Set<Long> ids = pinsWest.pins.stream().map(pin -> pin.id).collect(Collectors.toCollection(LinkedHashSet::new));
                return fetchDetailedLocationData(provider, ids);
            }
            default -> {
                try {
                    String apiResponse = apiController.getLocationsFromApi(provider, "");
                    return OBJECT_MAPPER.readValue(apiResponse, locationClass);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to fetch location data for provider: " + provider, e);
                }
            }
        }
    }

    /**
     * Finds new station IDs by comparing API data with IDs stored in the database.
     *
     * @param provider
     *         the provider to process
     * @param apiStationIds
     *         the set of station IDs fetched from the API
     *
     * @return a set of new station IDs not yet stored in the database
     */
    private Set<Long> findNewStationIds(Providers provider, Set<Long> apiStationIds) {
        Set<Long> dbStationIds = chargingStationsRepository.findStationIdsByProvider(provider);
        LOG.info("Charging stations in DB: {}, Charging stations online: {}", dbStationIds.size(), apiStationIds.size());
        return apiStationIds.stream().filter(id -> !dbStationIds.contains(id)).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Processes new stations by filtering the fetched data and then saving and notifying via email.
     *
     * @param provider
     *         the provider to process
     * @param locationDataFromApi
     *         the fetched location data
     * @param newStations
     *         the set of new station IDs
     */
    private void processNewStations(Providers provider, Object locationDataFromApi, Set<Long> newStations) {
        if (locationDataFromApi instanceof AmpecoDetailedLocation ampecoDetailedLocation) {
            // Retain only the new stations.
            ampecoDetailedLocation.locations.removeIf(location -> !newStations.contains(location.id));
            saveAmpecoChargingStationsToDb(ampecoDetailedLocation, provider);
            sendEmailAboutNewChargingStations(ampecoDetailedLocation, provider);
        } else if (locationDataFromApi instanceof PetrolLocations[] petrolLocations) {
            List<PetrolLocations> filtered = filterLocationData(petrolLocations, newStations, PetrolLocations::getId);
            saveChargingStationsToDb(filtered, petrol -> new ChargingStationsEntity(petrol.id, Providers.PETROL.getId(), petrol.friendlyName, petrol.address.toString(), petrol.access != null ? petrol.access.toString() : null));
            sendEmailAboutNewChargingStations(filtered, provider);
        } else if (locationDataFromApi instanceof MoonChargeLocation[] moonChargeLocations) {
            List<MoonChargeLocation> filtered = filterLocationData(moonChargeLocations, newStations, MoonChargeLocation::getId);
            saveChargingStationsToDb(filtered, moon -> new ChargingStationsEntity(moon.id, Providers.MOONCHARGE.getId(), moon.friendlyName, moon.address.toString(), moon.access != null ? moon.access.toString() : null));
            sendEmailAboutNewChargingStations(filtered, provider);
        } else {
            LOG.warn("Processing for provider {} with data type {} is not implemented.", provider, locationDataFromApi.getClass().getSimpleName());
        }
    }

    /**
     * Sends an email notification with the new charging stations.
     *
     * @param detailedLocationData
     *         the detailed location data to include in the email
     * @param provider
     *         the provider being processed
     */
    private void sendEmailAboutNewChargingStations(Object detailedLocationData, Providers provider) {
        try {
            String emailBody = OBJECT_MAPPER.writer().withDefaultPrettyPrinter().writeValueAsString(detailedLocationData);
            emailController.sendMail(provider, emailBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize detailed location data for provider: " + provider, e);
        }
    }

    /**
     * Saves Ampeco station data into the database.
     *
     * @param detailedLocationData
     *         the detailed location data fetched from Ampeco
     * @param provider
     *         the provider being processed
     */
    private void saveAmpecoChargingStationsToDb(AmpecoDetailedLocation detailedLocationData, Providers provider) {
        List<ChargingStationsEntity> entities = detailedLocationData.locations.stream().map(loc -> new ChargingStationsEntity(loc.id, provider.getId(), loc.name, loc.address, loc.location)).collect(Collectors.toList());
        chargingStationsRepository.addChargingStationList(entities);
    }

    /**
     * Saves a list of charging station entities to the database using a provided mapping function.
     *
     * @param data
     *         the list of location data objects
     * @param mapper
     *         a function that maps each object to a {@link ChargingStationsEntity}
     * @param <T>
     *         the type of location data
     */
    private <T> void saveChargingStationsToDb(List<T> data, Function<T, ChargingStationsEntity> mapper) {
        List<ChargingStationsEntity> entities = data.stream().map(mapper).collect(Collectors.toList());
        chargingStationsRepository.addChargingStationList(entities);
    }

    /**
     * Fetches detailed location data for providers that use Ampeco URLs.
     *
     * @param provider
     *         the provider being processed
     * @param stationIds
     *         the set of station IDs for which to fetch details
     *
     * @return the detailed location data
     */
    private AmpecoDetailedLocation fetchDetailedLocationData(Providers provider, Set<Long> stationIds) {
        try {
            // Instead of manual string concatenation, build a request payload via a Map.
            Map<String, Object> locationsMap = new HashMap<>();
            Map<String, Object> stationsMap = new LinkedHashMap<>();
            stationIds.forEach(id -> stationsMap.put(String.valueOf(id), null));
            locationsMap.put("locations", stationsMap);
            String requestBody = OBJECT_MAPPER.writeValueAsString(locationsMap);

            String apiResponse = apiController.getAmpecoDetailedLocationsApi(requestBody, provider);
            AmpecoDetailedLocation detailedLocation = OBJECT_MAPPER.readValue(apiResponse, AmpecoDetailedLocation.class);
            // Filter out locations based on specific conditions.
            detailedLocation.locations.removeIf(location -> location.zones.getFirst().evses.getFirst().roamingEvseId != null);
            return detailedLocation;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to fetch detailed location data for provider: " + provider, e);
        }
    }

    /**
     * Filters an array of location data objects to include only those with IDs in the newStations set.
     *
     * @param locationData
     *         the array of location data objects
     * @param newStations
     *         the set of new station IDs
     * @param idExtractor
     *         a function to extract the ID from a location data object
     * @param <T>
     *         the type of location data
     *
     * @return a list of filtered location data objects
     */
    private <T> List<T> filterLocationData(T[] locationData, Set<Long> newStations, Function<T, Long> idExtractor) {
        return Arrays.stream(locationData).filter(location -> newStations.contains(idExtractor.apply(location))).collect(Collectors.toList());
    }

    /**
     * Gets the number of stations from the location data.
     *
     * @param locationData
     *         the location data object (its type defines how the count is computed)
     *
     * @return the number of stations
     */
    private int getNumberOfStationsFromApi(Object locationData) {
        return switch (locationData) {
            case AmpecoDetailedLocation detailed -> detailed.locations.size();
            case PetrolLocations[] petrol -> petrol.length;
            case MoonChargeLocation[] moon -> moon.length;
            case Avant2GoLocations avant -> avant.results.size();
            case ImpleraLocations implera -> implera.marker.size();
            default -> throw new IllegalArgumentException("Unsupported location data type: " + (locationData != null ? locationData.getClass().getSimpleName() : "null"));
        };
    }

    /**
     * Extracts a set of station IDs from the location data.
     *
     * @param locationData
     *         the location data object
     *
     * @return a set of station IDs
     */
    private Set<Long> getStationIdsFromApiData(Object locationData) {
        Set<Long> stationIds = new LinkedHashSet<>();
        switch (locationData) {
            case AmpecoDetailedLocation detailed -> detailed.locations.forEach(loc -> stationIds.add(loc.id));
            case PetrolLocations[] petrol -> {
                for (PetrolLocations loc : petrol) {
                    stationIds.add(loc.id);
                }
            }
            case MoonChargeLocation[] moon -> {
                for (MoonChargeLocation loc : moon) {
                    stationIds.add(loc.id);
                }
            }
            case Avant2GoLocations avant -> avant.results.forEach(result -> stationIds.add((long) result.hashCode()));
            case ImpleraLocations implera -> implera.marker.forEach(marker -> stationIds.add(marker.id));
            default -> throw new IllegalArgumentException("Unsupported location data type: " + (locationData != null ? locationData.getClass().getSimpleName() : "null"));
        }
        return stationIds;
    }
}
