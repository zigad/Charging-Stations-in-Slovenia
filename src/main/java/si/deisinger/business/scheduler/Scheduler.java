package si.deisinger.business.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.deisinger.business.ProviderProcessor;
import si.deisinger.business.exceptions.UnsupportedProviderException;
import si.deisinger.providers.enums.Providers;
import si.deisinger.providers.model.efrend.EfrendLocationPins;
import si.deisinger.providers.model.gremonaelektriko.GNELocationPins;
import si.deisinger.providers.model.megatel.MegaTelLocationPins;
import si.deisinger.providers.model.mooncharge.MoonChargeLocation;
import si.deisinger.providers.model.petrol.PetrolLocations;

/**
 * Scheduler responsible for invoking periodic provider checks.
 * <p>
 * This scheduler iterates over the providers defined in the {@link Providers} enum and invokes {@link ProviderProcessor#checkProviderStations(Providers, Class)} for each.
 * <p>
 */
@ApplicationScoped
public class Scheduler {

    private static final Logger LOG = LoggerFactory.getLogger(Scheduler.class);
    private final ProviderProcessor providerProcessor;

    public Scheduler(ProviderProcessor providerProcessor) {
        this.providerProcessor = providerProcessor;
    }

    /**
     * The main scheduled method which checks each provider on a fixed interval.
     * <p>
     * The scheduling interval is parameterized via configuration (with a default of 12 hours).
     */
    @Scheduled(every = "{scheduler.interval:12h}")
    void schedule() {
        for (Providers provider : Providers.values()) {
            try {
                LOG.info("Checking provider: {}", provider.getProviderName());
                switch (provider) {
                    case GREMONAELEKTRIKO -> providerProcessor.checkProviderStations(provider, GNELocationPins.class);
                    case PETROL -> providerProcessor.checkProviderStations(provider, PetrolLocations[].class);
                    case MOONCHARGE -> providerProcessor.checkProviderStations(provider, MoonChargeLocation[].class);
                    case EFREND -> providerProcessor.checkProviderStations(provider, EfrendLocationPins.class);
                    case MEGATEL -> providerProcessor.checkProviderStations(provider, MegaTelLocationPins.class);
                    case IMPLERA, AVANT2GO -> // Not implemented due to SSL issues. Skip processing.
                            LOG.info("Provider {} is disabled (not implemented)", provider.getProviderName());
                    default -> throw new UnsupportedProviderException("Unexpected provider: " + provider);
                }
            } catch (Exception e) {
                // Log error and continue with the next provider
                LOG.error("Error while checking provider {}: {}", provider.getProviderName(), e.getMessage(), e);
            }
        }
    }
}
