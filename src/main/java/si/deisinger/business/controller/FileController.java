package si.deisinger.business.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.deisinger.providers.enums.Providers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Set;

@ApplicationScoped
public class FileController {

    private static final Logger LOG = LoggerFactory.getLogger(FileController.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd@HH.mm.ss");

    @Inject
    GitController gitController;

    @Inject
    EmailController emailController;

    /**
     * Writes new station data to a file.
     *
     * @param provider
     *         the provider object which holds information about the provider.
     * @param data
     *         the data to be written to the file in JSON format.
     */
    public void writeNewStationsToFile(Providers provider, Object data) {
        LOG.info("Writing data to new file in {} ", provider.getProviderName());
        String timeStamp = LocalDateTime.now().format(DATE_FORMAT);
        File file = new File(provider.getProviderName() + "/" + provider.getProviderName() + "_" + timeStamp + ".json");
        try {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, data);
            LOG.info("File created successfully");
        } catch (IOException e) {
            LOG.error("Creating new file failed", e);
            throw new RuntimeException(e);
        }
        //Commit File
        String urlOfCommit = gitController.gitCommit(provider, timeStamp);
        //Send email
        emailController.sendMail(provider, urlOfCommit);
    }

    /**
     * Writes new data to a JSON file.
     *
     * @param providers
     *         the provider object which holds information about the provider.
     * @param numOfStationsOnline
     *         the number of stations that are online.
     * @param aNew
     *         the set of IDs of the new stations.
     */
    public void writeNewDataToJsonFile(Providers providers, int numOfStationsOnline, Set<Integer> aNew) {
        LOG.info("Writing new data to JSON file for provider: {}", providers.getProviderName());
        LOG.info("Number of stations: {}", numOfStationsOnline);
        LOG.info("Station IDs: {}", aNew);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root;
        try {
            root = (ObjectNode) mapper.readTree(new File("currentInfoPerProvider.json"));
        } catch (IOException e) {
            LOG.error("Error while reading JSON file", e);
            throw new RuntimeException(e);
        }
        root.withObject("/" + providers.getProviderName()).put("numberOfStationsOnline", numOfStationsOnline);
        if (aNew != null) {
            ArrayNode stationIds = root.get(providers.getProviderName()).withArray("stationIds");
            for (Integer integer : aNew) {
                stationIds.add(integer);
            }
        }
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("currentInfoPerProvider.json"), root);
        } catch (IOException e) {
            LOG.error("Error while writing JSON file", e);
            throw new RuntimeException(e);
        }
        LOG.info("Write to file successful");
    }

    /**
     * Gets the IDs of stations from a file for a specific provider.
     *
     * @param providers
     *         the provider object which holds information about the provider.
     *
     * @return a set of station IDs.
     */
    public Set<Integer> getStationIdsFromFile(Providers providers) {
        LOG.info("Getting station IDs from file: currentInfoPerProvider.json for provider {}", providers.getProviderName());
        Set<Integer> stations = new LinkedHashSet<>();
        try (FileInputStream fis = new FileInputStream("currentInfoPerProvider.json")) {
            JsonReader jsonReader = Json.createReader(fis);
            JsonArray jsonArray = jsonReader.readObject().getJsonObject(providers.getProviderName()).getJsonArray("stationIds");
            LOG.info("Read from file successfully. Number of station IDs in file: {}", jsonArray.size());
            LOG.info("Converting JsonArray to Set from {} stations." + jsonArray.size());
            for (int i = 0; i < jsonArray.size(); i++) {
                stations.add(jsonArray.getInt(i));
            }
            LOG.info("Converted successfully. Number of IDs in Set: " + stations.size());
        } catch (IOException e) {
            LOG.error("Failed to get station IDs from file.", e);
        }
        return stations;
    }

    /**
     * Gets number of stations from a file for a specific provider.
     *
     * @param providers
     *         the provider for which the number of stations is to be retrieved.
     *
     * @return the number of stations for the specified provider.
     */
    public Integer getNumberOfStationsFromFile(Providers providers) {
        LOG.info("Getting number of stations from file: currentInfoPerProvider.json for provider: " + providers.getProviderName());
        Integer numberOfStationsOnline = null;
        try (FileInputStream fis = new FileInputStream("currentInfoPerProvider.json")) {
            JsonReader jsonReader = Json.createReader(fis);
            JsonObject jsonObject = jsonReader.readObject().getJsonObject(providers.getProviderName());
            if (jsonObject == null) {
                LOG.error("No information found for provider: " + providers.getProviderName());
                return null;
            }
            numberOfStationsOnline = jsonObject.getInt("numberOfStationsOnline");
            LOG.info("Read from file successfully. Number of stations in file: " + numberOfStationsOnline);
        } catch (IOException e) {
            LOG.error("Error reading currentInfoPerProvider.json file: " + e.getMessage());
        }
        return numberOfStationsOnline;
    }

}
