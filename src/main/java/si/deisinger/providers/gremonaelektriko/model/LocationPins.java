package si.deisinger.providers.gremonaelektriko.model;

import java.util.ArrayList;

public class LocationPins {
	public ArrayList<Pin> pins;

	public static class Pin {
		public int id;
		public String geo;
	}
}
