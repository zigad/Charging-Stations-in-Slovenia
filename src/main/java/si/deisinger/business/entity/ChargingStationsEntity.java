package si.deisinger.business.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "charging_stations")
public class ChargingStationsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "station_id")
    private Long stationId;

    @Column(name = "provider")
    private Integer provider;

    @Column(name = "friendly_name")
    private String friendlyName;

    @Column(name = "address")
    private String address;

    @Column(name = "location")
    private String location;

    public ChargingStationsEntity() {
    }

    public ChargingStationsEntity(Long stationId, Integer provider, String friendlyName, String address, String location) {
        this.stationId = stationId;
        this.provider = provider;
        this.friendlyName = friendlyName;
        this.address = address;
        this.location = location;
    }

    public Long getStationId() {
        return stationId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
