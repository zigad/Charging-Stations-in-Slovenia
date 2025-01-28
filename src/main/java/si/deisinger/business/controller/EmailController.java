package si.deisinger.business.controller;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.deisinger.business.configuration.ConfigUtils;
import si.deisinger.providers.enums.Providers;

@ApplicationScoped
public class EmailController {
    private static final Logger LOG = LoggerFactory.getLogger(EmailController.class);
    private static final String TO_EMAIL = ConfigUtils.getEmailUsername();

    @Inject
    Mailer mailer;

    /**
     * A method that sends an email with the commit URL for the new charging stations for the given provider.
     *
     * @param provider
     *         the provider
     * @param url
     *         the commit URL
     */
    public void sendMail(Providers provider, String url) {
        LOG.info("Sending email");
        mailer.send(Mail.withText(TO_EMAIL, "New charging station for: " + provider.getProviderName(), "Hello there sailor,\nThere are new charging stations from: " + provider.getProviderName() + "\n\n" + url));
    }
}