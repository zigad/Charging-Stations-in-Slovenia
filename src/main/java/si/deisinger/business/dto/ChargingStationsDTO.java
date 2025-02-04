package si.deisinger.business.dto;

public class ChargingStationsDTO {

    private String stationId;
    private String provider;
    private String friendlyName;
    private String address;
    private String location;

    @Override
    public String toString() {
        return "{" + "stationId='" + stationId + '\'' + ", provider='" + provider + '\'' + ", friendlyName='" + friendlyName + '\'' + ", address='" + address + '\'' + ", location='" + location + '\'' + '}';
    }
}
