package si.deisinger.providers.model.mooncharge;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MoonChargeLocation {

    @JsonProperty("Id")
    public Long id;
    @JsonProperty("FriendlyName")
    public String friendlyName;
    @JsonProperty("FriendlyCode")
    public String friendlyCode;
    @JsonProperty("Access")
    public Access access;
    @JsonProperty("Address")
    public Address address;
    @JsonProperty("AccessType")
    public AccessType accessType;
    @JsonProperty("TotalEvses")
    public int totalEvses;

    public static class Access {
        @JsonProperty("GPSLongitude")
        public double gPSLongitude;
        @JsonProperty("GPSLatitude")
        public double gPSLatitude;
    }

    public static class AccessType {
        @JsonProperty("IsPrivate")
        public boolean isPrivate;
        @JsonProperty("Id")
        public int id;
        @JsonProperty("Title")
        public String title;
    }

    public static class Address {
        @JsonProperty("Country")
        public Country country;
        @JsonProperty("CityName")
        public String cityName;
        @JsonProperty("PostNumber")
        public String postNumber;
        @JsonProperty("StreetName")
        public String streetName;
        @JsonProperty("HouseNumber")
        public String houseNumber;
        @JsonProperty("CityDistrictName")
        public String cityDistrictName;
    }

    public static class Country {
        @JsonProperty("Id")
        public int id;
        @JsonProperty("Code")
        public String code;
        @JsonProperty("ISO2Code")
        public String iSO2Code;
        @JsonProperty("ISO3Code")
        public String iSO3Code;
        @JsonProperty("Title")
        public String title;
    }

    public static class RoamingActor {
        @JsonProperty("Id")
        public int id;
    }
}
