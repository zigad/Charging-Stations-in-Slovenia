package si.deisinger.providers.enums;

public enum Providers {
	AVANT2GO("Avant2Go", ""),
	GREMO_NA_ELEKTRIKO("GremoNaElektriko", "https://cp.emobility.gremonaelektriko.si/api/v2/app/pins"),
	MOL_PLUNGEE("MOLPlungee", ""),
	MOON_CHARGE("MoonCharge", ""),
	PETROL("Petrol", "https://onecharge.eu/DuskyWebApi//noauthlocations?UserGPSaccessLatitude=46.119944&UserGPSaccessLongitude=14.815333&searchRadius=200000&showAlsoRoaming=false");

	private final String providerName;
	private final String url;

	Providers(String providerName, String url) {
		this.providerName = providerName;
		this.url = url;
	}

	public String getProviderName() {
		return providerName;
	}

	public String getUrl() {
		return url;
	}
}
