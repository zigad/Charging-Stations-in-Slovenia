package si.deisinger.business.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.deisinger.business.configuration.ConfigUtils;
import si.deisinger.providers.enums.Providers;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Objects;
import java.util.Properties;

public class EmailController {
	private static final Logger LOG = LoggerFactory.getLogger(EmailController.class);
	private static final String USERNAME = ConfigUtils.getEmailUsername();
	private static final String PASSWORD = ConfigUtils.getEmailPassword();
	private static final Properties PROPERTIES = createProperties();
	private static final Session SESSION = createSession();

	/**
	 * A method that sends an email with the commit URL for the new charging stations for the given provider.
	 *
	 * @param provider
	 * 		the provider
	 * @param url
	 * 		the commit URL
	 */
	public void sendMail(Providers provider, String url) {
		LOG.info("Sending email");
		try {
			Message message = new MimeMessage(SESSION);
			message.setFrom(new InternetAddress(Objects.requireNonNull(USERNAME)));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(USERNAME));
			message.setSubject("New charging station for: " + provider.getProviderName());
			message.setText("Hello there sailor,\nThere are new charging stations from: " + provider.getProviderName() + "\n\n" + url);
			Transport.send(message);
			LOG.info("Email Send");
		} catch (MessagingException e) {
			LOG.error("Failed to send email: " + e.getMessage(), e);
		}
	}

	/**
	 * This method creates a new Properties object with specific mail settings required for sending email.
	 *
	 * @return A Properties object containing the necessary properties for sending an email.
	 */
	private static Properties createProperties() {
		Properties properties = new Properties();
		properties.put("mail.smtp.host", ConfigUtils.getEmailHost());
		properties.put("mail.smtp.port", ConfigUtils.getEmailPort());
		properties.put("mail.smtp.auth", ConfigUtils.getEmailAuth());
		properties.put("mail.smtp.starttls.enable", ConfigUtils.getEmailTls());
		return properties;
	}

	/**
	 * This method creates a new session for sending email, with a specified Authenticator for handling authentication.
	 *
	 * @return A new session object with a specified Authenticator.
	 */
	private static Session createSession() {
		return Session.getInstance(PROPERTIES, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(USERNAME, PASSWORD);
			}
		});
	}
}