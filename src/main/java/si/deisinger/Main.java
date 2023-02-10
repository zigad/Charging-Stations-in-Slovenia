package si.deisinger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.deisinger.business.ProviderProcessor;
import si.deisinger.providers.enums.Providers;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	/**
	 * The main method of the class. This method invokes methods for checking each provider from the enum {@link Providers}. The provider is checked by calling the corresponding method from the {@link ProviderProcessor} class. Information about the provider
	 * being checked is logged.
	 *
	 * @param args
	 * 		the command line arguments, not used in this method.
	 */
	public static void main(String[] args) {
		ProviderProcessor providerProcessor = new ProviderProcessor();
		for (Providers provider : Providers.values()) {
			LOG.info("Checking provider: " + provider.getProviderName());
			switch (provider) {
				case GREMONAELEKTRIKO -> providerProcessor.checkGremoNaElektriko(provider);
				case PETROL -> providerProcessor.checkPetrol(provider);
				case MOONCHARGE -> providerProcessor.checkMoonCharge(provider);
				case AVANT2GO -> providerProcessor.checkAvant2Go(provider);
				case EFREND -> providerProcessor.checkEFrend(provider);
				case MEGATEL -> providerProcessor.checkMegaTel(provider);
				case IMPLERA -> providerProcessor.checkImplera(provider);
				default -> throw new IllegalStateException("Unexpected value: " + provider);
			}
		}
	}
}

