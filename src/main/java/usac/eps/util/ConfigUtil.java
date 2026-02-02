package usac.eps.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigUtil {
    private static final Logger LOGGER = Logger.getLogger(ConfigUtil.class.getName());
    private static final String CONFIG_FILE = "/config.properties";
    private static Properties properties = new Properties();

    static {
        try (InputStream input = ConfigUtil.class.getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error al cargar configuración", e);
        }
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}
