package si.deisinger.providers.enums;

public enum Providers {
	AVANT2GO("Avant2Go", "https://api.avant2go.com/api/locations?providerID=58ee0cc36d818563a9ff46af&populate=%5B%22companyID%22,%22providerID%22,%22regionID%22%5D&filters=%7B%22chargers%22%3A%5B1%5D%7D&limit=1000&position=14.815333%2C46.119944&searchFields=name%2Caddress.city%2Caddress.address1"),
	GREMO_NA_ELEKTRIKO("GremoNaElektriko", "https://cp.emobility.gremonaelektriko.si/api/v2/app/pins"),
	MOL_PLUNGEE("MOLPlungee", ""),
	MOON_CHARGE("MoonCharge", "https://charge.moon-power.si/DuskyWebApi//noauthlocations?UserGPSaccessLatitude=46.119944&UserGPSaccessLongitude=14.815333&searchRadius=200000&showAlsoRoaming=false"),
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
