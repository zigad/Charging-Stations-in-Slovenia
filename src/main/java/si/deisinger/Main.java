package si.deisinger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import si.deisinger.providers.enums.Providers;
import si.deisinger.providers.gremonaelektriko.model.LocationPins;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main {

	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public static void main(String[] args) throws JsonProcessingException {
		checkGremoNaElektriko();
	}

	private static void checkGremoNaElektriko() throws JsonProcessingException {
		System.out.println(getNumOfStationsFromFile(Providers.GREMO_NA_ELEKTRIKO.getProviderName()));

		LocationPins locationPins = OBJECT_MAPPER.readValue(getGremoNaElektrikoLocationPins(), LocationPins.class);
		System.out.println(locationPins.pins.size());
	}

	public static String getGremoNaElektrikoLocationPins() {
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

	private static int getNumOfStationsFromFile(String provider) {
		try (FileInputStream fis = new FileInputStream("src/main/resources/currentInfoPerProvider.json")) {
			JsonReader reader = Json.createReader(fis);

			JsonObject obj = reader.readObject();
			reader.close();
			return obj.getJsonObject(provider).getInt("numberOfStationsOnline");

		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
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