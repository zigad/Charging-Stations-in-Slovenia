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

    @ConfigProperty(name = "quarkus.mailer.recipient.email")
    private String recipientEmail;

    public EmailController(Mailer mailer) {
        this.mailer = mailer;
    }

    /**
     * Sends an email with the commit URL for new charging stations for the given provider.
     *
     * @param provider
     *         The provider.
     * @param url
     *         The commit URL.
     */
    public void sendMail(Providers provider, String url) {
        LOG.info("Sending email to {} about new charging stations for provider {}", recipientEmail, provider.getProviderName());

        String body = """
                Hello there sailor,
                
                There are new charging stations from: %s
                
                %s
                """.formatted(provider.getProviderName(), url);

        mailer.send(Mail.withText(recipientEmail, "New charging station for: " + provider.getProviderName(), body));
    }
}
