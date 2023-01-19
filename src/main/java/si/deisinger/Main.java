package si.deisinger;

import si.deisinger.business.CheckForNewStationsProcessor;
import si.deisinger.providers.enums.Providers;

import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException {
		CheckForNewStationsProcessor checkForNewStationsProcessor = new CheckForNewStationsProcessor();
		checkForNewStationsProcessor.checkGremoNaElektriko(Providers.GREMO_NA_ELEKTRIKO);
		checkForNewStationsProcessor.checkPetrol(Providers.PETROL);
	}
}

