package si.deisinger.business.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "charging_stations")
public class ChargingStationsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(name = "station_id")
    private String stationId;

    @Column(name = "provider")
    private Integer provider;

    @Column(name = "friendly_name")
    private String friendlyName;

    @Column(name = "address")
    private String address;

    @Column(name = "location")
    private String location;

}
