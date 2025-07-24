package usac.eps.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtil {
    private static final String CONFIG_FILE = "/config.properties";
    private static Properties properties = new Properties();

    static {
        try (InputStream input = ConfigUtil.class.getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            // Puedes loguear el error si lo deseas
        }
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}
