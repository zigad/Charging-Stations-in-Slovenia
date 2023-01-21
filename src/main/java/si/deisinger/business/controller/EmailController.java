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

	public void sendMail(Providers provider, String url) {
		LOG.info("Sending email");
		final String username = ConfigUtils.getEmailUsername();
		final String password = ConfigUtils.getEmailPassword();

		Properties prop = new Properties();
		prop.put("mail.smtp.host", ConfigUtils.getEmailHost());
		prop.put("mail.smtp.port", ConfigUtils.getEmailPort());
		prop.put("mail.smtp.auth", ConfigUtils.getEmailAuth());
		prop.put("mail.smtp.starttls.enable", ConfigUtils.getEmailTls()); //TLS

		Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(Objects.requireNonNull(ConfigUtils.getEmailUsername())));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(ConfigUtils.getEmailUsername()));
			message.setSubject("New charging station for: " + provider.getProviderName());
			message.setText("Hello there sailor,\nThere are new charging stations from: " + provider.getProviderName() + "\n\n" + url);

			Transport.send(message);

			LOG.info("Email Send");

		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
}