package si.deisinger.providers.model.generic;

import java.util.ArrayList;

public class AmpecoDetailedLocation {
	public ArrayList<Locations> locations;

	public static class Locations {
		public int id;
		public String name;
		public String address;
		public String location;
		public ArrayList<Zone> zones;
		public String updatedAt;
		public String timezone;
	}

	public static class Connector {
		public String name;
		public String icon;
		public String format;
		public String status;
	}

	public static class Evse {
		public String id;
		public String identifier;
		public String networkId;
		public int maxPower;
		public String currentType;
		public String status;
		public boolean managedByOperator;
		public int reservationMinutes;
		public boolean isAvailable;
		public String tariffId;
		public ArrayList<Connector> connectors;
		public String roamingEvseId;
		public String qrUrl;
		public String capabilities;
		public boolean hasParkingBarrier;
		public boolean canReserve;
		public boolean isTemporarilyUnavailable;
		public boolean isLongTermUnavailable;
	}

	public static class Zone {
		public ArrayList<Evse> evses;
	}
}
