package si.deisinger.providers.enums;

import java.util.Optional;

//@formatter:off
public enum Providers {
    GREMONAELEKTRIKO(1, "GremoNaElektriko", "https://cp.emobility.gremonaelektriko.si/api/v2/app/pins?includeAvailability=false", "https://cp.emobility.gremonaelektriko.si/api/v2/app/locations"),
    PETROL(2, "Petrol", "https://onecharge.eu/DuskyWebApi/api/locations?searchLatitude=46.119944&searchLongitude=14.815333&searchRadius=500&showAlsoRoaming=false&onlyCurrentlyAvailable=false&onlyFreeOfCharge=false"),
    MOONCHARGE(3, "MoonCharge", "https://charge.moon-power.si/DuskyWebApi/api/locations?searchLatitude=46.119944&searchLongitude=14.815333&searchRadius=200&showAlsoRoaming=false&onlyCurrentlyAvailable=false&onlyFreeOfCharge=false"),
    EFREND(4, "eFrend", "https://efrend.eu.charge.ampeco.tech/api/v2/app/pins", "https://efrend.eu.charge.ampeco.tech/api/v2/app/locations"),
    MEGATEL(5, "MegaTel", "https://megatel.eu.charge.ampeco.tech/api/v2/app/pins", "https://megatel.eu.charge.ampeco.tech/api/v2/app/locations"),
    AVANT2GO(6, "Avant2Go", "https://api.avant2go.com/api/locations?providerID=58ee0cc36d818563a9ff46af&populate=%5B%22companyID%22,%22providerID%22,%22regionID%22%5D&filters=%7B%22chargers%22%3A%5B1%5D%7D&limit=1000&position=14.815333%2C46.119944&searchFields=name%2Caddress.city%2Caddress.address1"),
    IMPLERA(7, "Implera", "https://napolni.me/app/_get_P_data_xml.php?lat=46.119944&lng=14.815333&radius=200000");


    private final Integer id;
    private final String providerName;
    private final String url;
    private final String ampecoUrl;

    /**
     * Constructor for the {@code Providers} enum with an Ampeco URL.
     *
     * @param id
     *         id in database
     * @param providerName
     *         name of the provider
     * @param url
     *         API URL for the provider
     * @param ampecoUrl
     *         API URL for Ampeco for the provider
     */
    Providers(Integer id, String providerName, String url, String ampecoUrl) {
        this.id = id;
        this.providerName = providerName;
        this.url = url;
        this.ampecoUrl = ampecoUrl;
    }

    /**
     * Constructor for the {@code Providers} enum without an Ampeco URL.
     *
     * @param id
     *         id in database
     * @param providerName
     *         name of the provider
     * @param url
     *         API URL for the provider
     */
    Providers(Integer id, String providerName, String url) {
        this(id, providerName, url, null);
    }

    /**
     * Returns the id of the provider.
     *
     * @return the provider id.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Returns the name of the provider.
     *
     * @return the provider name.
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * Returns the API URL for the provider.
     *
     * @return the API URL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns an Optional containing the Ampeco URL for the provider if available.
     *
     * @return an Optional with the Ampeco URL.
     */
    public Optional<String> getAmpecoUrl() {
        return Optional.ofNullable(ampecoUrl);
    }

    @Override
    public String toString() {
        return providerName;
    }
}
