package si.deisinger.business.controller;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.deisinger.providers.enums.Providers;

@ApplicationScoped
public class EmailController {

    private static final Logger LOG = LoggerFactory.getLogger(EmailController.class);

    private final Mailer mailer;
    private final String recipientEmail;

    public EmailController(Mailer mailer, @ConfigProperty(name = "recipient.email") String recipientEmail) {
        this.mailer = mailer;
        this.recipientEmail = recipientEmail;
    }

    /**
     * Sends an email with new charging station details for the given provider.
     *
     * @param provider
     *         The provider.
     * @param newStations
     *         JSON string containing the new station details.
     */
    public void sendMail(Providers provider, String newStations) {
        String subject = String.format("New charging station for: %s", provider.getProviderName());
        String body = String.format("Hello there sailor,%n%nThere are new charging stations from: %s%n%n%s", provider.getProviderName(), newStations);

        LOG.info("Sending email to {} about new charging stations for provider {}", recipientEmail, provider.getProviderName());
        try {
            mailer.send(Mail.withText(recipientEmail, subject, body));
            LOG.info("Email successfully sent to {} for provider {}", recipientEmail, provider.getProviderName());
        } catch (Exception e) {
            LOG.error("Failed to send email to {} for provider {}: {}", recipientEmail, provider.getProviderName(), e.getMessage(), e);
        }
    }
}
