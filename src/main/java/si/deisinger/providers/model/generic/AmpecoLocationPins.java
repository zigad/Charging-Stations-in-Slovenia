package si.deisinger.providers.model.generic;

import java.util.ArrayList;

public class AmpecoLocationPins {
	public ArrayList<Pin> pins;

	public static class Pin {
		public int id;
		public String geo;
	}
}
