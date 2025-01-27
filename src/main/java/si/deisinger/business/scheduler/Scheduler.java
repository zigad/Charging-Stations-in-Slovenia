package si.deisinger.business.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.deisinger.business.ProviderProcessor;
import si.deisinger.providers.enums.Providers;
import si.deisinger.providers.model.avant2go.Avant2GoLocations;
import si.deisinger.providers.model.efrend.EfrendDetailedLocation;
import si.deisinger.providers.model.efrend.EfrendLocationPins;
import si.deisinger.providers.model.gremonaelektriko.GNEDetailedLocation;
import si.deisinger.providers.model.gremonaelektriko.GNELocationPins;
import si.deisinger.providers.model.implera.ImpleraLocations;
import si.deisinger.providers.model.megatel.MegaTelDetailedLocation;
import si.deisinger.providers.model.megatel.MegaTelLocationPins;
import si.deisinger.providers.model.mooncharge.MoonChargeLocation;
import si.deisinger.providers.model.petrol.PetrolLocations;

@ApplicationScoped
public class Scheduler {

    private static final Logger LOG = LoggerFactory.getLogger(Scheduler.class);

    /**
     * The main scheduler. This Scheduler invokes methods for checking each provider from the enum {@link Providers}. The provider is checked by calling the corresponding method from the {@link ProviderProcessor} class. Information about the provider being
     * checked is logged.
     */

    @Scheduled(every = "10m")
    void schedule() {
        ProviderProcessor providerProcessor = new ProviderProcessor();
        for (Providers provider : Providers.values()) {
            LOG.info("Checking provider: {}", provider.getProviderName());
            switch (provider) {
                case GREMONAELEKTRIKO -> providerProcessor.checkProviderStations(provider, GNELocationPins.class, GNEDetailedLocation.class);
                case PETROL -> providerProcessor.checkProviderStations(provider, PetrolLocations.class);
                case MOONCHARGE -> providerProcessor.checkProviderStations(provider, MoonChargeLocation.class);
                case AVANT2GO -> providerProcessor.checkProviderStations(provider, Avant2GoLocations.class);
                case EFREND -> providerProcessor.checkProviderStations(provider, EfrendLocationPins.class, EfrendDetailedLocation.class);
                case MEGATEL -> providerProcessor.checkProviderStations(provider, MegaTelLocationPins.class, MegaTelDetailedLocation.class);
                case IMPLERA -> providerProcessor.checkProviderStations(provider, ImpleraLocations.class);
                default -> throw new IllegalStateException("Unexpected value: " + provider);
            }
        }
    }
}
