package si.deisinger.business.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import si.deisinger.business.entity.ChargingStationsEntity;

@ApplicationScoped
public class ChargingStationsRepository implements PanacheRepository<ChargingStationsEntity> {
}
