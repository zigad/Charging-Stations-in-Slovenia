package si.deisinger.business.credentials;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Credentials {
	private static final String CONFIG_FILE = "jgit.credentials";

	public static String getUsername() {
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
			prop.load(input);
			return prop.getProperty("username");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getPassword() {
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
			prop.load(input);
			return prop.getProperty("password");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
