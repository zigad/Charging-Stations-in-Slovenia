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
	 * Gets the location data from a specified API.
	 *
	 * @param providers
	 * 		the API to get the location data from
	 * @return the location data in a string format
	 * @throws RuntimeException
	 * 		if there is an IOException or InterruptedException while sending the API request
	 */
	public static String getLocationsFromApi(Providers providers) {
		LOG.info("Getting API data for provider: {}", providers.getProviderName());
		HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
		HttpRequest request = HttpRequest.newBuilder().version(HttpClient.Version.HTTP_2).uri(URI.create(providers.getUrl())).build();
		HttpResponse<String> response;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			LOG.error("Error getting API data for provider: {}", providers.getProviderName(), e);
			throw new RuntimeException(e);
		}
		LOG.info("Received API data for provider: {}", providers.getProviderName());
		LOG.debug("Response body: {}", response.body());
		return response.body();
	}

	/**
	 * Gets detailed location data from the AMPECO API.
	 *
	 * @param postRequestBody
	 * 		the body of the POST request to send to the API
	 * @param providers
	 * 		the API to get the detailed location data from
	 * @return the detailed location data in a string format
	 * @throws RuntimeException
	 * 		if there is a URISyntaxException or if there is an IOException or InterruptedException while sending the API request
	 */
	public static String getAmpecoDetailedLocationsApi(String postRequestBody, Providers providers) {
		LOG.info("*AMPECO Only*");
		LOG.info("Getting detailed location data from AMPECO API");
		HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
		HttpRequest request;
		try {
			request = HttpRequest.newBuilder(new URI(providers.getAmpecoUrl())).version(HttpClient.Version.HTTP_2).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(postRequestBody, StandardCharsets.UTF_8)).build();
		} catch (URISyntaxException e) {
			LOG.error("Error getting detailed location data from AMPECO API", e);
			throw new RuntimeException(e);
		}
		HttpResponse<String> response;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			LOG.error("Error getting detailed location data from AMPECO API", e);
			throw new RuntimeException(e);
		}
		LOG.info("Received detailed location data from AMPECO API");
		LOG.debug("Response body: {}", response.body());
		return response.body();
	}
}
