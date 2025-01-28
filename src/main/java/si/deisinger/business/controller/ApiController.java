package si.deisinger.business.controller;

import jakarta.enterprise.context.ApplicationScoped;
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

/**
 * Controller for interacting with external APIs. Provides methods for fetching location data and detailed data from APIs, including AMPECO-specific endpoints.
 */
@ApplicationScoped
public class ApiController {

    private static final Logger LOG = LoggerFactory.getLogger(FileController.class);
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

    /**
     * Fetches location data from the specified API provider.
     *
     * @param provider
     *         the API provider from which to fetch location data
     *
     * @return the location data as a string
     *
     * @throws IllegalStateException
     *         if an error occurs while sending the API request
     */
    public String getLocationsFromApi(Providers provider) {
        LOG.info("Fetching location data from provider: {}", provider.getProviderName());
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(provider.getUrl())).GET().build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            LOG.info("Successfully fetched location data from provider: {}", provider.getProviderName());
            LOG.debug("Response body: {}", response.body());
            return response.body();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Error while fetching location data from provider: {}", provider.getProviderName(), e);
            throw new IllegalStateException("Failed to fetch location data", e);
        }
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
        LOG.info("Fetching detailed location data from AMPECO API for provider: {}", provider.getProviderName());

        HttpRequest request;
        try {
            request = HttpRequest.newBuilder().uri(new URI(provider.getAmpecoUrl())).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(postRequestBody, StandardCharsets.UTF_8)).build();
        } catch (URISyntaxException e) {
            LOG.error("Invalid URI for AMPECO API endpoint", e);
            throw new IllegalStateException("Invalid URI for AMPECO API endpoint", e);
        }

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            LOG.info("Successfully fetched detailed location data from AMPECO API for provider: {}", provider.getProviderName());
            LOG.debug("Response body: {}", response.body());
            return response.body();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Error while fetching detailed location data from AMPECO API", e);
            throw new IllegalStateException("Failed to fetch detailed location data", e);
        }
    }
}
