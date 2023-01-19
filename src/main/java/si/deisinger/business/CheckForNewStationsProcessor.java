package si.deisinger.business;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.GpgConfig;
import si.deisinger.providers.enums.Providers;
import si.deisinger.providers.gremonaelektriko.model.DetailedLocation;
import si.deisinger.providers.gremonaelektriko.model.LocationPins;
import si.deisinger.providers.petrol.model.PetrolLocations;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static si.deisinger.Main.OBJECT_MAPPER;

public class CheckForNewStationsProcessor implements CheckForNewStationsInterface {

	private final Git git = Git.open(new File(""));

	public CheckForNewStationsProcessor() throws IOException {
	}

	@Override
	public void checkGremoNaElektriko() throws IOException, GitAPIException {
		LocationPins locationPins = OBJECT_MAPPER.readValue(getLocationsFromApi(Providers.GREMO_NA_ELEKTRIKO), LocationPins.class);

		LinkedList<Integer> stationsAroundSlovenia = restrictToGeoLocation(locationPins);

		Map<String, List<Integer>> diff = getDiffFrom2Arrays(getStationsFromFile(Providers.GREMO_NA_ELEKTRIKO.getProviderName()), stationsAroundSlovenia);
		if (diff.get("new").size() > 0) {
			DetailedLocation detailedLocation = OBJECT_MAPPER.readValue(getGremoNaElektrikoDetailedLocationsApi(buildPostRequestBody(diff.get("new"))), DetailedLocation.class);
			writeNewDataToJsonFile(Providers.GREMO_NA_ELEKTRIKO.getProviderName(), locationPins.pins.size(), diff.get("new"));
			writeNewStationsToFile(detailedLocation);
		}
		gitCommit(Providers.GREMO_NA_ELEKTRIKO);
	}

	@Override
	public void checkPetrol() throws IOException {
		PetrolLocations[] locations = OBJECT_MAPPER.readValue(getLocationsFromApi(Providers.PETROL), PetrolLocations[].class);
		LinkedList<Integer> stationsAroundSlovenia = convertToLinkedList(locations);

		Map<String, List<Integer>> diff = getDiffFrom2Arrays(getStationsFromFile(Providers.PETROL.getProviderName()), stationsAroundSlovenia);
		if (diff.get("new").size() > 0) {

			writeNewDataToJsonFile(Providers.PETROL.getProviderName(), locations.length, diff.get("new"));
			writeNewStationsToFile(locations);
		}
		gitCommit(Providers.PETROL);

	}

	private LinkedList<Integer> convertToLinkedList(PetrolLocations[] locations) {
		LinkedList<Integer> linkedList = new LinkedList<>();
		for (PetrolLocations location : locations) {
			linkedList.add(location.id);
		}
		return linkedList;

	}

	/**
	 * Extracts the station ids that are within the desired geographical location
	 *
	 * @param locationPins
	 * @return Ids in LinkedList<Integer> with pins around Slovenia
	 */
	private static LinkedList<Integer> restrictToGeoLocation(LocationPins locationPins) {
		LinkedList<Integer> apiIds = new LinkedList<>();
		for (int counter = 0; counter < locationPins.pins.size(); counter++) {
			try {
				int lat = Integer.parseInt(locationPins.pins.get(counter).geo.split(",")[0].substring(0, 2));
				int lon = Integer.parseInt(locationPins.pins.get(counter).geo.split(",")[1].substring(0, 2));
				if ((lat == 45 || lat == 46 || lat == 47) && (lon == 13 || lon == 14 || lon == 15 || lon == 16 || lon == 17)) {
					apiIds.add(locationPins.pins.get(counter).id);
				}
			} catch (NumberFormatException ignored) {
			}

		}
		return apiIds;
	}

	/**
	 * Finds the difference between two arrays and returns the new and removed values
	 *
	 * @param stationsFromFile
	 * @param stationsAroundSlovenia
	 * @return
	 */
	public static Map<String, List<Integer>> getDiffFrom2Arrays(JsonArray stationsFromFile, LinkedList<Integer> stationsAroundSlovenia) {
		Set<Integer> localStationsSet = convertToLocalArray(stationsFromFile);

		Map<String, List<Integer>> result = new HashMap<>();
		result.put("new", new ArrayList<>());
		result.put("removed", new ArrayList<>());
		// Find new values
		for (Integer value : stationsAroundSlovenia) {
			if (!localStationsSet.contains(value)) {
				result.get("new").add(value);
			}
		}

		// Find removed values
		for (Integer value : localStationsSet) {
			if (!stationsAroundSlovenia.contains(value)) {
				result.get("removed").add(value);
			}
		}

		return result;
	}

	/**
	 * Converts a JsonArray to a Set of integers
	 *
	 * @param stationsFromFile
	 * @return
	 */
	private static Set<Integer> convertToLocalArray(JsonArray stationsFromFile) {
		Set<Integer> localStationsSet = new HashSet<>();
		for (int i = 0; i < stationsFromFile.size(); i++) {
			localStationsSet.add(stationsFromFile.getInt(i));
		}
		return localStationsSet;
	}

	/**
	 * Builds a post request body for the detailed location API
	 *
	 * @param newValues
	 * @return
	 */
	private static String buildPostRequestBody(List<Integer> newValues) {
		StringBuilder postRequestBody = new StringBuilder("{\"locations\": {");
		for (Integer newValue : newValues) {
			postRequestBody.append("\"").append(newValue).append("\": null,");
		}
		postRequestBody.deleteCharAt(postRequestBody.length() - 1).append("}}");
		return postRequestBody.toString();
	}

	/**
	 * Writes new station data to a file
	 *
	 * @param detailedLocation
	 * @throws IOException
	 */
	private static void writeNewStationsToFile(DetailedLocation detailedLocation) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		// Write the POJO object to the JSON file
		mapper.writerWithDefaultPrettyPrinter()
				.writeValue(new File(Providers.GREMO_NA_ELEKTRIKO.getProviderName() + "/" + Providers.GREMO_NA_ELEKTRIKO.getProviderName() + "_" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd@HH.mm.ss")) + ".json"),
						detailedLocation);

	}

	private static void writeNewStationsToFile(PetrolLocations[] petrolLocations) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		// Write the POJO object to the JSON file
		mapper.writerWithDefaultPrettyPrinter()
				.writeValue(new File(Providers.PETROL.getProviderName() + "/" + Providers.PETROL.getProviderName() + "_" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd@HH.mm.ss")) + ".json"), petrolLocations);

	}

	private static void writeNewDataToJsonFile(String provider, int numOfStationsOnline, List<Integer> aNew) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode root = (ObjectNode) mapper.readTree(new File("src/main/resources/currentInfoPerProvider.json"));
		root.with(provider).put("numberOfStationsOnline", numOfStationsOnline);
		ArrayNode stationIds = root.get(provider).withArray("stationIds");
		for (Integer integer : aNew) {
			stationIds.add(integer);
		}
		root.with(provider).put("numberOfStationsOnline", numOfStationsOnline);
		mapper.writerWithDefaultPrettyPrinter().writeValue(new File("src/main/resources/currentInfoPerProvider.json"), root);
	}

	/**
	 * Gets location pins from the Gremo na Elektriko API
	 *
	 * @return
	 */
	private static String getLocationsFromApi(Providers providers) {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.version(HttpClient.Version.HTTP_2)
				.uri(URI.create(providers.getUrl()))
				.build();
		HttpResponse<String> response;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		System.out.println(response.body());
		return response.body();
	}

	/**
	 * Gets detailed location information from the Gremo na Elektriko API
	 *
	 * @param postRequestBody
	 * @return
	 */
	private static String getGremoNaElektrikoDetailedLocationsApi(String postRequestBody) {
		HttpClient client = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_2)
				.build();
		HttpRequest request = null;
		try {
			request = HttpRequest.newBuilder(new URI("https://cp.emobility.gremonaelektriko.si/api/v2/app/locations"))
					.version(HttpClient.Version.HTTP_2)
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(postRequestBody, StandardCharsets.UTF_8))
					.build();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		HttpResponse<String> response = null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		System.out.println(response.body());
		return response.body();
	}

	/**
	 * Gets station data from a file
	 *
	 * @param providerName
	 * @return
	 */
	private static JsonArray getStationsFromFile(String providerName) {
		try (FileInputStream fis = new FileInputStream("src/main/resources/currentInfoPerProvider.json")) {
			JsonReader reader = Json.createReader(fis);
			JsonArray obj = reader.readObject().getJsonObject(providerName).getJsonArray("stationIds");
			reader.close();
			return obj;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void gitCommit(Providers providers) {
		Config config = new Config();
		config.unset("gpg", null, "format");
		try {
			git.add().addFilepattern(".").call();
			git.commit().setMessage("Updated List Of Charging Stations for " + providers.getProviderName()).setGpgConfig(new GpgConfig(config)).call();
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
	}

}
