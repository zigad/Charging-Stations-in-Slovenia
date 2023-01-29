package si.deisinger.business.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.deisinger.providers.enums.Providers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ApiController {

	private static final Logger LOG = LoggerFactory.getLogger(ApiController.class);

	/**
	 * Gets location pins from the Gremo na Elektriko API
	 */
	public static String getLocationsFromApi(Providers providers) {
		LOG.info("Get API data for provider: " + providers.getProviderName());
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().version(HttpClient.Version.HTTP_2).uri(URI.create(providers.getUrl())).build();
		HttpResponse<String> response;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		LOG.info("Received API data for provider: " + providers.getProviderName());
		LOG.debug("Response body: " + response.body());
		return response.body();
	}

	/**
	 * Gets detailed location information from the Gremo na Elektriko API
	 */
	public static String getAmpecoDetailedLocationsApi(String postRequestBody, Providers providers) {
		LOG.info("*AMPECO Only*");
		LOG.info("Fetching details about new stations");
		HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
		HttpRequest request;
		try {
			request = HttpRequest.newBuilder(new URI(providers.getAmpecoUrl())).version(HttpClient.Version.HTTP_2).header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(postRequestBody, StandardCharsets.UTF_8)).build();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		HttpResponse<String> response;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		LOG.info("Received details about Gremo Na Elektriko stations");
		LOG.debug("Response body: " + response.body());
		return response.body();
	}
}
