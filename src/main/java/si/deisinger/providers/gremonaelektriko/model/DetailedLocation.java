package si.deisinger.providers.gremonaelektriko.model;

import java.util.ArrayList;
import java.util.Date;

public class DetailedLocation {
	public ArrayList<Location> locations;

	public static class Connector {
		public String name;
		public String icon;
		public String format;
		public String status;
	}

	public static class Currency {
		public int id;
		public String name;
		public String symbol;
		public String sign;
		public String code;
		public String formatter;
		public String unitPriceFormatter;
		public Date updatedAt;
		public int minorUnitDecimal;
	}

	public static class Evse {
		public String id;
		public String identifier;
		public String networkId;
		public int maxPower;
		public String currentType;
		public String status;
		public Object operatedBy;
		public boolean managedByOperator;
		public Object reservationId;
		public int reservationMinutes;
		public boolean isAvailable;
		public String tariffId;
		public ArrayList<Connector> connectors;
		public String roamingEvseId;
		public Object midMeterCertificationEndYear;
		public String qrUrl;
		public String capabilities;
		public boolean hasParkingBarrier;
		public boolean canReserve;
		public boolean isTemporarilyUnavailable;
		public boolean isLongTermUnavailable;
		public Object corporateBillingAsDefaultPaymentMethod;
	}

	public static class Location {
		public int id;
		public String name;
		public String address;
		public Object description;
		public Object detailed_description;
		public Object additional_description;
		public String location;
		public Object what3words_address;
	}
}
