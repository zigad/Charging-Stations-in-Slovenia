package si.deisinger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import si.deisinger.providers.enums.Providers;
import si.deisinger.providers.gremonaelektriko.model.LocationPins;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class Main {

	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public static void main(String[] args) throws JsonProcessingException {
		checkGremoNaElektriko();
	}

	private static void checkGremoNaElektriko() throws JsonProcessingException {
		JsonArray stationsFromFile = getStationsFromFile(Providers.GREMO_NA_ELEKTRIKO.getProviderName());
		LocationPins locationPins = OBJECT_MAPPER.readValue(getGremoNaElektrikoLocationPinsFromApi(), LocationPins.class);
		LinkedList<Integer> stationsAroundSlovenia = restrictToGeoLocation(locationPins);

		if (stationsAroundSlovenia.size() != stationsFromFile.size()) {
			Map<String, List<Integer>> diffFrom2Arrays = getDiffFrom2Arrays(stationsFromFile, stationsAroundSlovenia);
			if (diffFrom2Arrays.get("new").size() != 0) {
				System.out.println();
			}
		}

	}

	private static LinkedList<Integer> restrictToGeoLocation(LocationPins locationPins) {
		LinkedList<Integer> apiIds = new LinkedList<>();
		for (int counter = 0; counter < locationPins.pins.size(); counter++) {
			//"46.3596690,15.1137000"
			System.out.println(locationPins.pins.get(counter).geo);
			System.out.println(counter);
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

	private static LinkedList<Integer> convertToApiArray(LocationPins locationPins) {
		LinkedList<Integer> linkedList = new LinkedList<>();
		for (int i = 0; i < locationPins.pins.size(); i++) {
			linkedList.add(locationPins.pins.get(i).id);
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

		System.out.println("httpGetRequest status code: " + responseStatusCode);
		return responseBody;
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
}

/*

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {
		int numOfOldStations = getNumOfStationsFromFile("Petrol");
		JSONArray apiData = getApiData();
		int numOfStationsOnline = getApiData().length();

		System.out.println("");

		//getNumOfStationsOnline();

		/*
		JSONObject gne = new JSONObject();
		JSONObject gneInfo = new JSONObject();
		JSONArray stationIds = new JSONArray();
		stationIds.put(1);
		stationIds.put(2);
		gneInfo.put("numberOfStationsOnline", 0);
		gneInfo.put("stationsIds", stationIds);
		gne.put("GremoNaElektriko", gneInfo);
		//Write JSON file
		try (FileWriter file = new FileWriter("currentInfoPerProvider.json")) {
			file.write(gne.toString());
			file.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

private static JSONArray getApiData() throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
		.uri(URI.create("https://onecharge.eu/DuskyWebApi//noauthlocations?UserGPSaccessLatitude=46.03981500356593&UserGPSaccessLongitude=14.47101279598912&poiTypes=&searchRadius=200000&showAlsoRoaming=false"))
		.build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		return new JSONArray(response.body());
		}

private static int getNumOfStationsFromFile(String provider) {
		try (FileInputStream fis = new FileInputStream("currentInfoPerProvider.json")) {
		JsonReader reader = Json.createReader(fis);

		JsonObject obj = (JsonObject) reader.readObject();
		reader.close();
		return obj.getJsonObject(provider).getInt("numberOfStationsOnline");

		} catch (IOException e) {
		e.printStackTrace();
		}
		return 0;
		}

		}

 */