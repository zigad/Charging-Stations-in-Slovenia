package si.deisinger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.deisinger.business.ProviderProcessor;
import si.deisinger.providers.enums.Providers;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		ProviderProcessor providerProcessor = new ProviderProcessor();
		LOG.info("Checking provider: " + Providers.GREMO_NA_ELEKTRIKO.getProviderName());
		providerProcessor.checkGremoNaElektriko(Providers.GREMO_NA_ELEKTRIKO);
		LOG.info("Checking provider: " + Providers.PETROL.getProviderName());
		providerProcessor.checkPetrol(Providers.PETROL);
	}
}

