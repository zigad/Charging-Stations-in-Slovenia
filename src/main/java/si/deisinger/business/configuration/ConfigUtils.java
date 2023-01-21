package si.deisinger.business.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigUtils {
	private static final String CONFIG_FILE = "configuration.properties";

	public static String getJgitUsername() {
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
			prop.load(input);
			return prop.getProperty("jgit_username");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getJgitPassword() {
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
			prop.load(input);
			return prop.getProperty("jgit_password");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getEmailUsername() {
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
			prop.load(input);
			return prop.getProperty("email_username");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getEmailPassword() {
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
			prop.load(input);
			return prop.getProperty("email_password");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getEmailHost() {
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
			prop.load(input);
			return prop.getProperty("email_host");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getEmailPort() {
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
			prop.load(input);
			return prop.getProperty("email_port");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getEmailAuth() {
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
			prop.load(input);
			return prop.getProperty("email_auth");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getEmailTls() {
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
			prop.load(input);
			return prop.getProperty("email_tls");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
