package si.deisinger.business.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import si.deisinger.business.entity.ChargingStationsEntity;
import si.deisinger.business.repository.ChargingStationsRepository;
import si.deisinger.providers.enums.Providers;

import java.util.List;

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
    public List<ChargingStationsEntity> getListOfChargingStationsPerProvider(Providers provider) {
        return chargingStationsRepository.list("provider", provider.getId());
    }
}