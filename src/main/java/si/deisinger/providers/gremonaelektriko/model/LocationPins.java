package si.deisinger.providers.gremonaelektriko.model;

import java.util.ArrayList;

public class LocationPins {
	public ArrayList<Pin> pins;

	public static class Av {
		public int ava;
		public int unk;
		public int una;
	}

	public static class Pin {
		public int id;
		public Object pin_image_id;
		public String geo;
		public Av av;
	}

}
