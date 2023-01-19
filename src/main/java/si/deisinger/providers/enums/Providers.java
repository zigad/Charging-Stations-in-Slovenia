package si.deisinger.providers.enums;

public enum Providers {
	AVANT2GO("Avant2Go"),
	GREMO_NA_ELEKTRIKO("GremoNaElektriko"),
	MOL_PLUNGEE("MOLPlungee"),
	MOON_CHARGE("MoonCharge");

	private final String providerName;

	Providers(String providerName) {
		this.providerName = providerName;
	}

	public String getProviderName() {
		return providerName;
	}
}
