package si.deisinger.business.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import si.deisinger.business.entity.ChargingStationsEntity;
import si.deisinger.providers.enums.Providers;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class ChargingStationsRepository implements PanacheRepository<ChargingStationsEntity> {

    @Transactional
    public void addChargingStationList(List<ChargingStationsEntity> chargingStationsEntityList) {
        persist(chargingStationsEntityList);
    }

    @Transactional
    public Set<Long> findStationIdsByProvider(Providers provider) {
        List<ChargingStationsEntity> stations = list("provider", provider.getId());
        return stations.stream().map(ChargingStationsEntity::getStationId).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
