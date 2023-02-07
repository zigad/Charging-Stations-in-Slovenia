package si.deisinger.business.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigUtils {
	private static final String CONFIG_FILE = "configuration.properties";
	private static final Properties prop = new Properties();

	static {
		try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
			prop.load(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the jgit username from the configuration file.
	 *
	 * @return the jgit username
	 */
	public static String getJgitUsername() {
		return prop.getProperty("jgit_username");
	}

	/**
	 * Gets the jgit password from the configuration file.
	 *
	 * @return the jgit password
	 */
	public static String getJgitPassword() {
		return prop.getProperty("jgit_password");
	}

	/**
	 * Gets the email username from the configuration file.
	 *
	 * @return the email username
	 */
	public static String getEmailUsername() {
		return prop.getProperty("email_username");
	}

	/**
	 * Gets the email password from the configuration file.
	 *
	 * @return the email password
	 */
	public static String getEmailPassword() {
		return prop.getProperty("email_password");
	}

	/**
	 * Gets the email host from the configuration file.
	 *
	 * @return the email host
	 */
	public static String getEmailHost() {
		return prop.getProperty("email_host");
	}

	/**
	 * Gets the email port from the configuration file.
	 *
	 * @return the email port
	 */
	public static String getEmailPort() {
		return prop.getProperty("email_port");
	}

	/**
	 * Gets the email authentication method from the configuration file.
	 *
	 * @return the email authentication method
	 */
	public static String getEmailAuth() {
		return prop.getProperty("email_auth");
	}

	/**
	 * Gets the email TLS setting from the configuration file.
	 *
	 * @return the email TLS setting
	 */
	public static String getEmailTls() {
		return prop.getProperty("email_tls");
	}

	/**
	 * Gets whether push is enabled from the configuration file.
	 *
	 * @return true if push is enabled, false otherwise
	 */
	public static Boolean isPushEnabled() {
		return Boolean.valueOf(prop.getProperty("is_push_enabled"));
	}
}
