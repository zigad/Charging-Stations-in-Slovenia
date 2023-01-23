package si.deisinger.providers.enums;

public enum Providers {
	AVANT2GO("Avant2Go", "https://api.avant2go.com/api/locations?providerID=58ee0cc36d818563a9ff46af&populate=%5B%22companyID%22,%22providerID%22,%22regionID%22%5D&filters=%7B%22chargers%22%3A%5B1%5D%7D&limit=1000&position=14.815333%2C46.119944&searchFields=name%2Caddress.city%2Caddress.address1", null),
	EFREND("eFrend", "https://efrend.eu.charge.ampeco.tech/api/v2/app/pins", "https://efrend.eu.charge.ampeco.tech/api/v2/app/locations"),
	GREMO_NA_ELEKTRIKO("GremoNaElektriko", "https://cp.emobility.gremonaelektriko.si/api/v2/app/pins", "https://cp.emobility.gremonaelektriko.si/api/v2/app/locations"),
	IMPLERA("Implera", "https://napolni.me/app/_get_P_data_xml.php?lat=46.119944&lng=14.815333&radius=200000", null),
	MOON_CHARGE("MoonCharge", "https://charge.moon-power.si/DuskyWebApi//noauthlocations?UserGPSaccessLatitude=46.119944&UserGPSaccessLongitude=14.815333&searchRadius=200000&showAlsoRoaming=false", null),
	PETROL("Petrol", "https://onecharge.eu/DuskyWebApi//noauthlocations?UserGPSaccessLatitude=46.119944&UserGPSaccessLongitude=14.815333&searchRadius=200000&showAlsoRoaming=false", null);

	private final String providerName;
	private final String url;
	private final String ampecoUrl;

	Providers(String providerName, String url, String ampecoUrl) {
		this.providerName = providerName;
		this.url = url;
		this.ampecoUrl = ampecoUrl;
	}

	public String getProviderName() {
		return providerName;
	}

	public String getUrl() {
		return url;
	}

	public String getAmpecoUrl() {
		return ampecoUrl;
	}
}
