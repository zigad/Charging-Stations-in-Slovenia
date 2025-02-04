package si.deisinger.business.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import si.deisinger.business.entity.ChargingStationsEntity;
import si.deisinger.business.repository.ChargingStationsRepository;
import si.deisinger.providers.enums.Providers;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class ChargingStationsService {

    private final ChargingStationsRepository chargingStationsRepository;

    public ChargingStationsService(ChargingStationsRepository chargingStationsRepository) {
        this.chargingStationsRepository = chargingStationsRepository;
    }

    @Transactional
    public void addChargingStation(ChargingStationsEntity chargingStationsEntity) {
        chargingStationsRepository.persist(chargingStationsEntity);
    }

    @Transactional
    public List<ChargingStationsEntity> getAllChargingStations() {
        return chargingStationsRepository.listAll();
    }

    @Transactional
    public Set<Long> getListOfChargingStationsPerProvider(Providers provider) {
        List<ChargingStationsEntity> chargingStationsEntityList = chargingStationsRepository.list("provider", provider.getId());
        Set<Long> ids = new LinkedHashSet<>();
        for (ChargingStationsEntity chargingStationsEntity : chargingStationsEntityList) {
            ids.add(chargingStationsEntity.getId());
        }
        return ids;
    }
}