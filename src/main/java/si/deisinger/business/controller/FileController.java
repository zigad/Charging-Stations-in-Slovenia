package si.deisinger.business.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.deisinger.providers.enums.Providers;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Set;

public class FileController {

	private static final Logger LOG = LoggerFactory.getLogger(FileController.class);
	private static final GitController GIT_CONTROLLER = new GitController();
	private static final EmailController EMAIL_CONTROLLER = new EmailController();

	/**
	 * Writes new station data to a file
	 */
	public static void writeNewStationsToFile(Providers provider, Object data) {
		LOG.info("Writing data to new file in " + provider.getProviderName() + "folder");
		// Write the POJO object to the JSON file
		ObjectMapper mapper = new ObjectMapper();
		String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd@HH.mm.ss"));
		try {
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(provider.getProviderName() + "/" + provider.getProviderName() + "_" + timeStamp + ".json"), data);
		} catch (IOException e) {
			LOG.error("Creating new file failed");
			throw new RuntimeException(e);
		}
		LOG.info("File created successfully");
		//Commit File
		String urlOfCommit = GIT_CONTROLLER.gitCommit(provider, timeStamp);
		//Send email
		EMAIL_CONTROLLER.sendMail(provider, urlOfCommit);

	}

	public static void writeNewDataToJsonFile(Providers providers, int numOfStationsOnline, Set<Integer> aNew) {
		LOG.info("Writing new data to JSON file");
		LOG.info("Number of stations: " + numOfStationsOnline);
		LOG.info("Station IDs: " + aNew);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode root;
		try {
			root = (ObjectNode) mapper.readTree(new File("currentInfoPerProvider.json"));
		} catch (IOException e) {
			LOG.error("Read failed");
			throw new RuntimeException(e);
		}
		root.with(providers.getProviderName()).put("numberOfStationsOnline", numOfStationsOnline);

		if (aNew != null) {
			ArrayNode stationIds = root.get(providers.getProviderName()).withArray("stationIds");
			for (Integer integer : aNew) {
				stationIds.add(integer);
			}
		}
		try {
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File("currentInfoPerProvider.json"), root);
		} catch (IOException e) {
			LOG.error("Write failed");
			throw new RuntimeException(e);
		}
		LOG.info("Write to file successful");
	}

	/**
	 * Gets IDs of stations from a file for specific provider
	 */
	public static Set<Integer> getStationIdsFromFile(Providers providers) {
		LOG.info("Getting stations IDs from file: currentInfoPerProvider.json for provider: " + providers.getProviderName());
		Set<Integer> stations = new LinkedHashSet<>();
		try (FileInputStream fis = new FileInputStream("currentInfoPerProvider.json")) {
			JsonReader jsonReader = Json.createReader(fis);
			JsonArray jsonArray = jsonReader.readObject().getJsonObject(providers.getProviderName()).getJsonArray("stationIds");
			LOG.info("Read from file successfully. Number of stations in file: " + jsonArray.size());
			LOG.info("Mapping " + jsonArray.size() + " stations to Set<Integer>");
			for (int i = 0; i < jsonArray.size(); i++) {
				stations.add(jsonArray.getInt(i));
			}
			LOG.info("Mapped successfully. Number of stations in Set<Integer>: " + stations.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stations;
	}

	/**
	 * Gets number of stations from a file for specific provider
	 */
	public static Integer getNumberOfStationsFromFile(Providers providers) {
		LOG.info("Getting number of stations from file: currentInfoPerProvider.json for provider: " + providers.getProviderName());
		Integer numberOfStationsOnline = null;
		try (FileInputStream fis = new FileInputStream("currentInfoPerProvider.json")) {
			JsonReader jsonReader = Json.createReader(fis);
			numberOfStationsOnline = jsonReader.readObject().getJsonObject(providers.getProviderName()).getInt("numberOfStationsOnline");
			LOG.info("Read from file successfully. Number of stations in file: " + numberOfStationsOnline);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return numberOfStationsOnline;
	}

}
