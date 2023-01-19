package si.deisinger.business;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.GpgConfig;
import si.deisinger.providers.enums.Providers;
import si.deisinger.providers.model.gremonaelektriko.GNEDetailedLocation;
import si.deisinger.providers.model.gremonaelektriko.GNELocationPins;
import si.deisinger.providers.model.petrol.PetrolLocations;

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
import java.util.LinkedHashSet;
import java.util.Set;

public class CheckForNewStationsProcessor implements CheckForNewStationsInterface {

	private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	public CheckForNewStationsProcessor() {
	}

	@Override
	public void checkGremoNaElektriko(Providers provider) throws IOException {
		GNELocationPins GNELocationPins = objectMapper.readValue(getLocationsFromApi(provider), GNELocationPins.class);
		Set<Integer> stationsAroundSlovenia = restrictToGeoLocation(GNELocationPins);

		Set<Integer> oldStations = getStationsFromFile(provider);
		Set<Integer> newStations = new LinkedHashSet<>(stationsAroundSlovenia);
		newStations.removeAll(oldStations);

		if (!newStations.isEmpty()) {
			GNEDetailedLocation GNEDetailedLocation = objectMapper.readValue(getGremoNaElektrikoDetailedLocationsApi(buildPostRequestBody(newStations)), GNEDetailedLocation.class);
			writeNewDataToJsonFile(provider, GNELocationPins.pins.size(), newStations);
			writeNewStationsToFile(provider, GNEDetailedLocation);
		}

		gitCommit(provider);
	}

	@Override
	public void checkPetrol(Providers provider) throws IOException {
		PetrolLocations[] locations = objectMapper.readValue(getLocationsFromApi(provider), PetrolLocations[].class);
		Set<Integer> stationsAroundSlovenia = new LinkedHashSet<>();
		for (PetrolLocations location : locations) {
			stationsAroundSlovenia.add(location.id);
		}

		Set<Integer> oldStations = getStationsFromFile(provider);
		Set<Integer> newStations = new LinkedHashSet<>(stationsAroundSlovenia);
		newStations.removeAll(oldStations);

		if (!newStations.isEmpty()) {
			writeNewDataToJsonFile(provider, locations.length, newStations);
			writeNewStationsToFile(provider, locations);
		}

		gitCommit(provider);
	}

	/**
	 * Extracts the station ids that are within the desired geographical location
	 */
	private static Set<Integer> restrictToGeoLocation(GNELocationPins GNELocationPins) {
		Set<Integer> apiIds = new LinkedHashSet<>();
		for (int counter = 0; counter < GNELocationPins.pins.size(); counter++) {
			try {
				int lat = Integer.parseInt(GNELocationPins.pins.get(counter).geo.split(",")[0].substring(0, 2));
				int lon = Integer.parseInt(GNELocationPins.pins.get(counter).geo.split(",")[1].substring(0, 2));
				if ((lat == 45 || lat == 46 || lat == 47) && (lon == 13 || lon == 14 || lon == 15 || lon == 16 || lon == 17)) {
					apiIds.add(GNELocationPins.pins.get(counter).id);
				}
			} catch (NumberFormatException ignored) {
			}

		}
		return apiIds;
	}

	/**
	 * Builds a post request body for the detailed location API
	 */
	private static String buildPostRequestBody(Set<Integer> newValues) {
		StringBuilder postRequestBody = new StringBuilder("{\"locations\": {");
		for (Integer newValue : newValues) {
			postRequestBody.append("\"").append(newValue).append("\": null,");
		}
		postRequestBody.deleteCharAt(postRequestBody.length() - 1).append("}}");
		return postRequestBody.toString();
	}

	/**
	 * Writes new station data to a file
	 */
	private static void writeNewStationsToFile(Providers providers, Object data) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		// Write the POJO object to the JSON file
		mapper.writerWithDefaultPrettyPrinter()
				.writeValue(new File(providers.getProviderName() + "/" + providers.getProviderName() + "_" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd@HH.mm.ss")) + ".json"),
						data);

	}

	private static void writeNewDataToJsonFile(Providers providers, int numOfStationsOnline, Set<Integer> aNew) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode root = (ObjectNode) mapper.readTree(new File("currentInfoPerProvider.json"));
		root.with(providers.getProviderName()).put("numberOfStationsOnline", numOfStationsOnline);
		ArrayNode stationIds = root.get(providers.getProviderName()).withArray("stationIds");
		for (Integer integer : aNew) {
			stationIds.add(integer);
		}
		root.with(providers.getProviderName()).put("numberOfStationsOnline", numOfStationsOnline);
		mapper.writerWithDefaultPrettyPrinter().writeValue(new File("currentInfoPerProvider.json"), root);
	}

	/**
	 * Gets location pins from the Gremo na Elektriko API
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
	 */
	private static String getGremoNaElektrikoDetailedLocationsApi(String postRequestBody) {
		HttpClient client = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_2)
				.build();
		HttpRequest request;
		try {
			request = HttpRequest.newBuilder(new URI("https://cp.emobility.gremonaelektriko.si/api/v2/app/locations"))
					.version(HttpClient.Version.HTTP_2)
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(postRequestBody, StandardCharsets.UTF_8))
					.build();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
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
	 * Gets station data from a file
	 */
	private static Set<Integer> getStationsFromFile(Providers providers) {
		Set<Integer> stations = new LinkedHashSet<>();
		try (FileInputStream fis = new FileInputStream("currentInfoPerProvider.json")) {
			JsonReader jsonReader = Json.createReader(fis);
			JsonArray jsonArray = jsonReader.readObject().getJsonObject(providers.getProviderName()).getJsonArray("stationIds");

			for (int i = 0; i < jsonArray.size(); i++) {
				stations.add(jsonArray.getInt(i));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stations;
	}

	private void gitCommit(Providers provider) {
		Config config = new Config();
		config.unset("gpg", null, "format");
		try (Git git = Git.open(new File(""))) {
			git.add().addFilepattern(".").call();
			git.commit().setMessage("Updated List Of Charging Stations for " + provider.getProviderName()).setGpgConfig(new GpgConfig(config)).call();
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}
	}

}
