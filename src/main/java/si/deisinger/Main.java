package si.deisinger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import si.deisinger.business.CheckForNewStationsProcessor;

import java.io.IOException;

public class Main {

	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	public static void main(String[] args) throws IOException {
		CheckForNewStationsProcessor checkForNewStationsProcessor = new CheckForNewStationsProcessor();
		checkForNewStationsProcessor.checkPetrol();
		checkForNewStationsProcessor.checkGremoNaElektriko();
	}
}

