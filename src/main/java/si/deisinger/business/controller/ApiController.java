package si.deisinger.business.controller;

import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.deisinger.providers.enums.Providers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Controller for interacting with external APIs. Provides methods for fetching location data and detailed data from APIs, including AMPECO-specific endpoints.
 */
@Singleton
public class ApiController {

    private static final Logger LOG = LoggerFactory.getLogger(ApiController.class);
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

    /**
     * Fetches location data from the specified API provider.
     *
     * @param provider
     *         the API provider from which to fetch location data
     * @param parameters
     *         additional URL parameters for the request
     *
     * @return the location data as a string
     *
     * @throws IllegalStateException
     *         if an error occurs while sending the API request
     */
    public String getLocationsFromApi(Providers provider, String parameters) {
        String url = provider.getUrl() + parameters;
        LOG.info("Fetching location data from provider: {} using URL: {}", provider.getProviderName(), url);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        return sendRequest(request, provider.getProviderName(), "location data");
    }

    /**
     * Fetches detailed location data from the AMPECO API.
     *
     * @param postRequestBody
     *         the JSON body of the POST request
     * @param provider
     *         the API provider containing AMPECO-specific endpoint details
     *
     * @return the detailed location data as a string
     *
     * @throws IllegalStateException
     *         if an error occurs while sending the API request
     */
    public String getAmpecoDetailedLocationsApi(String postRequestBody, Providers provider) {
        String url = provider.getAmpecoUrl().orElseThrow(() -> new IllegalArgumentException("No Ampeco URL available for provider: " + provider.getProviderName()));
        LOG.info("Fetching detailed location data from AMPECO API for provider: {} using URL: {}", provider.getProviderName(), url);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(postRequestBody, StandardCharsets.UTF_8)).build();
        return sendRequest(request, provider.getProviderName(), "detailed location data from AMPECO API");
    }

    /**
     * Sends the provided HTTP request and returns the response body if the request is successful. It also verifies that the HTTP status code indicates success (i.e. 2xx).
     *
     * @param request
     *         the HTTP request to send
     * @param providerName
     *         the provider's name (used for logging)
     * @param dataDescription
     *         a brief description of the data being fetched (used for logging)
     *
     * @return the response body as a string
     *
     * @throws IllegalStateException
     *         if the request is interrupted, fails due to an I/O error, or returns a non-success status code
     */
    private String sendRequest(HttpRequest request, String providerName, String dataDescription) {
        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                LOG.error("Non-success HTTP status {} when fetching {} for provider: {}. Response body: {}", response.statusCode(), dataDescription, providerName, response.body());
                throw new IllegalStateException("Non-success HTTP status: " + response.statusCode());
            }
            LOG.info("Successfully fetched {} for provider: {}", dataDescription, providerName);
            LOG.debug("Response body: {}", response.body());
            return response.body();
        } catch (InterruptedException e) {
            // Preserve the interrupt status and handle the interruption appropriately.
            Thread.currentThread().interrupt();
            LOG.error("Request interrupted while fetching {} for provider: {}", dataDescription, providerName, e);
            throw new IllegalStateException("Interrupted while fetching " + dataDescription, e);
        } catch (IOException e) {
            LOG.error("I/O error while fetching {} for provider: {}", dataDescription, providerName, e);
            throw new IllegalStateException("I/O error while fetching " + dataDescription, e);
        }
    }
}
