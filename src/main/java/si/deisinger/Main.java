package si.deisinger;

import si.deisinger.business.CheckForNewStationsProcessor;

import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException {
		CheckForNewStationsProcessor checkForNewStationsProcessor = new CheckForNewStationsProcessor();
		checkForNewStationsProcessor.checkGremoNaElektriko();
		switch (args[0]) {
			case "gne" -> checkForNewStationsProcessor.checkGremoNaElektriko();
			case "petrol" -> checkForNewStationsProcessor.checkPetrol();
			default -> System.out.println("Invalid input.");
		}
	}
}

