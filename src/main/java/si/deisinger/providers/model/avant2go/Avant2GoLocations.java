package si.deisinger.providers.model.avant2go;

import java.util.ArrayList;
import java.util.Date;

public class Avant2GoLocations {
	public Pagination pagination;
	public ArrayList<Result> results;

	public static class Address {
		public String address1;
		public String zipCode;
		public String city;
		public String country;
		public String address;
	}

	public static class GeoLocation {
		public double lng;
		public double lat;
	}

	public static class Info {
		public int totalRecords;
	}

	public static class Pagination {
		public Info info;
	}

	public static class Result {
		public String _id;
		public String name;
		public Address address;
		public GeoLocation geoLocation;
		public String status;
		public Date created;
	}

}
