package si.deisinger.business;

import si.deisinger.providers.enums.Providers;

import java.io.IOException;

public interface CheckForNewStationsInterface {

	void checkGremoNaElektriko(Providers provider) throws IOException;

	void checkPetrol(Providers provider) throws IOException;
}
