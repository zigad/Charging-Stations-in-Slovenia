package si.deisinger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import si.deisinger.providers.enums.Providers;
import si.deisinger.providers.gremonaelektriko.model.DetailedLocation;
import si.deisinger.providers.gremonaelektriko.model.LocationPins;

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

public class Main {

	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	public static void main(String[] args) throws IOException {
		checkGremoNaElektriko();
	}

	private static void checkGremoNaElektriko() throws IOException {
		JsonArray stationsFromFile = getStationsFromFile(Providers.GREMO_NA_ELEKTRIKO.getProviderName());
		LocationPins locationPins = OBJECT_MAPPER.readValue(getGremoNaElektrikoLocationPinsFromApi(), LocationPins.class);
		LinkedList<Integer> stationsAroundSlovenia = restrictToGeoLocation(locationPins);

		if (locationPins.pins.size() != stationsFromFile.size()) {
			Map<String, List<Integer>> diffFrom2Arrays = getDiffFrom2Arrays(stationsFromFile, stationsAroundSlovenia);
			if (diffFrom2Arrays.get("new").size() > 0) {
				StringBuilder postRequestBody = new StringBuilder("{\"locations\": {");
				for (int i = 0; i < diffFrom2Arrays.get("new").size(); i++) {
					postRequestBody.append("\"").append(diffFrom2Arrays.get("new").get(i)).append("\": null,");
				}
				postRequestBody.deleteCharAt(postRequestBody.length() - 1).append("}}");
				DetailedLocation detailedLocation = OBJECT_MAPPER.readValue(getGremoNaElektrikoDetailedLocationsApi(postRequestBody.toString()), DetailedLocation.class);

				writeNewDataToJsonFile(Providers.GREMO_NA_ELEKTRIKO.getProviderName(), locationPins.pins.size(), diffFrom2Arrays.get("new"));
				writeNewStationsToFile(Providers.GREMO_NA_ELEKTRIKO, detailedLocation);

			}
		}
	}

	private static LinkedList<Integer> restrictToGeoLocation(LocationPins locationPins) {
		LinkedList<Integer> apiIds = new LinkedList<>();
		for (int counter = 0; counter < locationPins.pins.size(); counter++) {
			//"46.3596690,15.1137000"
			try {
				int lat = Integer.parseInt(locationPins.pins.get(counter).geo.split(",")[0].substring(0, 2));
				int lon = Integer.parseInt(locationPins.pins.get(counter).geo.split(",")[1].substring(0, 2));
				if ((lat == 45 || lat == 46 || lat == 47) && (lon == 13 || lon == 14 || lon == 15 || lon == 16 || lon == 17)) {
					apiIds.add(locationPins.pins.get(counter).id);
				}
			} catch (NumberFormatException e) {
				continue;
			}

		}
		return apiIds;

	}

	public static Map<String, List<Integer>> getDiffFrom2Arrays(JsonArray stationsFromFile, LinkedList<Integer> stationsAroundSlovenia) {
		LinkedList<Integer> localStationsArray = convertToLocalArray(stationsFromFile);

		Map<String, List<Integer>> result = new HashMap<>();
		List<Integer> newValues = new ArrayList<>();
		List<Integer> removedValues = new ArrayList<>();

		// Find new values
		for (Integer value : stationsAroundSlovenia) {
			if (!localStationsArray.contains(value)) {
				newValues.add(value);
			}
		}

		// Find removed values
		for (Integer value : localStationsArray) {
			if (!stationsAroundSlovenia.contains(value)) {
				removedValues.add(value);
			}
		}

		result.put("new", newValues);
		result.put("removed", removedValues);
		return result;
	}

	private static LinkedList<Integer> convertToLocalArray(JsonArray stationsFromFile) {
		LinkedList<Integer> linkedList = new LinkedList<>();
		for (int i = 0; i < stationsFromFile.size(); i++) {
			linkedList.add(stationsFromFile.getInt(i));
		}
		return linkedList;
	}

	public static String getGremoNaElektrikoLocationPinsFromApi() {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.version(HttpClient.Version.HTTP_2)
				.uri(URI.create("https://cp.emobility.gremonaelektriko.si/api/v2/app/pins"))
				.build();
		HttpResponse<String> response = null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}

		String responseBody = response.body();
		int responseStatusCode = response.statusCode();

		return responseBody;
	}

	//https://github.com/eugenp/tutorials/blob/master/core-java-modules/core-java-11/src/main/java/com/baeldung/java11/httpclient/HttpClientExample.java

	public static String getGremoNaElektrikoDetailedLocationsApi(String body) {
		HttpClient client = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_2)
				.build();
		HttpRequest request = null;
		try {
			request = HttpRequest.newBuilder(new URI("https://cp.emobility.gremonaelektriko.si/api/v2/app/locations"))
					.version(HttpClient.Version.HTTP_2)
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
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

	private static JsonArray getStationsFromFile(String provider) {
		try (FileInputStream fis = new FileInputStream("src/main/resources/currentInfoPerProvider.json")) {
			JsonReader reader = Json.createReader(fis);
			JsonArray obj = reader.readObject().getJsonObject(provider).getJsonArray("stationIds");
			reader.close();
			return obj;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static JsonArray writeNewDataToJsonFile(String provider, int numOfStationsOnline, List<Integer> aNew) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode root = (ObjectNode) mapper.readTree(new File("src/main/resources/currentInfoPerProvider.json"));
		root.with(provider).put("numberOfStationsOnline", numOfStationsOnline);
		ArrayNode stationIds = root.get(provider).withArray("stationIds");
		for (Integer integer : aNew) {
			stationIds.add(integer);
		}
		root.with(provider).put("numberOfStationsOnline", numOfStationsOnline);
		mapper.writerWithDefaultPrettyPrinter().writeValue(new File("src/main/resources/currentInfoPerProvider.json"), root);
		return null;
	}

	private static void writeNewStationsToFile(Providers provider, DetailedLocation detailedLocation) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		// Write the POJO object to the JSON file
		mapper.writerWithDefaultPrettyPrinter()
				.writeValue(new File(provider.getProviderName() + "/" + provider.getProviderName() + "_" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd@HH.mm.ss")) + ".json"), detailedLocation);

	}
}