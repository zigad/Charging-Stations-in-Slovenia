package si.deisinger.providers.enums;

public enum Providers {
	GREMO_NA_ELEKTRIKO("GremoNaElektriko");

	private final String providerName;

	Providers(String providerName) {
		this.providerName = providerName;
	}

	public String getProviderName() {
		return providerName;
	}
}
